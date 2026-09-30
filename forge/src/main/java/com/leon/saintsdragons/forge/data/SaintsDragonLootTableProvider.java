package com.leon.saintsdragons.forge.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public final class SaintsDragonLootTableProvider {
    private SaintsDragonLootTableProvider() {
    }

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(
                output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(SaintsDragonEntityLootProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(SaintsDragonGameplayLootProvider::entityTables, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(SaintsDragonGameplayLootProvider::groomingTables, LootContextParamSets.CHEST)
                ),
                registries
        );
    }
}
