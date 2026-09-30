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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public record RaevyxLightningChainData(float size, Vec3 startPos, Vec3 endPos) implements ParticleOptions {
    public static final StreamCodec<RegistryFriendlyByteBuf, RaevyxLightningChainData> STREAM_CODEC =
            StreamCodec.of((buf, data) -> data.writeToNetwork(buf), RaevyxLightningChainData::fromNetwork);

    private static RaevyxLightningChainData fromNetwork(FriendlyByteBuf buf) {
        float size = buf.readFloat();
        double startX = buf.readDouble();
        double startY = buf.readDouble();
        double startZ = buf.readDouble();
        double endX = buf.readDouble();
        double endY = buf.readDouble();
        double endZ = buf.readDouble();

        return new RaevyxLightningChainData(size, new Vec3(startX, startY, startZ), new Vec3(endX, endY, endZ));
    }

    public static MapCodec<RaevyxLightningChainData> CODEC(@SuppressWarnings("unused") ParticleType<RaevyxLightningChainData> type) {
        return RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.FLOAT.fieldOf("size").forGetter(RaevyxLightningChainData::size),
                Vec3.CODEC.fieldOf("startPos").forGetter(RaevyxLightningChainData::startPos),
                Vec3.CODEC.fieldOf("endPos").forGetter(RaevyxLightningChainData::endPos)
        ).apply(b, RaevyxLightningChainData::new));
    }

    public void writeToNetwork(@Nonnull FriendlyByteBuf buf) {
        buf.writeFloat(this.size);
        buf.writeDouble(this.startPos.x);
        buf.writeDouble(this.startPos.y);
        buf.writeDouble(this.startPos.z);
        buf.writeDouble(this.endPos.x);
        buf.writeDouble(this.endPos.y);
        buf.writeDouble(this.endPos.z);
    }

    @Override
    public @NotNull ParticleType<RaevyxLightningChainData> getType() {
        return ModParticles.LIGHTNING_CHAIN.get();
    }
}
