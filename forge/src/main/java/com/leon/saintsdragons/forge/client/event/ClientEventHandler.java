package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.camera.ClientCameraImpulse;
import com.leon.saintsdragons.client.camera.DragonFovEffects;
import com.leon.saintsdragons.client.camera.DragonRideCameraController;
import com.leon.saintsdragons.client.camera.DragonDiveCameraWobble;
import com.leon.saintsdragons.client.init.CommonClientLifecycleEvents;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.forge.client.accessor.CameraAccessor;
import com.leon.saintsdragons.forge.client.camera.CameraLeanData;
import com.leon.saintsdragons.forge.client.camera.DragonCameraState;
import com.leon.saintsdragons.forge.platform.ForgeClientConfig;
import com.leon.saintsdragons.server.entity.base.RideableDragonBase;
import com.leon.saintsdragons.server.entity.dragons.atroxiia.Atroxiia;
import com.leon.saintsdragons.server.entity.dragons.cindervane.Cindervane;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.leon.saintsdragons.server.entity.dragons.stegonaut.Stegonaut;
import com.leon.saintsdragons.server.entity.dragons.varasuchus.Varasuchus;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import com.leon.saintsdragons.server.entity.interfaces.ShakesScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Client camera hooks.
 *
 * <p>In 1.20.1 Forge, {@code ViewportEvent.ComputeCameraAngles} was posted by the game renderer after
 * {@code Camera#setup} had positioned the camera, so the rider camera could zoom and offset it with
 * {@code Camera#move} from that event. NeoForge 21.1 posts the event inside {@code Camera#setup} before
 * the camera is positioned, which silently discards those moves. The work is therefore split:
 * <ul>
 *     <li>{@link #onComputeCamera}: rotation that must exist before the camera is positioned
 *     (first-person bank roll, used by the seat anchor);</li>
 *     <li>{@link #onCalculateDetachedCameraDistance}: the per-dragon third-person distance, through
 *     NeoForge's dedicated detached-distance event (vanilla then applies block collision);</li>
 *     <li>{@link #onCameraSetupComplete}: everything else the 1.20.1 event did, run at the end of
 *     {@code Camera#setup} (see {@code CameraPositionMixin}), which is when the 1.20.1 event ran.</li>
 * </ul>
 */
@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static final double[] randomTremorOffsets = new double[3];

    // Raevyx beam camera state
    private static boolean wasBeaming = false;
    private static CameraType previousPerspective = null;
    private static float beamCameraForward = 0.0f;
    private static float beamCameraUp = 0.0f;

    // Rider camera output for the frame being set up; null when the dragon rider camera is not active.
    private static DragonRideCameraController.CameraOutput detachedCameraOutput = null;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(DragonFovEffects.apply(event.getFOV(), (float) event.getPartialTick()));
    }

    @SubscribeEvent
    public static void onComputeCamera(ViewportEvent.ComputeCameraAngles event) {
        // Posted first in Camera#setup, so this also starts a new camera frame.
        detachedCameraOutput = null;
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player == null) return;
        if (!applyFirstPersonDragonCamera(player, event)) {
            CameraLeanData.reset();
            DragonCameraState.clearRoll();
        }
    }

    @SubscribeEvent
    public static void onCalculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player == null) return;
        Entity vehicle = player.getVehicle();
        if (!DragonRideCameraController.supports(vehicle)) {
            return;
        }
        if (vehicle instanceof Raevyx raevyx && raevyx.isBeaming()
                && ForgeClientConfig.isRaevyxBeamFirstPersonEnabled()) {
            return;
        }

        DragonRideCameraController.CameraOutput output =
                DragonRideCameraController.update(vehicle, event.getCamera().getPartialTickTime());
        detachedCameraOutput = output;
        // 1.20.1 pulled the camera back by the dragon's zoom on top of vanilla's third-person distance,
        // then clipped against blocks. The vanilla distance is multiplied by the entity scale afterwards,
        // so divide the unscaled dragon zoom by it.
        float entityScale = event.getEntityScalingFactor();
        float extra = entityScale > 0.0F ? output.zoom() / entityScale : output.zoom();
        event.setDistance(event.getDistance() + extra);
    }

    /**
     * Called at the end of {@code Camera#setup}, after vanilla positioning and the seat anchor.
     */
    public static void onCameraSetupComplete(Camera camera, float partialTick) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player == null) return;
        Entity vehicle = player.getVehicle();

        // In 1.20.1 the event's angle changes did not rotate the camera until after these moves,
        // so they are collected here and applied last.
        CameraAngles angles = new CameraAngles(camera.getYRot(), camera.getXRot(), camera.getRoll());
        handleRaevyxBeamCamera(camera, vehicle);

        if (camera.isDetached()) {
            if (!applyDetachedDragonCamera(camera, angles)) {
                DragonRideCameraController.reset();
            }
        } else if (!player.isPassenger() || !DragonRideCameraController.supports(vehicle)) {
            DragonRideCameraController.reset();
        }

        applyDiveCameraWobble(angles, vehicle, partialTick);
        applyCameraShakeAndImpulse(camera, player, partialTick);
        angles.applyTo(camera);
    }

    private static void applyCameraShakeAndImpulse(Camera camera, Entity player, float partialTick) {
        double shakeDistanceScale = 64.0;
        double distance = Double.MAX_VALUE;
        // Screen shake system
        float tremorAmount = 0.0F; // Reset tremor amount each frame

        AABB aabb = player.getBoundingBox().inflate(shakeDistanceScale);
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        for (Mob screenShaker : level.getEntitiesOfClass(Mob.class, aabb, (mob -> mob instanceof ShakesScreen))) {
            ShakesScreen shakesScreen = (ShakesScreen) screenShaker;
            if (shakesScreen.canFeelShake(player) && screenShaker.distanceTo(player) < distance) {
                distance = screenShaker.distanceTo(player);
                float shakeAmount = shakesScreen.getScreenShakeAmount(partialTick);
                tremorAmount = Math.min((1F - (float) Math.min(1, distance / shakesScreen.getShakeDistance())) * Math.max(shakeAmount, 0F), 2.0F);
            }
        }

        if (tremorAmount > 0) {
            // Generate random offsets for camera movement
            double intensity = tremorAmount * Minecraft.getInstance().options.screenEffectScale().get();
            camera.move((float) (randomTremorOffsets[0] * 0.2F * intensity),
                    (float) (randomTremorOffsets[1] * 0.2F * intensity),
                    (float) (randomTremorOffsets[2] * 0.5F * intensity));

            // Update random offsets for next frame
            randomTremorOffsets[0] = (Math.random() - 0.5) * 2.0;
            randomTremorOffsets[1] = (Math.random() - 0.5) * 2.0;
            randomTremorOffsets[2] = (Math.random() - 0.5) * 2.0;
        }

        ClientCameraImpulse.Offset impulse = ClientCameraImpulse.sample(partialTick);
        if (impulse.active()) {
            camera.move((float) impulse.forward(), (float) impulse.vertical(), (float) impulse.lateral());
        }
    }

    private static void applyDiveCameraWobble(CameraAngles angles, Entity vehicle, float partialTick) {
        if (!ForgeClientConfig.DIVE_CAMERA_WOBBLE_ENABLED.get()) {
            return;
        }
        if (vehicle instanceof Raevyx raevyx && raevyx.isBeaming()
                && ForgeClientConfig.isRaevyxBeamFirstPersonEnabled()) {
            return;
        }

        DragonDiveCameraWobble.Output wobble = DragonDiveCameraWobble.get(vehicle, partialTick);
        if (!wobble.active()) {
            return;
        }

        angles.yaw += wobble.yawDegrees();
        angles.pitch = Mth.clamp(angles.pitch + wobble.pitchDegrees(), -90.0F, 90.0F);
        angles.roll += wobble.rollDegrees();
    }

    private static void handleRaevyxBeamCamera(Camera camera, Entity vehicle) {
        // Disabling the override or dismounting releases it just like ending the beam.
        boolean isBeaming = ForgeClientConfig.isRaevyxBeamFirstPersonEnabled()
                && vehicle instanceof Raevyx raevyx && raevyx.isBeaming();
        Minecraft mc = Minecraft.getInstance();
        if (isBeaming && !wasBeaming) {
            previousPerspective = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            wasBeaming = true;
        } else if (!isBeaming && wasBeaming) {
            if (previousPerspective != null) {
                mc.options.setCameraType(previousPerspective);
                previousPerspective = null;
            }
            wasBeaming = false;
            beamCameraForward = 0.0f;
            beamCameraUp = 0.0f;
        }

        if (!isBeaming) {
            return;
        }

        float targetForward = 7.5f;
        float targetUp = -2.0f;
        float blendRate = 0.2f;
        beamCameraForward += (targetForward - beamCameraForward) * blendRate;
        beamCameraUp += (targetUp - beamCameraUp) * blendRate;
        camera.move(beamCameraForward, 0, 0);
        camera.move(0, -beamCameraUp, 0);
    }

    private static boolean applyDetachedDragonCamera(Camera camera, CameraAngles angles) {
        // The zoom itself was applied through CalculateDetachedCameraDistanceEvent.
        DragonRideCameraController.CameraOutput output = detachedCameraOutput;
        if (output == null) {
            return false;
        }

        double lateralShift = isThirdPersonBankingCameraEnabled() ? output.lateralShift() : 0.0D;
        camera.move(0, (float) output.verticalShift(), (float) lateralShift);
        angles.pitch = Mth.clamp(angles.pitch + output.pitchOffset(), -90.0f, 90.0f);
        return true;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        CommonClientLifecycleEvents.bootstrap();
        CommonClientLifecycleEvents.onEndClientTick(Minecraft.getInstance());
    }

    private static boolean applyFirstPersonDragonCamera(Entity player, ViewportEvent.ComputeCameraAngles event) {
        if (event.getCamera().isDetached()) {
            return false;
        }

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof RideableDragonBase dragon) || !usesFirstPersonDragonCamera(dragon)) {
            return false;
        }

        if (!isFirstPersonBankingCameraEnabled()) {
            CameraLeanData.reset();
            DragonCameraState.clearRoll();
            return true;
        }

        if (dragon instanceof Raevyx raevyx && raevyx.isBeaming()
                && ForgeClientConfig.isRaevyxBeamFirstPersonEnabled()) {
            CameraLeanData.reset();
            DragonCameraState.clearRoll();
            return true;
        }
        if (!usesAerialBankingCamera(dragon)) {
            CameraLeanData.reset();
            DragonCameraState.clearRoll();
            return true;
        }

        // NeoForge builds Camera#rotation from the event roll, so this also covers what 1.20.1's
        // CameraRollMixin did (rolling the camera's own rotation and vectors).
        event.setRoll(event.getRoll() + DragonCameraState.getCurrentRoll());
        return true;
    }

    private static boolean usesFirstPersonDragonCamera(RideableDragonBase dragon) {
        return com.leon.saintsdragons.client.renderer.DragonSeatAnchoredCamera.supports(dragon);
    }

    private static boolean isFirstPersonBankingCameraEnabled() {
        return ForgeClientConfig.FIRST_PERSON_BANKING_CAMERA_ENABLED == null
                || ForgeClientConfig.FIRST_PERSON_BANKING_CAMERA_ENABLED.get();
    }

    private static boolean isThirdPersonBankingCameraEnabled() {
        return ForgeClientConfig.THIRD_PERSON_BANKING_CAMERA_ENABLED == null
                || ForgeClientConfig.THIRD_PERSON_BANKING_CAMERA_ENABLED.get();
    }

    private static boolean usesAerialBankingCamera(RideableDragonBase dragon) {
        return dragon.isFlying()
                || dragon.isTakeoff()
                || dragon.isLanding()
                || dragon.isHovering();
    }

    private static final class CameraAngles {
        private final float originalYaw;
        private final float originalPitch;
        private final float originalRoll;
        private float yaw;
        private float pitch;
        private float roll;

        private CameraAngles(float yaw, float pitch, float roll) {
            this.originalYaw = this.yaw = yaw;
            this.originalPitch = this.pitch = pitch;
            this.originalRoll = this.roll = roll;
        }

        private void applyTo(Camera camera) {
            if (this.yaw != this.originalYaw || this.pitch != this.originalPitch || this.roll != this.originalRoll) {
                ((CameraAccessor) camera).saintsdragons$invokeSetRotation(this.yaw, this.pitch, this.roll);
            }
        }
    }
}
