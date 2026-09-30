package com.leon.saintsdragons.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import com.leon.saintsdragons.client.renderer.armor.DraconianArmorTextures;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class DraconianArmorItem extends ArmorItem {
    public DraconianArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    // NeoForge's armor texture hook also feeds render replacements such as Epic Fight.
    // The material has a single undyed layer, matching the 1.20.1 "type == null" base pass.
    @Override
    @Nullable
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer, boolean innerModel) {
        return DraconianArmorTextures.texture(slot == EquipmentSlot.LEGS);
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.saintsdragons.draconian_armor.tooltip.passive.title")
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.empty()
                .append(Component.translatable("item.saintsdragons.draconian_armor.tooltip.full_set")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(" "))
                .append(Component.translatable("item.saintsdragons.draconian_armor.tooltip.passive.description")
                        .withStyle(ChatFormatting.GRAY)));
    }
}
