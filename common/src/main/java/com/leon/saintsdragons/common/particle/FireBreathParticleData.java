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


public record FireBreathParticleData(float range, float density, float scale, int dragonId) implements ParticleOptions {
    public static final MapCodec<FireBreathParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(FireBreathParticleData::range),
            Codec.FLOAT.fieldOf("density").forGetter(FireBreathParticleData::density),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(FireBreathParticleData::scale),
            Codec.INT.optionalFieldOf("dragon_id", -1).forGetter(FireBreathParticleData::dragonId)
    ).apply(instance, FireBreathParticleData::new));

    public FireBreathParticleData(float range, float density) {
        this(range, density, 1.0F);
    }

    public FireBreathParticleData(float range, float density, float scale) {
        this(range, density, scale, -1);
    }

    public FireBreathParticleData {
        range = Float.isFinite(range) ? Math.max(1, Math.min(96, range)) : (float) ExpandingBreathSection.DEFAULT_RANGE;
        density = Float.isFinite(density) ? Math.max(0, Math.min(5, density)) : 1;
        scale = Float.isFinite(scale) ? Math.max(0.1F, Math.min(5.0F, scale)) : 1.0F;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FireBreathParticleData> STREAM_CODEC =
            StreamCodec.of((buffer, data) -> data.writeToNetwork(buffer),
                    buffer -> new FireBreathParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt()));

    @Override
    public @NotNull ParticleType<FireBreathParticleData> getType() {
        return ModParticles.FIRE_BREATH_FLAME.get();
    }

    public void writeToNetwork(@NotNull FriendlyByteBuf buffer) {
        buffer.writeFloat(range);
        buffer.writeFloat(density);
        buffer.writeFloat(scale);
        buffer.writeInt(dragonId);
    }
}
