package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.ui.DragonRideHealthBar;
import com.leon.saintsdragons.client.ui.SpeedLineOverlay;
import com.leon.saintsdragons.client.ui.DragonUIRegistry;
import com.leon.saintsdragons.client.ui.FireballChargeIndicator;
import com.leon.saintsdragons.client.ui.IgnivorusFireBreathMeterIndicator;
import com.leon.saintsdragons.client.ui.MeleeModeNotification;
import com.leon.saintsdragons.client.ui.RaevyxBeamMeterIndicator;
import com.leon.saintsdragons.client.ui.SwarmWaveBarOverlay;
import com.leon.saintsdragons.client.debug.DragonBrainDebugHud;
import com.leon.saintsdragons.client.ui.VolitansBreathMeterIndicator;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.base.RideableGroundDragon;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import com.leon.saintsdragons.forge.platform.ForgeClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public class DragonUIEventHandler {
    private static final MeleeModeNotification meleeModeNotification = new MeleeModeNotification();
    private static final FireballChargeIndicator fireballChargeIndicator = new FireballChargeIndicator();
    private static final RaevyxBeamMeterIndicator raevyxBeamMeterIndicator = new RaevyxBeamMeterIndicator();
    private static final IgnivorusFireBreathMeterIndicator ignivorusFireBreathMeterIndicator = new IgnivorusFireBreathMeterIndicator();
    private static final VolitansBreathMeterIndicator volitansBreathMeterIndicator = new VolitansBreathMeterIndicator();
    private static final DragonRideHealthBar rideHealthBar = new DragonRideHealthBar();
    private static final SpeedLineOverlay diveSpeedLineOverlay = SpeedLineOverlay.INSTANCE;

    static {
        DragonUIRegistry.init(meleeModeNotification);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof DragonEntity)) {
            return;
        }

        if (minecraft.player.getVehicle() instanceof RideableGroundDragon
                && (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)
                || event.getName().equals(VanillaGuiLayers.JUMP_METER))) {
            event.setCanceled(true);
            return;
        }

        if (!DragonUIRegistry.isUIVisible()) {
            return;
        }

        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        } else if (event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            event.setCanceled(true);
        } else if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)
                && !(minecraft.player.getVehicle() instanceof PlayerRideableJumping)) {
            event.setCanceled(true);
        } else if (event.getName().equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
            event.setCanceled(true);
        } else if (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);
        } else if (event.getName().equals(VanillaGuiLayers.AIR_LEVEL)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            SwarmWaveBarOverlay.render(event.getGuiGraphics(), screenWidth, event.getPartialTick().getGameTimeDeltaPartialTick(false));
            DragonBrainDebugHud.render(event.getGuiGraphics(), screenWidth, screenHeight);
        }

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)
                && ForgeClientConfig.DIVE_SPEED_LINES_ENABLED.get()) {
            diveSpeedLineOverlay.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)
                && minecraft.player.getVehicle() instanceof RideableGroundDragon groundDragon) {
            int x = screenWidth / 2 - 91;
            if (minecraft.player.getJumpRidingScale() > 0.0F) {
                minecraft.gui.renderJumpMeter(groundDragon, event.getGuiGraphics(), x);
            } else if (!DragonUIRegistry.isUIVisible()
                    && minecraft.gameMode != null
                    && minecraft.gameMode.hasExperience()) {
                minecraft.gui.renderExperienceBar(event.getGuiGraphics(), x);
            }
        }

        DragonEntity currentDragon = null;
        if (minecraft.player.getVehicle() instanceof DragonEntity dragon) {
            currentDragon = dragon;
            rideHealthBar.setDragon(dragon);
        }
        meleeModeNotification.render(event.getGuiGraphics(), screenWidth, screenHeight);

        if (!DragonUIRegistry.isUIVisible()) {
            return;
        }

        if (currentDragon instanceof Ignivorus ignivorus) {
            fireballChargeIndicator.setChargeLevel(ignivorus.getFireballChargeLevel());
            fireballChargeIndicator.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
            ignivorusFireBreathMeterIndicator.setBreathEnergy(ignivorus.getFireBreathEnergy());
            ignivorusFireBreathMeterIndicator.setBreathing(ignivorus.isBreathingFire());
            ignivorusFireBreathMeterIndicator.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        } else if (currentDragon instanceof Raevyx raevyx) {
            raevyxBeamMeterIndicator.setBeamEnergy(raevyx.getBeamEnergy());
            raevyxBeamMeterIndicator.setBeaming(raevyx.isBeaming());
            raevyxBeamMeterIndicator.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        } else if (currentDragon instanceof Volitans volitans) {
            volitansBreathMeterIndicator.setWaterEnergy(volitans.getWaterBreathEnergy());
            volitansBreathMeterIndicator.setPoisonEnergy(volitans.getPoisonBreathEnergy());
            volitansBreathMeterIndicator.setBreathMode(volitans.getBreathMode());
            volitansBreathMeterIndicator.setBreathing(volitans.isBreathing());
            volitansBreathMeterIndicator.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }

        if (currentDragon != null) {
            rideHealthBar.render(event.getGuiGraphics(), screenWidth, screenHeight, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DragonUIKeybinds.handleKeybinds();
        meleeModeNotification.tick();
        fireballChargeIndicator.tick();
        raevyxBeamMeterIndicator.tick();
        ignivorusFireBreathMeterIndicator.tick();
        volitansBreathMeterIndicator.tick();
    }
}
