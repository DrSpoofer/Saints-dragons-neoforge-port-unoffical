package com.leon.saintsdragons.common.item.tools;

import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.item.ConfiguredItemAttributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

public final class ConfiguredWorldrootItems {
    public static final class Sword extends SwordItem {
        private static final ItemAttributeModifiers BASE_ATTRIBUTES =
                SwordItem.createAttributes(WorldrootTier.INSTANCE, 3, -2.4F);

        public Sword(Item.Properties properties) {
            super(WorldrootTier.INSTANCE, properties);
        }

        @Override
        public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
            return ConfiguredItemAttributes.weapon(BASE_ATTRIBUTES,
                    ToolsArmorConfig.WORLDROOT_SWORD_DAMAGE.get(), ToolsArmorConfig.WORLDROOT_SWORD_SPEED.get());
        }
    }

    public static final class Pickaxe extends PickaxeItem {
        private static final ItemAttributeModifiers BASE_ATTRIBUTES =
                DiggerItem.createAttributes(WorldrootTier.INSTANCE, 1, -2.8F);

        public Pickaxe(Item.Properties properties) {
            super(WorldrootTier.INSTANCE, properties);
        }

        @Override
        public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
            return ConfiguredItemAttributes.weapon(BASE_ATTRIBUTES,
                    ToolsArmorConfig.WORLDROOT_PICKAXE_DAMAGE.get(), ToolsArmorConfig.WORLDROOT_PICKAXE_SPEED.get());
        }
    }

    public static final class Axe extends AxeItem {
        private static final ItemAttributeModifiers BASE_ATTRIBUTES =
                DiggerItem.createAttributes(WorldrootTier.INSTANCE, 5.0F, -3.0F);

        public Axe(Item.Properties properties) {
            super(WorldrootTier.INSTANCE, properties);
        }

        @Override
        public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
            return ConfiguredItemAttributes.weapon(BASE_ATTRIBUTES,
                    ToolsArmorConfig.WORLDROOT_AXE_DAMAGE.get(), ToolsArmorConfig.WORLDROOT_AXE_SPEED.get());
        }
    }

    public static final class Shovel extends ShovelItem {
        private static final ItemAttributeModifiers BASE_ATTRIBUTES =
                DiggerItem.createAttributes(WorldrootTier.INSTANCE, 1.5F, -3.0F);

        public Shovel(Item.Properties properties) {
            super(WorldrootTier.INSTANCE, properties);
        }

        @Override
        public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
            return ConfiguredItemAttributes.weapon(BASE_ATTRIBUTES,
                    ToolsArmorConfig.WORLDROOT_SHOVEL_DAMAGE.get(), ToolsArmorConfig.WORLDROOT_SHOVEL_SPEED.get());
        }
    }

    public static final class Hoe extends HoeItem {
        private static final ItemAttributeModifiers BASE_ATTRIBUTES =
                DiggerItem.createAttributes(WorldrootTier.INSTANCE, -4.0F, 0.0F);

        public Hoe(Item.Properties properties) {
            super(WorldrootTier.INSTANCE, properties);
        }

        @Override
        public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
            return ConfiguredItemAttributes.weapon(BASE_ATTRIBUTES,
                    ToolsArmorConfig.WORLDROOT_HOE_DAMAGE.get(), ToolsArmorConfig.WORLDROOT_HOE_SPEED.get());
        }
    }

    private ConfiguredWorldrootItems() {
    }
}
