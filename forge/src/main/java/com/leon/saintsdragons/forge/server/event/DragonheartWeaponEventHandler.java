package com.leon.saintsdragons.forge.server.event;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.item.tools.DragonheartSwordItem;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID)
public final class DragonheartWeaponEventHandler {
    private DragonheartWeaponEventHandler() {
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!event.isVanillaCritical()
                || !(event.getEntity().getMainHandItem().getItem() instanceof DragonheartSwordItem sword)) {
            return;
        }

        event.setDamageMultiplier(event.getDamageMultiplier() + sword.getCriticalDamageBonus());
    }
}
