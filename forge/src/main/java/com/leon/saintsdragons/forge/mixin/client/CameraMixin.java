package com.leon.saintsdragons.forge.mixin.client;

import com.leon.saintsdragons.forge.client.accessor.CameraAccessor;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraMixin extends CameraAccessor {
    @Override
    @Invoker("move")
    void saintsdragons$invokeMove(float distance, float yaw, float pitch);

    @Override
    @Invoker("getMaxZoom")
    float saintsdragons$invokeGetMaxZoom(float distance);

    @Override
    @Invoker("setPosition")
    void saintsdragons$invokeSetPosition(double x, double y, double z);

    @Override
    @Invoker("setRotation")
    void saintsdragons$invokeSetRotation(float yaw, float pitch, float roll);
}
