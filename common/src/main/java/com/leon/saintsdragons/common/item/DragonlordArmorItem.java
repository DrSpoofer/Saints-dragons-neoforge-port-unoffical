package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.registry.ModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

public class DragonlordArmorItem extends ArmorItem implements GeoItem {
    public static final String FLIGHT_CONTROLLER = "dragonlord_flight";
    public static final String FLAP_TRIGGER = "flap";
    private static final RawAnimation GLIDE =
            RawAnimation.begin().thenLoop("animation.dragonlord_armor.glide");
    private static final RawAnimation FLAP =
            RawAnimation.begin().thenPlay("animation.dragonlord_armor.flap");
    private static final ResourceLocation HELMET_MAX_HEALTH_ID = SaintsDragonsCommon.rl("dragonlord_max_health_helmet");
    private static final ResourceLocation CHESTPLATE_MAX_HEALTH_ID = SaintsDragonsCommon.rl("dragonlord_max_health_chestplate");
    private static final ResourceLocation LEGGINGS_MAX_HEALTH_ID = SaintsDragonsCommon.rl("dragonlord_max_health_leggings");
    private static final ResourceLocation BOOTS_MAX_HEALTH_ID = SaintsDragonsCommon.rl("dragonlord_max_health_boots");
    private static final ResourceLocation HELMET_FIRE_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_fire_resistance_helmet");
    private static final ResourceLocation CHESTPLATE_FIRE_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_fire_resistance_chestplate");
    private static final ResourceLocation LEGGINGS_FIRE_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_fire_resistance_leggings");
    private static final ResourceLocation BOOTS_FIRE_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_fire_resistance_boots");
    private static final ResourceLocation HELMET_BLAST_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_blast_resistance_helmet");
    private static final ResourceLocation CHESTPLATE_BLAST_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_blast_resistance_chestplate");
    private static final ResourceLocation LEGGINGS_BLAST_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_blast_resistance_leggings");
    private static final ResourceLocation BOOTS_BLAST_RESISTANCE_ID = SaintsDragonsCommon.rl("dragonlord_blast_resistance_boots");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DragonlordArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(this.getType().getSlot());
        return ConfiguredItemAttributes.armor(
                        super.getDefaultAttributeModifiers(stack),
                        configuredDefense(),
                        ToolsArmorConfig.DRAGONLORD_TOUGHNESS.get(),
                        configuredKnockbackResistance()
                )
                .withModifierAdded(
                        Attributes.MAX_HEALTH,
                        new AttributeModifier(
                                maxHealthId(),
                                maxHealthMultiplier(),
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        slot
                )
                .withModifierAdded(
                        BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.FIRE_RESISTANCE.get()),
                        new AttributeModifier(
                                fireResistanceId(),
                                ToolsArmorConfig.DRAGONLORD_FIRE_RESISTANCE.get(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .withModifierAdded(
                        BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ModAttributes.BLAST_RESISTANCE.get()),
                        new AttributeModifier(
                                blastResistanceId(),
                                ToolsArmorConfig.DRAGONLORD_BLAST_RESISTANCE.get(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                );
    }

    private double configuredKnockbackResistance() {
        return switch (this.getType()) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_KNOCKBACK_RESISTANCE.get();
            case CHESTPLATE -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_KNOCKBACK_RESISTANCE.get();
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_KNOCKBACK_RESISTANCE.get();
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_KNOCKBACK_RESISTANCE.get();
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    private ResourceLocation maxHealthId() {
        return switch (this.getType()) {
            case HELMET -> HELMET_MAX_HEALTH_ID;
            case CHESTPLATE -> CHESTPLATE_MAX_HEALTH_ID;
            case LEGGINGS -> LEGGINGS_MAX_HEALTH_ID;
            case BOOTS -> BOOTS_MAX_HEALTH_ID;
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    private double maxHealthMultiplier() {
        return switch (this.getType()) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_MAX_HEALTH_BONUS.get() / 100.0D;
            case CHESTPLATE -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_MAX_HEALTH_BONUS.get() / 100.0D;
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_MAX_HEALTH_BONUS.get() / 100.0D;
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_MAX_HEALTH_BONUS.get() / 100.0D;
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    @Override
    public int getDefense() {
        return (int) Math.round(configuredDefense());
    }

    @Override
    public float getToughness() {
        return (float) ToolsArmorConfig.DRAGONLORD_TOUGHNESS.get();
    }

    private double configuredDefense() {
        return switch (this.getType()) {
            case HELMET -> ToolsArmorConfig.DRAGONLORD_HELMET_ARMOR.get();
            case CHESTPLATE -> ToolsArmorConfig.DRAGONLORD_CHESTPLATE_ARMOR.get();
            case LEGGINGS -> ToolsArmorConfig.DRAGONLORD_LEGGINGS_ARMOR.get();
            case BOOTS -> ToolsArmorConfig.DRAGONLORD_BOOTS_ARMOR.get();
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.double_jump.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.double_jump.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.dragonfall.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.dragonfall.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.title")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.enter")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.saintsdragons.dragonlord_armor.tooltip.aerial_dominion.boost")
                .withStyle(ChatFormatting.GRAY));
    }

    private ResourceLocation fireResistanceId() {
        return switch (this.getType()) {
            case HELMET -> HELMET_FIRE_RESISTANCE_ID;
            case CHESTPLATE -> CHESTPLATE_FIRE_RESISTANCE_ID;
            case LEGGINGS -> LEGGINGS_FIRE_RESISTANCE_ID;
            case BOOTS -> BOOTS_FIRE_RESISTANCE_ID;
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    private ResourceLocation blastResistanceId() {
        return switch (this.getType()) {
            case HELMET -> HELMET_BLAST_RESISTANCE_ID;
            case CHESTPLATE -> CHESTPLATE_BLAST_RESISTANCE_ID;
            case LEGGINGS -> LEGGINGS_BLAST_RESISTANCE_ID;
            case BOOTS -> BOOTS_BLAST_RESISTANCE_ID;
            // BODY (animal armor, 1.20.5+) is never used by these items.
            case BODY -> throw new IllegalStateException("No body armor piece for " + this);
        };
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DragonlordArmorItem> flightController = new AnimationController<>(
                this,
                FLIGHT_CONTROLLER,
                3,
                state -> {
                    Entity wearer = state.getData(DataTickets.ENTITY);
                    if (!(wearer instanceof LivingEntity living)
                            || !living.isFallFlying()
                            || !DragonlordArmorSetBonus.isWearingFullSet(living)) {
                        return PlayState.STOP;
                    }

                    state.setAndContinue(GLIDE);
                    return PlayState.CONTINUE;
                }
        );
        flightController.triggerableAnim(FLAP_TRIGGER, FLAP);
        controllers.add(flightController);
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
            Class<?> providerClass = Class.forName("software.bernie.geckolib.animatable.client.GeoRenderProvider");
            return Proxy.newProxyInstance(DragonlordArmorItem.class.getClassLoader(), new Class<?>[]{providerClass},
                    (proxy, method, args) -> {
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
            Class<?> provider = Class.forName("com.leon.saintsdragons.client.renderer.armor.DragonlordArmorRenderProvider");
            return provider.getMethod("getHumanoidArmorModel", Object.class, Object.class, Object.class, Object.class)
                    .invoke(null, args[0], args[1], args[2], args[3]);
        } catch (ReflectiveOperationException ignored) {
            return args[3];
        }
    }
}
