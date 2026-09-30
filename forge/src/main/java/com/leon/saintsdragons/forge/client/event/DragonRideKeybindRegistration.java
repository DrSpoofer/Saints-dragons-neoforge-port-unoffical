package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.input.DragonRideInputHandler;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public final class DragonRideKeybindRegistration {
    private DragonRideKeybindRegistration() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        DragonRideInputHandler.registerKeys(event::register, false);
    }
}
