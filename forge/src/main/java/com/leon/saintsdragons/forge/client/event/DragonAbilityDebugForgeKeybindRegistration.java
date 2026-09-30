package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public final class DragonAbilityDebugForgeKeybindRegistration {
    private DragonAbilityDebugForgeKeybindRegistration() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(DragonAbilityDebugForgeHandler.TOGGLE_ABILITY_DEBUG);
    }
}
