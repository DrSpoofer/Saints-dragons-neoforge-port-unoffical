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

public record FireBreathBurstData(int dragonId) implements ParticleOptions {
    public static final MapCodec<FireBreathBurstData> CODEC = Codec.INT.fieldOf("dragon_id")
            .xmap(FireBreathBurstData::new, FireBreathBurstData::dragonId);
    public static final StreamCodec<RegistryFriendlyByteBuf, FireBreathBurstData> STREAM_CODEC =
            StreamCodec.of((buffer, data) -> data.writeToNetwork(buffer),
                    buffer -> new FireBreathBurstData(buffer.readVarInt()));

    @Override
    public @NotNull ParticleType<FireBreathBurstData> getType() {
        return ModParticles.FIRE_BREATH_BURST.get();
    }

    public void writeToNetwork(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(dragonId);
    }
}
