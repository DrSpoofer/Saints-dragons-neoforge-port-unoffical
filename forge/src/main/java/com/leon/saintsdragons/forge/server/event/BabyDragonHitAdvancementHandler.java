package com.leon.saintsdragons.forge.server.event;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID)
public final class BabyDragonHitAdvancementHandler {

    // Forge's LivingHurtEvent fired from actuallyHurt; NeoForge 1.21 fires LivingDamageEvent.Pre there.
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event == null || event.getEntity() == null) {
            return;
        }
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof DragonEntity dragon) || !dragon.isBaby()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        var advancement = player.server.getAdvancements()
            .get(SaintsDragonsCommon.rl("why"));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "hit_baby");
        }
    }
}
