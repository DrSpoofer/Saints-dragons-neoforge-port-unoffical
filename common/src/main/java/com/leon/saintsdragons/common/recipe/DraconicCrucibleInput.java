package com.leon.saintsdragons.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * Minecraft 1.21 recipes match against a {@link RecipeInput} instead of a {@link Container}.
 * The crucible keeps matching against its untrimmed 3x3 grid view, exactly as in 1.20.1.
 */
public record DraconicCrucibleInput(Container container) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        return this.container.getItem(index);
    }

    @Override
    public int size() {
        return this.container.getContainerSize();
    }
}
