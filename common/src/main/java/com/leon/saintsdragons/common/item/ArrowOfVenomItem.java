package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.server.entity.effect.volitans.ArrowOfVenomEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArrowOfVenomItem extends ArrowItem {
    public ArrowOfVenomItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity shooter,
                                              @Nullable ItemStack weapon) {
        return new ArrowOfVenomEntity(level, shooter, stack.copyWithCount(1), weapon);
    }
}