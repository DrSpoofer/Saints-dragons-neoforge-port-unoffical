package com.leon.saintsdragons.common.particle.raevyx;

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

public record RaevyxLightningStormData(float size, boolean nightGold) implements ParticleOptions {
    public RaevyxLightningStormData(float size) {
        this(size, false);
    }

    public static StreamCodec<RegistryFriendlyByteBuf, RaevyxLightningStormData> streamCodec(
            ParticleType<RaevyxLightningStormData> type) {
        return StreamCodec.of((buf, data) -> data.writeToNetwork(buf),
                buf -> new RaevyxLightningStormData(buf.readFloat(), isNightGoldType(type)));
    }

    public static MapCodec<RaevyxLightningStormData> CODEC(ParticleType<RaevyxLightningStormData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("size").forGetter(RaevyxLightningStormData::size)
        ).apply(b, size -> new RaevyxLightningStormData(size, isNightGoldType(type))));
    }

    private static boolean isNightGoldType(ParticleType<RaevyxLightningStormData> type) {
        return type == ModParticles.LIGHTNING_STORM_NIGHT_GOLD.get();
    }

    public void writeToNetwork(@Nonnull FriendlyByteBuf buf) {
        buf.writeFloat(this.size);
    }

    @Override
    public @NotNull ParticleType<RaevyxLightningStormData> getType() {
        return this.nightGold ? ModParticles.LIGHTNING_STORM_NIGHT_GOLD.get() : ModParticles.LIGHTNING_STORM.get();
    }
}
