package com.leon.saintsdragons.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;

public class FixedPotionItem extends PotionItem {
    private final Holder<Potion> potion;

    public FixedPotionItem(Item.Properties properties, Holder<Potion> potion) {
        super(properties);
        this.potion = potion;
    }

    private ItemStack ensurePotion(ItemStack stack) {
        // 1.20.1 "no potion" (Potions.EMPTY) is an absent potion in 1.21's potion_contents component.
        if (stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().isEmpty()) {
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(this.potion));
        }
        return stack;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return ensurePotion(super.getDefaultInstance());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ensurePotion(stack);
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!level.isClientSide) {
            for (MobEffectInstance effectInstance : this.potion.value().getEffects()) {
                if (effectInstance.getEffect().value().isInstantenous()) {
                    effectInstance.getEffect().value().applyInstantenousEffect(
                            null,
                            null,
                            livingEntity,
                            effectInstance.getAmplifier(),
                            1.0D
                    );
                } else {
                    livingEntity.addEffect(new MobEffectInstance(effectInstance));
                }
            }
        }

        if (livingEntity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else {
            stack.shrink(1);
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        PotionContents.addPotionTooltip(this.potion.value().getEffects(), tooltipComponents::add, 1.0F, context.tickRate());
    }
}
