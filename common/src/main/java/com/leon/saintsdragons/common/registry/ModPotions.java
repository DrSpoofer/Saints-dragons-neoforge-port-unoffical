package com.leon.saintsdragons.common.registry;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.platform.RegistryHelper;
import com.leon.saintsdragons.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

import java.util.function.Supplier;
import net.minecraft.core.Holder;

public final class ModPotions {
    public static final RegistryHelper.RegistryWrapper<Potion> REGISTER =
            Services.PLATFORM.getRegistryHelper()
                    .create(Registries.POTION, () -> BuiltInRegistries.POTION, SaintsDragonsCommon.MOD_ID);

    // 1.21 potion stacks store Holder<Potion> in their potion_contents component.
    public static final Holder<Potion> TIDEGUARD_HOLDER =
            REGISTER.registerHolder("tideguard", () -> new Potion(
                    new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 60),
                    new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 20 * 60)
            ));

    public static final Holder<Potion> SEARING_HOLDER =
            REGISTER.registerHolder("searing", () -> new Potion(
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60 * 8, 2),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60 * 8)
            ));

    public static final Supplier<Potion> TIDEGUARD = TIDEGUARD_HOLDER::value;
    public static final Supplier<Potion> SEARING = SEARING_HOLDER::value;

    private ModPotions() {
    }

    public static void register() {
        REGISTER.register();
    }
}
