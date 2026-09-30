package com.leon.saintsdragons.client.renderer.item;

import com.leon.saintsdragons.client.model.item.MossbackItemModel;
import com.leon.saintsdragons.common.item.MossbackItem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.model.GeoModel;

public class MossbackItemRenderer extends GeoItemRenderer<MossbackItem> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("saintsdragons", "item/mossback/mossback");
    private static final RenderType GUI_RENDER_TYPE = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);
    private final MossbackItemModel babyModel = new MossbackItemModel(true);

    public MossbackItemRenderer() {
        super(new MossbackItemModel());
    }

    @Override
    public GeoModel<MossbackItem> getGeoModel() {
        return MossbackItem.isBaby(getCurrentItemStack()) ? babyModel : super.getGeoModel();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (displayContext == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
            renderGuiSprite(poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
            if (bufferSource instanceof MultiBufferSource.BufferSource buffers) {
                buffers.endBatch(GUI_RENDER_TYPE);
            }
            Lighting.setupFor3DItems();
            return;
        }

        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private static void renderGuiSprite(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, int packedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(GUI_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(GUI_RENDER_TYPE);
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose normal = poseStack.last();
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vertex(consumer, pose, normal, 0.0F, 0.0F, u0, v1, packedLight, packedOverlay);
        vertex(consumer, pose, normal, 1.0F, 0.0F, u1, v1, packedLight, packedOverlay);
        vertex(consumer, pose, normal, 1.0F, 1.0F, u1, v0, packedLight, packedOverlay);
        vertex(consumer, pose, normal, 0.0F, 1.0F, u0, v0, packedLight, packedOverlay);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose normal,
                               float x, float y, float u, float v, int packedLight, int packedOverlay) {
        consumer.addVertex(pose, x, y, 0.5F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(normal, 0.0F, 0.0F, 1.0F);
    }
}
