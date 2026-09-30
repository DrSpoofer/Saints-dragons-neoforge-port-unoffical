package com.leon.saintsdragons.forge.server.event;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID)
public class DragonAttackEventHandler {

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        if (event == null || event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide) return;
        var player = event.getEntity();
        if (!(player.getVehicle() instanceof DragonEntity dragon)) return;
        if (dragon.isBaby()) return;
        if (dragon.areRiderControlsLocked()) return;
        if (!dragon.isTame() || !dragon.isOwnedBy(player)) return;
        var abilityType = dragon.getPrimaryAttackAbility();
        if (abilityType == null) return;

        dragon.combatManager.tryUseAbility(abilityType);
        event.setCanceled(true);
    }
}
