package com.leon.saintsdragons.common.particle;

import com.leon.saintsdragons.common.registry.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record VolitansBreathParticleData(int dragonId) implements ParticleOptions {
    public static final MapCodec<VolitansBreathParticleData> CODEC = Codec.INT.fieldOf("dragon_id")
            .xmap(VolitansBreathParticleData::new, VolitansBreathParticleData::dragonId);
    public static final StreamCodec<RegistryFriendlyByteBuf, VolitansBreathParticleData> STREAM_CODEC =
            StreamCodec.of((buffer, data) -> data.writeToNetwork(buffer),
                    buffer -> new VolitansBreathParticleData(buffer.readVarInt()));

    @Override
    public @NotNull ParticleType<VolitansBreathParticleData> getType() {
        return ModParticles.VOLITANS_BREATH_STREAM.get();
    }

    public void writeToNetwork(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(dragonId);
    }
}
