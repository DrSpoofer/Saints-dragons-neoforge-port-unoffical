package com.leon.saintsdragons.client.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Minecraft 1.20.2 changed {@code ImageButton} to draw GUI-atlas sprites. Saint's Dragons' screens
 * use texture-sheet regions (hovered state offset by {@code yDiffTex}), so this reproduces the
 * 1.20.1 {@code ImageButton}/{@code AbstractWidget#renderTexture} behaviour with the same arguments.
 */
public class TextureSheetButton extends Button {
    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    public TextureSheetButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
                              ResourceLocation resourceLocation, int textureWidth, int textureHeight,
                              OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight,
                onPress, CommonComponents.EMPTY);
    }

    public TextureSheetButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
                              ResourceLocation resourceLocation, int textureWidth, int textureHeight,
                              OnPress onPress, Component message) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int v = this.yTexStart;
        if (!this.isActive()) {
            v += this.yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            v += this.yDiffTex;
        }
        RenderSystem.enableDepthTest();
        guiGraphics.blit(this.resourceLocation, this.getX(), this.getY(), (float) this.xTexStart, (float) v,
                this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
