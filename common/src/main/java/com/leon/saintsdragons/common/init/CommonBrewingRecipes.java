package com.leon.saintsdragons.common.init;

import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.common.registry.ModPotions;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

public final class CommonBrewingRecipes {
    private CommonBrewingRecipes() {
    }

    // 1.21 builds potion brewing through a per-registry-load builder instead of static mixes.
    public static void register(PotionBrewing.Builder builder) {
        builder.addMix(Potions.AWKWARD, ModItems.VARASUCHUS_SCALE.get(), ModPotions.TIDEGUARD_HOLDER);
        builder.addMix(Potions.AWKWARD, ModItems.IGNIVORUS_TOOTH.get(), ModPotions.SEARING_HOLDER);
    }
}
