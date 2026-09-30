package com.leon.saintsdragons.forge.client.event;

import com.leon.saintsdragons.client.input.BloodTempestDodgeInput;
import com.leon.saintsdragons.client.input.DragonRideInputHandler;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

/**
 * Forge wiring that exposes the shared dragon ride keybinds to the event system.
 */
@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID, value = Dist.CLIENT)
public final class DragonRideKeybinds {
    private DragonRideKeybinds() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        normalizeFlexModifier();
        BloodTempestDodgeInput.clientTick();
        DragonRideInputHandler.clientTick();
    }

    private static void normalizeFlexModifier() {
        KeyMapping flex = DragonRideInputHandler.DRAGON_FLEX;
        if (flex.getKeyModifier() == KeyModifier.NONE) {
            return;
        }

        flex.setKeyModifierAndCode(KeyModifier.NONE, flex.getKey());
        Minecraft.getInstance().options.save();
    }

    public static KeyMapping[] getKeyMappings() {
        return new KeyMapping[]{
                DragonRideInputHandler.DRAGON_ASCEND,
                DragonRideInputHandler.DRAGON_DESCEND,
                DragonRideInputHandler.DRAGON_ACCELERATE,
                DragonRideInputHandler.DRAGON_TERTIARY_ABILITY,
                DragonRideInputHandler.DRAGON_PRIMARY_ABILITY,
                DragonRideInputHandler.DRAGON_SECONDARY_ABILITY,
                DragonRideInputHandler.DRAGON_TOGGLE_MELEE,
                DragonRideInputHandler.DRAGON_FLEX
        };
    }
}

