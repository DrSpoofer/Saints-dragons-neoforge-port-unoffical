package com.leon.saintsdragons.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Where a mount is drawn in the current frame: its camera-relative render position (the translation
 * the level renderer applies before an entity renderer runs) and its interpolated body yaw (the yaw
 * GeckoLib rotates the model by).
 */
public record RiderRenderOrigin(Vec3 position, float bodyYaw) {
    @Nullable
    public static RiderRenderOrigin of(Entity entity, float partialTick) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (entity == null || !camera.isInitialized()) {
            return null;
        }
        Vec3 cameraPos = camera.getPosition();
        Vec3 position = new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()) - cameraPos.x,
                Mth.lerp(partialTick, entity.yOld, entity.getY()) - cameraPos.y,
                Mth.lerp(partialTick, entity.zOld, entity.getZ()) - cameraPos.z
        );
        float bodyYaw = entity instanceof LivingEntity living
                ? Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot)
                : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        return new RiderRenderOrigin(position, bodyYaw);
    }
}
