package com.leon.saintsdragons.client.ui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The 1.20.1 {@code InventoryScreen#renderEntityInInventoryFollowsMouse(GuiGraphics, int, int, int, float, float, LivingEntity)}
 * overload anchored the entity's feet at (x, y) and took mouse offsets relative to that point.
 * 1.20.2+ replaced it with a scissored, box-centred variant. This keeps the original placement and
 * rotation maths, drawing through the 1.21 {@link InventoryScreen#renderEntityInInventory} with no
 * extra translation (which is exactly the old transform).
 */
public final class InventoryEntityRenderer {
    private InventoryEntityRenderer() {
    }

    public static void renderEntityFollowsMouse(GuiGraphics guiGraphics, int x, int y, int scale,
                                                float mouseX, float mouseY, LivingEntity entity) {
        float yawAngle = (float) Math.atan(mouseX / 40.0F);
        float pitchAngle = (float) Math.atan(mouseY / 40.0F);
        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf cameraOrientation = new Quaternionf().rotateX(pitchAngle * 20.0F * ((float) Math.PI / 180.0F));
        pose.mul(cameraOrientation);
        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F + yawAngle * 20.0F;
        entity.setYRot(180.0F + yawAngle * 40.0F);
        entity.setXRot(-pitchAngle * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, scale, new Vector3f(), pose, cameraOrientation, entity);
        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
    }
}
