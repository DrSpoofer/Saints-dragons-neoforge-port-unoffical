package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.ui.DragonUIRegistry;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinds for Dragon UI system
 */
@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public class DragonUIKeybinds {
    public static final KeyMapping TOGGLE_DRAGON_UI = new KeyMapping(
        "key.saintsdragons.toggle_dragon_ui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F4,
        "key.categories.saintsdragons"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_DRAGON_UI);
    }

    /**
     * Handle keybind events
     */
    public static void handleKeybinds() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        if (TOGGLE_DRAGON_UI.consumeClick()) {
            DragonUIRegistry.toggleUIVisibility();
        }
    }
}
