package com.leon.saintsdragons.forge.server.event;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.common.init.CommonServerLifecycleEvents;
import com.leon.saintsdragons.common.item.BloodTempestArmorSetBonus;
import com.leon.saintsdragons.common.item.DragonlordArmorSetBonus;
import com.leon.saintsdragons.server.debug.DragonPathDebugTracker;
import com.leon.saintsdragons.server.ai.dragonbrain.debug.DragonBrainDiagnostics;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import com.leon.saintsdragons.server.entity.npc.IvyTheDragonMerchant;
import com.leon.saintsdragons.forge.entity.part.ForgeDragonPart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SaintsDragonsCommon.MOD_ID)
public class ServerEventHandler {

    // Forge's LivingMakeBrainEvent (used to record mod-added behaviours for the brain debug view)
    // has no NeoForge 1.21 equivalent. DragonBrainOwner already registers the dragon's behaviour
    // layout with DragonBrainDiagnostics when the brain is built, which the debug view falls back to.

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        CommonServerLifecycleEvents.onEndServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CommonServerLifecycleEvents.onPlayerJoin(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel sourceLevel = player.server.getLevel(event.getFrom());
        if (sourceLevel != null) {
            IvyTheDragonMerchant.followOwnerAcrossDimension(player, sourceLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.isEndConquered()) {
            return;
        }
        ServerLevel endLevel = player.server.getLevel(Level.END);
        if (endLevel != null) {
            player.server.execute(() -> IvyTheDragonMerchant.followOwnerAcrossDimension(player, endLevel));
        }
    }

    // Forge's LivingAttackEvent became LivingIncomingDamageEvent (fired at the start of hurt()).
    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (BloodTempestArmorSetBonus.blocksDamage(player, event.getSource())
                || DragonlordArmorSetBonus.blocksDamage(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getItemStack().is(Items.DEBUG_STICK)
                || !player.canUseGameMasterBlocks()) {
            return;
        }

        DragonEntity dragon = null;
        if (event.getTarget() instanceof DragonEntity directDragon) {
            dragon = directDragon;
        } else if (event.getTarget() instanceof ForgeDragonPart part
                && part.getParent() instanceof DragonEntity parentDragon) {
            dragon = parentDragon;
        }
        if (dragon == null) {
            return;
        }

        DragonPathDebugTracker.toggle(player, dragon);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CommonServerLifecycleEvents.onPlayerDisconnect(player);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CommonServerLifecycleEvents.onServerStopping(event.getServer());
    }
}
