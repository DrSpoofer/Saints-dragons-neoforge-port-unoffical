package com.leon.saintsdragons.forge.client.accessor;

public interface CameraAccessor {
    // 1.21 Camera#move and Camera#getMaxZoom take floats.
    void saintsdragons$invokeMove(float distance, float yaw, float pitch);
    float saintsdragons$invokeGetMaxZoom(float distance);
    void saintsdragons$invokeSetPosition(double x, double y, double z);
    // NeoForge's Camera#setRotation(yaw, pitch, roll).
    void saintsdragons$invokeSetRotation(float yaw, float pitch, float roll);
}
