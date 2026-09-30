package com.leon.saintsdragons.server.flight;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Minecraft 1.20.2 replaced {@code Entity#getMyRidingOffset()} with vehicle attachment points and
 * re-tuned player seating (the player offset moved from -0.35 to -0.6). Saint's Dragons' seat
 * offsets were authored against the 1.20.1 values, so dragon seats keep using them here.
 */
public final class LegacyRidingOffsets {
    /** 1.20.1 {@code Player#getMyRidingOffset()}. */
    public static final double PLAYER_RIDING_OFFSET = -0.35D;

    private LegacyRidingOffsets() {
    }

    /**
     * Equivalent of the 1.20.1 {@code passenger.getMyRidingOffset()} used by dragon seats. Players keep
     * the original -0.35 offset; other passengers use their 1.21 vehicle attachment point, the modern
     * replacement for their per-type riding offsets.
     */
    public static double myRidingOffset(Entity passenger, Entity vehicle) {
        if (passenger instanceof Player) {
            return PLAYER_RIDING_OFFSET;
        }
        return -passenger.getVehicleAttachmentPoint(vehicle).y;
    }
}
