package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.debug.DragonPathDebugClient;
import com.leon.saintsdragons.client.debug.DragonPathDebugRenderer;
import com.leon.saintsdragons.client.debug.DragonBrainDebugClient;
import com.leon.saintsdragons.client.debug.DragonBrainDebugHud;
import com.leon.saintsdragons.client.debug.DragonBrainDebugRenderer;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public final class DragonPathDebugForgeHandler {
    private DragonPathDebugForgeHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            DragonPathDebugClient.clear();
            DragonBrainDebugClient.clear();
            return;
        }
        DragonPathDebugClient.tick();
        DragonBrainDebugClient.tick();
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getScrollDeltaY() == 0.0D
                || minecraft.player == null
                || minecraft.screen != null
                || !Screen.hasShiftDown()
                || (!minecraft.player.getMainHandItem().is(Items.DEBUG_STICK)
                && !minecraft.player.getOffhandItem().is(Items.DEBUG_STICK))) {
            return;
        }

        int direction = event.getScrollDeltaY() < 0.0D ? 1 : -1;
        if (DragonBrainDebugHud.cyclePage(direction)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || (DragonPathDebugClient.getSnapshot() == null
                && DragonBrainDebugClient.getSnapshot() == null)) {
            return;
        }

        if (DragonPathDebugClient.getSnapshot() != null) {
            DragonPathDebugRenderer.render(event.getPoseStack(), event.getCamera().getPosition());
        }
        if (DragonBrainDebugClient.getSnapshot() != null) {
            DragonBrainDebugRenderer.render(event.getPoseStack(), event.getCamera().getPosition());
        }
    }
}
