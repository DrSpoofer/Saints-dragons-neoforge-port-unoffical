package com.leon.saintsdragons.common.item.tools;

import com.leon.saintsdragons.common.registry.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public enum DragonheartWeaponTier implements Tier {
    CHUNK(4, 2600, 5.0F, 5.0F, 18, () -> Ingredient.of(ModItems.DRAGONHEART_CHUNK.get())),
    ALLOY(4, 3400, 10.0F, 7.0F, 20, () -> Ingredient.of(ModItems.DRAGONHEART_ALLOY.get()));

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    DragonheartWeaponTier(int level, int uses, float speed, float attackDamageBonus,
                          int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    /**
     * 1.21 replaced numeric harvest levels with "incorrect for tool" block tags.
     * Levels map to the vanilla tiers they matched in 1.20.1 (3 = diamond, 4 = netherite).
     */
    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return switch (this.level) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
