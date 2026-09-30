package com.leon.saintsdragons.common.particle;

import com.leon.saintsdragons.common.registry.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public record SonicRingData(float yaw, float pitch, float scale, int duration) implements ParticleOptions {
    public static final StreamCodec<RegistryFriendlyByteBuf, SonicRingData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> data.writeToNetwork(buf),
            buf -> new SonicRingData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt()));

    public static MapCodec<SonicRingData> CODEC(@SuppressWarnings("unused") ParticleType<SonicRingData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(SonicRingData::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(SonicRingData::pitch),
                Codec.FLOAT.fieldOf("scale").forGetter(SonicRingData::scale),
                Codec.INT.fieldOf("duration").forGetter(SonicRingData::duration)
        ).apply(b, SonicRingData::new));
    }

    public void writeToNetwork(@Nonnull FriendlyByteBuf buf) {
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeFloat(this.scale);
        buf.writeInt(this.duration);
    }

    @Override
    public @NotNull ParticleType<SonicRingData> getType() {
        return ModParticles.RAEVYX_SONIC_RING.get();
    }
}
