package com.leon.saintsdragons.forge.init;

import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.common.registry.ModPotionItems;
import com.leon.saintsdragons.common.registry.ModPotions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public final class ForgeBrewingRecipes {
    private ForgeBrewingRecipes() {
    }

    public static void register(RegisterBrewingRecipesEvent event) {
        ItemStack awkwardPotion = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        ItemStack tideguardPotion = PotionContents.createItemStack(
                ModPotionItems.POTION_OF_TIDEGUARD.get(),
                ModPotions.TIDEGUARD_HOLDER
        );
        ItemStack searingPotion = PotionContents.createItemStack(
                ModPotionItems.POTION_OF_SEARING.get(),
                ModPotions.SEARING_HOLDER
        );

        event.getBuilder().addRecipe(
                DataComponentIngredient.of(true, awkwardPotion),
                Ingredient.of(ModItems.VARASUCHUS_SCALE.get()),
                tideguardPotion
        );
        event.getBuilder().addRecipe(
                DataComponentIngredient.of(true, awkwardPotion),
                Ingredient.of(ModItems.IGNIVORUS_TOOTH.get()),
                searingPotion
        );
    }
}
