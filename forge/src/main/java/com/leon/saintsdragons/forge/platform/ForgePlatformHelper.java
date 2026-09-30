package com.leon.saintsdragons.forge.platform;

import com.leon.saintsdragons.common.item.DraconianSwarmSpawnEggSpawner;
import com.leon.saintsdragons.common.item.tools.DragonheartSwordItem;
import com.leon.saintsdragons.platform.ConfigHelper;
import com.leon.saintsdragons.platform.NetworkHelper;
import com.leon.saintsdragons.platform.PlatformHelper;
import com.leon.saintsdragons.platform.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ForgePlatformHelper implements PlatformHelper {
    @Override
    public boolean canDragonBreakBlock(ServerLevel level,
                                      LivingEntity dragon,
                                      BlockPos pos,
                                      BlockState state) {
        return EventHooks.canEntityGrief(level, dragon)
                && state.canEntityDestroy(level, pos, dragon)
                && EventHooks.onEntityDestroyBlock(dragon, pos, state);
    }
    // Lazy initialization to avoid ServiceConfigurationError during early class loading
    private ForgeRegistryHelper registryHelper;
    private ForgeNetworkHelper networkHelper;
    private ForgeConfigHelper configHelper;

    @Override
    public RegistryHelper getRegistryHelper() {
        if (registryHelper == null) {
            registryHelper = new ForgeRegistryHelper();
        }
        return registryHelper;
    }

    @Override
    public NetworkHelper getNetworkHelper() {
        if (networkHelper == null) {
            networkHelper = new ForgeNetworkHelper();
        }
        return networkHelper;
    }

    @Override
    public ConfigHelper getConfigHelper() {
        if (configHelper == null) {
            configHelper = new ForgeConfigHelper();
        }
        return configHelper;
    }

    @Override
    public void runOnClient(Runnable runnable) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            runnable.run();
        }
    }

    @Override
    public <T> T callOnClient(Supplier<T> supplier) {
        return FMLEnvironment.dist == Dist.CLIENT ? supplier.get() : null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public String getPlatformId() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isGenericDiveLoopEnabled() {
        return ForgeClientConfig.GENERIC_DIVE_LOOP_ENABLED == null
                || ForgeClientConfig.GENERIC_DIVE_LOOP_ENABLED.get();
    }

    @Override
    public float getSwarmBattleMusicVolume() {
        if (ForgeClientConfig.SWARM_BATTLE_MUSIC_VOLUME == null) {
            return 1.0F;
        }
        return Math.max(0, Math.min(100, ForgeClientConfig.SWARM_BATTLE_MUSIC_VOLUME.get())) / 100.0F;
    }

    @Override
    public Item createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType,
                               int primaryColor,
                               int secondaryColor,
                               Item.Properties properties) {
        return new DeferredSpawnEggItem(entityType, primaryColor, secondaryColor, properties);
    }

    @Override
    public Item createDraconianSwarmSpawnEgg(Supplier<? extends EntityType<? extends Mob>> displayEntityType,
                                             int primaryColor,
                                             int secondaryColor,
                                             Item.Properties properties) {
        return new DeferredSpawnEggItem(displayEntityType, primaryColor, secondaryColor, properties) {
            @Override
            public InteractionResult useOn(UseOnContext context) {
                return DraconianSwarmSpawnEggSpawner.useOn(context);
            }
        };
    }

    @Override
    public Item createDragonheartSword(Tier tier,
                                       int attackDamageModifier,
                                       float attackSpeedModifier,
                                       double entityReach,
                                       float criticalDamageBonus,
                                       Item.Properties properties) {
        return new ForgeDragonheartSwordItem(
                tier,
                attackDamageModifier,
                attackSpeedModifier,
                entityReach,
                criticalDamageBonus,
                properties
        );
    }

    @Override
    public Item createMobBucket(Supplier<? extends EntityType<? extends Mob>> entityType,
                                Fluid fluid,
                                SoundEvent emptySound,
                                Item.Properties properties) {
        return new MobBucketItem(entityType.get(), fluid, emptySound, properties);
    }

    @Override
    public SimpleParticleType createSimpleParticle(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    private static final class ForgeDragonheartSwordItem extends DragonheartSwordItem {
        private ForgeDragonheartSwordItem(Tier tier,
                                          int attackDamageModifier,
                                          float attackSpeedModifier,
                                          double entityReach,
                                          float criticalDamageBonus,
                                          Properties properties) {
            super(tier, attackDamageModifier, attackSpeedModifier, entityReach, criticalDamageBonus, properties);
        }

        @Override
        public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
            // Forge's ENTITY_REACH became vanilla's entity interaction range in 1.20.5+.
            return super.getDefaultAttributeModifiers(stack).withModifierAdded(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(
                            ENTITY_REACH_MODIFIER_ID,
                            this.getEntityReachBonus(),
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        }
    }
    @Override
    public double getPlayerAttackReach(Player player) {
        return player.entityInteractionRange();
    }
}
