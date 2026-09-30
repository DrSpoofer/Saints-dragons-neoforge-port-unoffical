package com.leon.saintsdragons.server.ai.pathfinding;

import com.leon.saintsdragons.server.entity.dragons.util.DragonDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class DragonWalkNodeEvaluator extends WalkNodeEvaluator implements DragonPathSearchDebuggable {
    private final BooleanSupplier canPassThroughTrees;
    private final boolean avoidWater;
    private @Nullable DragonPathSearchDebug.NodeCollector pathSearchDebugCollector;

    public DragonWalkNodeEvaluator() {
        this(false, false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees) {
        this(canPassThroughTrees, false);
    }

    public DragonWalkNodeEvaluator(boolean canPassThroughTrees, boolean avoidWater) {
        this(() -> canPassThroughTrees, avoidWater);
    }

    public DragonWalkNodeEvaluator(BooleanSupplier canPassThroughTrees) {
        this(canPassThroughTrees, false);
    }

    public DragonWalkNodeEvaluator(BooleanSupplier canPassThroughTrees, boolean avoidWater) {
        this.canPassThroughTrees = canPassThroughTrees;
        this.avoidWater = avoidWater;
    }

    @Override
    public void setPathSearchDebugCollector(@Nullable DragonPathSearchDebug.NodeCollector collector) {
        this.pathSearchDebugCollector = collector;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node current) {
        int count = super.getNeighbors(neighbors, current);
        if (this.pathSearchDebugCollector != null) {
            this.pathSearchDebugCollector.recordExpansion(current, neighbors, count);
        }
        return count;
    }

    @Override
    public @NotNull Node getStart() {
        Node vanillaStart = super.getStart();
        int footprintOffset = getFootprintOffset();
        if (footprintOffset == 0) {
            return vanillaStart;
        }

        return getStartNode(new BlockPos(
                this.mob.getBlockX() - footprintOffset,
                vanillaStart.y,
                this.mob.getBlockZ() - footprintOffset
        ));
    }

    @Override
    public @NotNull Target getTarget(double x, double y, double z) {
        int footprintOffset = getFootprintOffset();
        return new Target(getNode(
                Mth.floor(x) - footprintOffset,
                Mth.floor(y),
                Mth.floor(z) - footprintOffset
        ));
    }

    private int getFootprintOffset() {
        return Math.max(0, this.entityWidth / 2);
    }

    @Override
    public @NotNull PathType getPathType(PathfindingContext context, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.getBlockState(pos);
        if (state.is(Blocks.LADDER)) {
            return PathType.WALKABLE;
        }
        if (this.canPassThroughTrees.getAsBoolean()
                && DragonDestructionManager.isPassivelyBreakableTreeBlock(state)) {
            return PathType.OPEN;
        }
        PathType pathType = super.getPathType(context, x, y, z);
        if (avoidWater && (pathType == PathType.WATER || pathType == PathType.WATER_BORDER)) {
            return PathType.BLOCKED;
        }
        return pathType;
    }
}
