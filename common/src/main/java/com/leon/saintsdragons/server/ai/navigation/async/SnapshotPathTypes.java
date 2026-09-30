package com.leon.saintsdragons.server.ai.navigation.async;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Worker-thread path-type classification over an {@link ImmutableBlockSnapshot}.
 * <p>
 * 1.20.1's {@code WalkNodeEvaluator#getBlockPathTypeStatic(BlockGetter, MutableBlockPos)} became
 * {@code getPathTypeStatic(PathfindingContext, MutableBlockPos)} in 1.21. A {@code PathfindingContext}
 * needs a {@code CollisionGetter} and a mob and reads the server level's path-type cache, none of
 * which may be touched from the async search. This mirrors the 1.21 algorithm exactly, evaluating the
 * same per-block types ({@code getPathTypeFromState}) directly against the snapshot.
 */
final class SnapshotPathTypes extends WalkNodeEvaluator {
    private SnapshotPathTypes() {
    }

    static PathType getPathTypeStatic(BlockGetter level, BlockPos.MutableBlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        PathType type = getPathTypeFromState(level, cursor.set(x, y, z));
        if (type == PathType.OPEN && y >= level.getMinBuildHeight() + 1) {
            return switch (getPathTypeFromState(level, cursor.set(x, y - 1, z))) {
                case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
                case DAMAGE_FIRE -> PathType.DAMAGE_FIRE;
                case DAMAGE_OTHER -> PathType.DAMAGE_OTHER;
                case STICKY_HONEY -> PathType.STICKY_HONEY;
                case POWDER_SNOW -> PathType.DANGER_POWDER_SNOW;
                case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
                case TRAPDOOR -> PathType.DANGER_TRAPDOOR;
                default -> checkNeighbourBlocks(level, cursor, x, y, z, PathType.WALKABLE);
            };
        }
        return type;
    }

    private static PathType checkNeighbourBlocks(BlockGetter level, BlockPos.MutableBlockPos cursor,
                                                 int x, int y, int z, PathType fallback) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    cursor.set(x + dx, y + dy, z + dz);
                    PathType type = getPathTypeFromState(level, cursor);
                    BlockState state = level.getBlockState(cursor);
                    PathType blockType = state.getAdjacentBlockPathType(level, cursor, null, type);
                    if (blockType != null) {
                        return blockType;
                    }
                    FluidState fluid = state.getFluidState();
                    PathType fluidType = fluid.getAdjacentBlockPathType(level, cursor, null, type);
                    if (fluidType != null) {
                        return fluidType;
                    }
                    if (type == PathType.DAMAGE_OTHER) {
                        return PathType.DANGER_OTHER;
                    }
                    if (type == PathType.DAMAGE_FIRE || type == PathType.LAVA) {
                        return PathType.DANGER_FIRE;
                    }
                    if (type == PathType.WATER) {
                        return PathType.WATER_BORDER;
                    }
                    if (type == PathType.DAMAGE_CAUTIOUS) {
                        return PathType.DAMAGE_CAUTIOUS;
                    }
                }
            }
        }
        return fallback;
    }
}
