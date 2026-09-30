package com.leon.saintsdragons.common.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Replaces an item's vanilla combat/armor modifiers with values read from the tools/armor config.
 * The original modifier ids (vanilla's base attack/armor ids) are reused so the replacement merges
 * with vanilla tooltips and stacking exactly as the untouched item would.
 */
public final class ConfiguredItemAttributes {
    public static ItemAttributeModifiers weapon(
            ItemAttributeModifiers base,
            double attackDamage,
            double attackSpeed
    ) {
        ResourceLocation damageId = modifierId(base, Attributes.ATTACK_DAMAGE, "attack damage");
        ResourceLocation speedId = modifierId(base, Attributes.ATTACK_SPEED, "attack speed");
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            if (!entry.attribute().is(Attributes.ATTACK_DAMAGE) && !entry.attribute().is(Attributes.ATTACK_SPEED)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                damageId,
                attackDamage - 1.0D,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                speedId,
                attackSpeed - 4.0D,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);
        return builder.build().withTooltip(base.showInTooltip());
    }

    public static ItemAttributeModifiers armor(
            ItemAttributeModifiers base,
            double armor,
            double toughness,
            double knockbackResistance
    ) {
        ItemAttributeModifiers.Entry slotEntry = base.modifiers().stream()
                .filter(entry -> entry.attribute().is(Attributes.ARMOR))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Armor item is missing its slot modifier"));
        ResourceLocation slotId = slotEntry.modifier().id();
        EquipmentSlotGroup slot = slotEntry.slot();

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            if (!entry.attribute().is(Attributes.ARMOR)
                    && !entry.attribute().is(Attributes.ARMOR_TOUGHNESS)
                    && !entry.attribute().is(Attributes.KNOCKBACK_RESISTANCE)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        builder.add(Attributes.ARMOR, new AttributeModifier(
                slotId, armor, AttributeModifier.Operation.ADD_VALUE), slot);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                slotId, toughness, AttributeModifier.Operation.ADD_VALUE), slot);
        if (knockbackResistance != 0.0D) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    slotId, knockbackResistance, AttributeModifier.Operation.ADD_VALUE), slot);
        }
        return builder.build().withTooltip(base.showInTooltip());
    }

    private static ResourceLocation modifierId(ItemAttributeModifiers base,
                                               Holder<Attribute> attribute,
                                               String description) {
        return base.modifiers().stream()
                .filter(entry -> entry.attribute().is(attribute))
                .findFirst()
                .map(entry -> entry.modifier().id())
                .orElseThrow(() -> new IllegalStateException("Weapon is missing its " + description + " modifier"));
    }

    private ConfiguredItemAttributes() {
    }
}
