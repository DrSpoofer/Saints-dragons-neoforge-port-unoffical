package com.leon.saintsdragons.forge.platform;

import com.leon.saintsdragons.platform.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * NeoForge 1.21 payload implementation of the shared network abstraction.
 * <p>
 * The 1.20.1 Forge SimpleChannel is replaced by one {@link CustomPacketPayload} type per message,
 * registered through {@link RegisterPayloadHandlersEvent}. Each message keeps its original
 * encoder/decoder and handler; payload handlers run on the main thread (NeoForge's default),
 * matching the previous {@code consumerMainThread} behaviour, and serverbound handlers still
 * only run with a sending server player.
 */
public final class ForgeNetworkHelper implements NetworkHelper {
    private static final String PROTOCOL_VERSION = "2";

    private final List<Consumer<PayloadRegistrar>> registrations = new ArrayList<>();
    private final Map<Class<?>, CustomPacketPayload.Type<?>> payloadTypes = new ConcurrentHashMap<>();
    private boolean listening;

    public ForgeNetworkHelper() {
        // Registration with the mod event bus is deferred until the first message is registered
        // to avoid ServiceConfigurationError during early class loading.
    }

    /** Wraps a shared message object so it can travel as a NeoForge payload. */
    public record MessagePayload<T>(CustomPacketPayload.Type<MessagePayload<T>> payloadType, T message)
            implements CustomPacketPayload {
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return this.payloadType;
        }
    }

    private void ensureListening() {
        if (!this.listening) {
            this.listening = true;
            NeoForgePlatformContext.modEventBus().addListener(RegisterPayloadHandlersEvent.class, this::onRegisterPayloads);
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        for (Consumer<PayloadRegistrar> registration : this.registrations) {
            registration.accept(registrar);
        }
    }

    private <T> CustomPacketPayload.Type<MessagePayload<T>> createType(Class<T> type, ResourceLocation id) {
        CustomPacketPayload.Type<MessagePayload<T>> payloadType = new CustomPacketPayload.Type<>(id);
        if (this.payloadTypes.putIfAbsent(type, payloadType) != null) {
            throw new IllegalStateException("Duplicate network message registration for " + type.getName());
        }
        return payloadType;
    }

    private static <T> StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> codec(
            CustomPacketPayload.Type<MessagePayload<T>> payloadType,
            PacketEncoder<T> encoder,
            PacketDecoder<T> decoder) {
        return StreamCodec.of(
                (buffer, payload) -> encoder.encode(payload.message(), buffer),
                buffer -> new MessagePayload<>(payloadType, decoder.decode(buffer)));
    }

    @Override
    public <T> void registerServerbound(Class<T> type,
                                        ResourceLocation id,
                                        PacketEncoder<T> encoder,
                                        PacketDecoder<T> decoder,
                                        ServerboundHandler<T> handler) {
        CustomPacketPayload.Type<MessagePayload<T>> payloadType = createType(type, id);
        StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> codec = codec(payloadType, encoder, decoder);
        this.registrations.add(registrar -> registrar.playToServer(payloadType, codec, (payload, context) -> {
            if (context.player() instanceof ServerPlayer sender) {
                handler.handle(payload.message(), sender);
            }
        }));
        ensureListening();
    }

    @Override
    public <T> void registerClientbound(Class<T> type,
                                        ResourceLocation id,
                                        PacketEncoder<T> encoder,
                                        PacketDecoder<T> decoder,
                                        ClientboundHandler<T> handler) {
        CustomPacketPayload.Type<MessagePayload<T>> payloadType = createType(type, id);
        StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> codec = codec(payloadType, encoder, decoder);
        this.registrations.add(registrar -> registrar.playToClient(payloadType, codec,
                (payload, context) -> handler.handle(payload.message())));
        ensureListening();
    }

    @SuppressWarnings("unchecked")
    private <T> MessagePayload<T> wrap(T message) {
        CustomPacketPayload.Type<MessagePayload<T>> payloadType =
                (CustomPacketPayload.Type<MessagePayload<T>>) this.payloadTypes.get(message.getClass());
        if (payloadType == null) {
            throw new IllegalArgumentException("Unregistered network message " + message.getClass().getName());
        }
        return new MessagePayload<>(payloadType, message);
    }

    @Override
    public void sendToServer(Object message) {
        PacketDistributor.sendToServer(wrap(message));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object message) {
        PacketDistributor.sendToPlayer(player, wrap(message));
    }

    @Override
    public void sendToTracking(Entity entity, Object message) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, wrap(message));
    }

    @Override
    public void sendToDimension(Level level, Object message) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, wrap(message));
        }
    }
}
