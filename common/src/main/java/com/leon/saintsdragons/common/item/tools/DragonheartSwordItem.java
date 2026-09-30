package com.leon.saintsdragons.common.item.tools;

import com.leon.saintsdragons.common.config.ToolsArmorConfig;
import com.leon.saintsdragons.common.item.ConfiguredItemAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

public class DragonheartSwordItem extends SwordItem {
    private static final int DRAGONLORD_FIRE_ASPECT_SECONDS = 8;
    // 1.21 attribute modifiers are keyed by id rather than UUID.
    public static final ResourceLocation ENTITY_REACH_MODIFIER_ID = SaintsDragonsCommon.rl("dragonheart_entity_reach");
    public static final ResourceLocation TARGETING_REACH_MODIFIER_ID = SaintsDragonsCommon.rl("dragonheart_targeting_reach");
    public static final double VANILLA_ENTITY_REACH = 3.0D;
    public static final double VANILLA_BLOCK_REACH = 4.5D;

    private final double entityReach;
    private final float criticalDamageBonus;
    private final Tier tier;
    private final ItemAttributeModifiers baseAttributes;

    public DragonheartSwordItem(Tier tier,
                                int attackDamageModifier,
                                float attackSpeedModifier,
                                double entityReach,
                                float criticalDamageBonus,
                                Properties properties) {
        super(tier, properties);
        this.baseAttributes = SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier);
        this.entityReach = entityReach;
        this.criticalDamageBonus = criticalDamageBonus;
        this.tier = tier;
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return ConfiguredItemAttributes.weapon(this.baseAttributes,
                isBloodTempest() ? bloodTempestDamage() : dragonlordDamage(),
                isBloodTempest() ? bloodTempestSpeed() : dragonlordSpeed());
    }

    public double getEntityReach() {
        if (ToolsArmorConfig.BLOOD_TEMPEST_KATANA_REACH == null || ToolsArmorConfig.DRAGONLORD_SWORD_REACH == null) {
            return this.entityReach;
        }
        return isBloodTempest() ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_REACH.get() : ToolsArmorConfig.DRAGONLORD_SWORD_REACH.get();
    }

    public double getEntityReachBonus() {
        return getEntityReach() - VANILLA_ENTITY_REACH;
    }

    public double getTargetingReachBonus() {
        return getEntityReach() - VANILLA_BLOCK_REACH;
    }

    public float getCriticalDamageBonus() {
        if (ToolsArmorConfig.BLOOD_TEMPEST_KATANA_CRITICAL_BONUS == null
                || ToolsArmorConfig.DRAGONLORD_SWORD_CRITICAL_BONUS == null) {
            return this.criticalDamageBonus;
        }
        return (float) (isBloodTempest()
                ? ToolsArmorConfig.BLOOD_TEMPEST_KATANA_CRITICAL_BONUS.get()
                : ToolsArmorConfig.DRAGONLORD_SWORD_CRITICAL_BONUS.get());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hurt = super.hurtEnemy(stack, target, attacker);
        if (hurt && !isBloodTempest()) {
            target.igniteForSeconds(DRAGONLORD_FIRE_ASPECT_SECONDS);
        }
        return hurt;
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!isBloodTempest()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.ability.title")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.ability.description")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.saintsdragons.dragonlord_sword.tooltip.description")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.passive.title")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.passive.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.ability.title")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.ability.description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.blood_tempest_katana.tooltip.quote")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

    private boolean isBloodTempest() {
        return this.tier == DragonheartWeaponTier.CHUNK;
    }

    private double bloodTempestDamage() {
        return ToolsArmorConfig.BLOOD_TEMPEST_KATANA_DAMAGE == null
                ? 1.0D + this.tier.getAttackDamageBonus() + 3.0D
                : ToolsArmorConfig.BLOOD_TEMPEST_KATANA_DAMAGE.get();
    }

    private double bloodTempestSpeed() {
        return ToolsArmorConfig.BLOOD_TEMPEST_KATANA_SPEED == null
                ? 3.0D
                : ToolsArmorConfig.BLOOD_TEMPEST_KATANA_SPEED.get();
    }

    private double dragonlordDamage() {
        return ToolsArmorConfig.DRAGONLORD_SWORD_DAMAGE == null
                ? 1.0D + this.tier.getAttackDamageBonus() + 5.0D
                : ToolsArmorConfig.DRAGONLORD_SWORD_DAMAGE.get();
    }

    private double dragonlordSpeed() {
        return ToolsArmorConfig.DRAGONLORD_SWORD_SPEED == null
                ? 1.4D
                : ToolsArmorConfig.DRAGONLORD_SWORD_SPEED.get();
    }
}
