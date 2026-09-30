package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.core.Holder;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

public class BloodTempestArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BloodTempestArmorItem(Holder<ArmorMaterial> armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        return ConfiguredItemAttributes.armor(base, configuredDefense(),
                ToolsArmorConfig.BLOOD_TEMPEST_TOUGHNESS.get(),
                ToolsArmorConfig.BLOOD_TEMPEST_KNOCKBACK_RESISTANCE.get());
    }

    @Override
    public int getDefense() {
        return (int) Math.round(configuredDefense());
    }

    @Override
    public float getToughness() {
        return (float) ToolsArmorConfig.BLOOD_TEMPEST_TOUGHNESS.get();
    }

    private double configuredDefense() {
        return switch (getType()) {
            case HELMET -> ToolsArmorConfig.BLOOD_TEMPEST_HELMET_ARMOR.get();
            case CHESTPLATE -> ToolsArmorConfig.BLOOD_TEMPEST_CHESTPLATE_ARMOR.get();
            case LEGGINGS -> ToolsArmorConfig.BLOOD_TEMPEST_LEGGINGS_ARMOR.get();
            case BOOTS -> ToolsArmorConfig.BLOOD_TEMPEST_BOOTS_ARMOR.get();
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(Component.empty()
                .append(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.title")
                        .withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(" "))
                .append(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.full_set")
                        .withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_armor.tooltip.description")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        Object provider = createFabricRenderProvider();
        if (provider != null) {
            consumer.accept((GeoRenderProvider) provider);
        }
    }

    private Object createFabricRenderProvider() {
        try {
            Class<?> renderProviderClass = Class.forName("software.bernie.geckolib.animatable.client.GeoRenderProvider");
            return Proxy.newProxyInstance(
                    BloodTempestArmorItem.class.getClassLoader(),
                    new Class<?>[]{renderProviderClass},
                    (proxyInstance, method, args) -> {
                        if ("getGeoArmorRenderer".equals(method.getName())) {
                            return getHumanoidArmorModel(args);
                        }
                        return null;
                    });
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Object getHumanoidArmorModel(Object[] args) {
        if (args == null || args.length < 4) {
            return null;
        }
        try {
            Class<?> provider = Class.forName("com.leon.saintsdragons.client.renderer.armor.BloodTempestRenderProvider");
            return provider.getMethod("getHumanoidArmorModel", Object.class, Object.class, Object.class, Object.class)
                    .invoke(null, args[0], args[1], args[2], args[3]);
        } catch (ReflectiveOperationException ignored) {
            return args[3];
        }
    }
}
