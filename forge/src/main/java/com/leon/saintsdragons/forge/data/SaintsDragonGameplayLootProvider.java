package com.leon.saintsdragons.forge.data;

import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.server.loot.DragonLootTables;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class SaintsDragonGameplayLootProvider implements LootTableSubProvider {
    private final boolean grooming;

    private SaintsDragonGameplayLootProvider(boolean grooming) {
        this.grooming = grooming;
    }

    public static SaintsDragonGameplayLootProvider entityTables(HolderLookup.Provider registries) {
        return new SaintsDragonGameplayLootProvider(false);
    }

    public static SaintsDragonGameplayLootProvider groomingTables(HolderLookup.Provider registries) {
        return new SaintsDragonGameplayLootProvider(true);
    }

    private static ResourceKey<LootTable> key(ResourceLocation id) {
        return ResourceKey.create(Registries.LOOT_TABLE, id);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        if (grooming) {
            generateGrooming(output);
            return;
        }
        generateEntityContext(output);
    }

    private static void generateEntityContext(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(key(DragonLootTables.ATROXIIA_FEMALE_DEATH), chanceTable(ModItems.ATROXIIA_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.CINDERVANE_FEMALE_DEATH), chanceTable(ModItems.CINDERVANE_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.IGNIVORUS_FEMALE_DEATH), chanceTable(ModItems.IGNIVORUS_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.RAEVYX_FEMALE_DEATH), chanceTable(ModItems.RAEVYX_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.STEGONAUT_FEMALE_DEATH), chanceTable(ModItems.STEGONAUT_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.VARASUCHUS_FEMALE_DEATH), chanceTable(ModItems.VARASUCHUS_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.VOLITANS_FEMALE_DEATH), chanceTable(ModItems.VOLITANS_EGG.get(), 0.12F));
        output.accept(key(DragonLootTables.IGNIVORUS_HIT), chanceTable(ModItems.IGNIVORUS_TOOTH.get(), 0.12F));
        output.accept(key(DragonLootTables.VOLITANS_HIT), chanceTable(ModItems.VOLITANS_SPINE.get(), 0.30F));
    }

    private static void generateGrooming(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(key(DragonLootTables.ATROXIIA_GROOMING), countTable(ModItems.ATROXIIA_SCALE.get(), 1, 2));
        output.accept(key(DragonLootTables.CINDERVANE_GROOMING), countTable(ModItems.CINDERVANE_SCALE.get(), 1, 1));
        output.accept(key(DragonLootTables.IGNIVORUS_GROOMING), countTable(ModItems.IGNIVORUS_SCALE.get(), 1, 2));
        output.accept(key(DragonLootTables.RAEVYX_GROOMING), countTable(ModItems.RAEVYX_SCALE.get(), 1, 2));
        output.accept(key(DragonLootTables.STEGONAUT_GROOMING), countTable(ModItems.STEGONAUT_SCALE.get(), 1, 2));
        output.accept(key(DragonLootTables.VARASUCHUS_GROOMING), countTable(ModItems.VARASUCHUS_SCALE.get(), 1, 2));
        output.accept(key(DragonLootTables.VOLITANS_GROOMING), countTable(ModItems.VOLITANS_SCALE.get(), 1, 2));
    }

    private static LootTable.Builder chanceTable(ItemLike item, float chance) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .setBonusRolls(ConstantValue.exactly(0))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(item)));
    }

    private static LootTable.Builder countTable(ItemLike item, int min, int max) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .setBonusRolls(ConstantValue.exactly(0))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))));
    }
}
