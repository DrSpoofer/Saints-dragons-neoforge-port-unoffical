package com.leon.saintsdragons.forge;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.config.SaintsDragonsConfig;
import com.leon.saintsdragons.common.config.ConfigStorageLayout;
import com.leon.saintsdragons.common.config.dragon.DragonAttributeConfigLoader;
import com.leon.saintsdragons.common.block.crucible.DraconicCrucibleThermalReloadListener;
import com.leon.saintsdragons.server.entity.variant.DragonVariantReloadListener;
import com.leon.saintsdragons.server.entity.npc.chatter.IvyChatterReloadListener;
import com.leon.saintsdragons.server.entity.npc.trade.IvyTradeReloadListener;
import com.leon.saintsdragons.server.entity.npc.dialogue.DialogueReloadListener;
import com.leon.saintsdragons.server.loot.DragonChestLootReloadListener;
import com.leon.saintsdragons.common.init.CommonModEvents;
import com.leon.saintsdragons.common.registry.ModAttributes;
import com.leon.saintsdragons.common.registry.ModPotions;
import com.leon.saintsdragons.forge.client.ForgeConfigRootScreen;
import com.leon.saintsdragons.forge.data.SaintsDragonBiomeTagsProvider;
import com.leon.saintsdragons.forge.data.SaintsDragonBlockTagsProvider;
import com.leon.saintsdragons.forge.data.SaintsDragonEntityTypeTagsProvider;
import com.leon.saintsdragons.forge.data.SaintsDragonItemTagsProvider;
import com.leon.saintsdragons.forge.data.SaintsDragonLootTableProvider;
import com.leon.saintsdragons.forge.init.ForgeBrewingRecipes;
import com.leon.saintsdragons.forge.loot.ModLootModifiers;
import com.leon.saintsdragons.forge.mixin.RangedAttributeAccessor;
import com.leon.saintsdragons.forge.platform.ForgeClientConfig;
import com.leon.saintsdragons.forge.platform.ForgeDragonAttributesConfig;
import com.leon.saintsdragons.forge.world.AddConditionalFeaturesBiomeModifier;
import com.leon.saintsdragons.forge.world.AddDragonsBiomeModifier;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.dragons.cindervane.Cindervane;
import com.leon.saintsdragons.server.entity.dragons.ignivorus.Ignivorus;
import com.leon.saintsdragons.server.entity.dragons.nulljaw.Nulljaw;
import com.leon.saintsdragons.server.entity.dragons.raevyx.Raevyx;
import com.leon.saintsdragons.server.entity.dragons.stegonaut.Stegonaut;
import com.leon.saintsdragons.server.entity.dragons.varasuchus.Varasuchus;
import com.leon.saintsdragons.server.entity.dragons.volitans.Volitans;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import com.leon.saintsdragons.forge.platform.NeoForgePlatformContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.LinkedHashSet;
import java.util.Set;



@Mod(SaintsDragonsCommon.MOD_ID)
public final class SaintsDragonsForge {
    private static final double ATTRIBUTE_CAP = 100000.0D;
    private static final String FORGE_ATTRIBUTES_CONFIG_FILE = SaintsDragonsConfig.DRAGON_ATTRIBUTES_CONFIG_FILE;
    private static final String FORGE_CLIENT_CONFIG_FILE = SaintsDragonsConfig.CLIENT_COMMON_CONFIG_FILE;
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SaintsDragonsCommon.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddConditionalFeaturesBiomeModifier>> ADD_CONDITIONAL_FEATURES =
            BIOME_MODIFIERS.register("add_conditional_features", () -> AddConditionalFeaturesBiomeModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddDragonsBiomeModifier>> ADD_DRAGONS =
            BIOME_MODIFIERS.register("add_dragons", () -> AddDragonsBiomeModifier.CODEC);

    public SaintsDragonsForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgePlatformContext.initialize(modEventBus, modContainer);
        ConfigStorageLayout.migrateLegacyFiles();
        raiseVanillaMaxHealthCap();
        BIOME_MODIFIERS.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON,
                ForgeDragonAttributesConfig.ATTRIBUTES_SPEC,
                FORGE_ATTRIBUTES_CONFIG_FILE);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnly.registerConfigScreen(modContainer);
        }
        modEventBus.addListener(this::onEntityAttributeCreation);
        modEventBus.addListener(this::onEntityAttributeModification);
        modEventBus.addListener(this::onBuildCreativeTabs);
        modEventBus.addListener(this::onRegisterSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(ForgeBrewingRecipes::register);
        modEventBus.addListener(this::onGatherData);
        modEventBus.addListener(this::onModConfigEvent);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        SaintsDragonsCommon.init();
    }

    private static void raiseVanillaMaxHealthCap() {
        if (!(Attributes.MAX_HEALTH.value() instanceof RangedAttribute ranged)) {
            raiseVanillaArmorCap();
            return;
        }

        RangedAttributeAccessor accessor = (RangedAttributeAccessor) ranged;
        if (accessor.saintsdragons$getMaxValue() < ATTRIBUTE_CAP) {
            accessor.saintsdragons$setMaxValue(ATTRIBUTE_CAP);
            SaintsDragonsCommon.LOGGER.info("Raised MAX_HEALTH attribute cap to {}", ATTRIBUTE_CAP);
        }
        raiseVanillaArmorCap();
    }

    private static void raiseVanillaArmorCap() {
        if (!(Attributes.ARMOR.value() instanceof RangedAttribute ranged)) {
            return;
        }
        RangedAttributeAccessor accessor = (RangedAttributeAccessor) ranged;
        if (accessor.saintsdragons$getMaxValue() >= ATTRIBUTE_CAP) {
            return;
        }
        accessor.saintsdragons$setMaxValue(ATTRIBUTE_CAP);
        SaintsDragonsCommon.LOGGER.info("Raised ARMOR attribute cap to {}", ATTRIBUTE_CAP);
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        CommonModEvents.registerEntityAttributes((type, builder) -> event.put(type, builder.build()));
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.DOUBLE_JUMP.get()));
        event.add(EntityType.PLAYER, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.FIRE_RESISTANCE.get()));
        event.add(EntityType.PLAYER, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.BLAST_RESISTANCE.get()));
    }

    private void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        CommonModEvents.registerCreativeTabEntries((tabKey, itemSupplier) -> {
            if (event.getTabKey().equals(tabKey)) {
                event.accept(itemSupplier.get());
            }
        });

        // As in 1.20.1: collect the matching entries first, then remove them from both the
        // parent tab and search results. NeoForge's getParentEntries()/getSearchEntries() are
        // read-only views, so removals must go through the event's remove(...) method.
        Set<ItemStack> hiddenEntries = new LinkedHashSet<>();
        for (ItemStack stack : event.getParentEntries()) {
            if (isHiddenVanillaPotionVariant(stack)) {
                hiddenEntries.add(stack);
            }
        }
        for (ItemStack stack : event.getSearchEntries()) {
            if (isHiddenVanillaPotionVariant(stack)) {
                hiddenEntries.add(stack);
            }
        }
        for (ItemStack stack : hiddenEntries) {
            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean isHiddenVanillaPotionVariant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
            return false;
        }

        Potion potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .potion().map(net.minecraft.core.Holder::value).orElse(null);
        return potion == ModPotions.TIDEGUARD.get() || potion == ModPotions.SEARING.get();
    }

    private void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        CommonModEvents.registerSpawnPlacements(new CommonModEvents.SpawnPlacementRegistrar() {
            @Override
            public <T extends Mob> void register(
                    EntityType<T> type,
                    SpawnPlacementType placementType,
                    Heightmap.Types heightmap,
                    SpawnPlacements.SpawnPredicate<T> predicate
            ) {
                event.register(type, placementType, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.AND);
            }
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommonModEvents.registerCommands(event.getDispatcher());
    }

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        var blockTags = new SaintsDragonBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new SaintsDragonItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(),
                new SaintsDragonEntityTypeTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new SaintsDragonBiomeTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                SaintsDragonLootTableProvider.create(output, lookupProvider));
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DragonAttributeConfigLoader.getInstance());
        event.addListener(DragonVariantReloadListener.getInstance());
        event.addListener(IvyChatterReloadListener.getInstance());
        event.addListener(IvyTradeReloadListener.getInstance());
        event.addListener(DialogueReloadListener.getInstance());
        event.addListener(DragonChestLootReloadListener.getInstance());
        event.addListener(DraconicCrucibleThermalReloadListener.getInstance());
    }

    private void onModConfigEvent(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (!SaintsDragonsCommon.MOD_ID.equals(config.getModId())) {
            return;
        }
        if (config.getType() != ModConfig.Type.COMMON) {
            return;
        }

        if (matchesConfigFile(config.getFileName(), FORGE_ATTRIBUTES_CONFIG_FILE)) {
            if (event instanceof ModConfigEvent.Loading
                    && Math.abs(ForgeDragonAttributesConfig.IGNIVORUS_FIRE_BREATH_DRAIN_PER_TICK.get() - 0.00625D) < 1.0E-10D) {
                ForgeDragonAttributesConfig.IGNIVORUS_FIRE_BREATH_DRAIN_PER_TICK.set(1.0D / 240.0D);
                ForgeDragonAttributesConfig.ATTRIBUTES_SPEC.save();
            }
            DragonAttributeConfigLoader.getInstance().refreshFromForgeConfig();
            applyAttributesToLoadedDragons();
        }
    }

    private static boolean matchesConfigFile(String actual, String expected) {
        return actual != null && actual.replace('\\', '/').equals(expected);
    }

    private void applyAttributesToLoadedDragons() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (var level : server.getAllLevels()) {
            AABB bounds = new AABB(
                    level.getWorldBorder().getMinX(),
                    level.getMinBuildHeight(),
                    level.getWorldBorder().getMinZ(),
                    level.getWorldBorder().getMaxX(),
                    level.getMaxBuildHeight(),
                    level.getWorldBorder().getMaxZ()
            );

            for (var dragon : level.getEntitiesOfClass(DragonEntity.class, bounds)) {
                if (dragon instanceof Cindervane cindervane) {
                    cindervane.applyConfiguredAttributes();
                } else if (dragon instanceof Raevyx raevyx) {
                    raevyx.applyConfiguredAttributes();
                } else if (dragon instanceof Varasuchus varasuchus) {
                    varasuchus.applyConfiguredAttributes();
                } else if (dragon instanceof Ignivorus ignivorus) {
                    ignivorus.applyConfiguredAttributes();
                } else if (dragon instanceof Volitans volitans) {
                    volitans.applyConfiguredAttributes();
                } else if (dragon instanceof Stegonaut stegonaut) {
                    stegonaut.applyConfiguredAttributes();
                } else if (dragon instanceof Nulljaw nulljaw) {
                    nulljaw.applyConfiguredAttributes();
                }
            }
        }
    }

    private static final class ClientOnly {
        private static void registerConfigScreen(ModContainer modContainer) {
            modContainer.registerConfig(ModConfig.Type.CLIENT,
                    ForgeClientConfig.CLIENT_SPEC,
                    FORGE_CLIENT_CONFIG_FILE);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (container, parent) -> new ForgeConfigRootScreen(parent));
        }
    }
}
