package com.leon.saintsdragons.common.item;

import com.leon.saintsdragons.client.renderer.item.MossbackItemRenderer;
import com.leon.saintsdragons.common.registry.ModEntities;
import com.leon.saintsdragons.server.entity.dragons.Mossback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.leon.saintsdragons.common.item.util.ItemCustomData;
import net.minecraft.nbt.CompoundTag;

public class MossbackItem extends Item implements GeoItem {
    private static final String BABY_TAG = "BabyMossback";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mossback.idle");
    private static final RawAnimation BABY_IDLE = RawAnimation.begin().thenLoop("baby_mossback.animation.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MossbackItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            Mossback mossback = ModEntities.MOSSBACK.get().create(serverLevel);
            if (mossback != null) {
                mossback.setBaby(isBaby(stack));
                Vec3 look = player.getLookAngle();
                Vec3 spawn = player.getEyePosition().add(look.scale(0.65D));
                mossback.moveTo(spawn.x, spawn.y - 0.25D, spawn.z, player.getYRot(), 0.0F);
                mossback.setDeltaMovement(look.scale(1.25D).add(0.0D, 0.18D, 0.0D));
                mossback.markThrown();
                serverLevel.addFreshEntity(mossback);
                player.playSound(SoundEvents.SNOWBALL_THROW, 0.6F, 0.85F + player.getRandom().nextFloat() * 0.25F);
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 4, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            state.setAndContinue(isBaby(stack) ? BABY_IDLE : IDLE);
            return PlayState.CONTINUE;
        }));
    }

    public static boolean isBaby(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = ItemCustomData.get(stack);
        return tag != null && tag.getBoolean(BABY_TAG);
    }

    public static void setBaby(ItemStack stack, boolean baby) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (baby) {
            ItemCustomData.update(stack, tag -> tag.putBoolean(BABY_TAG, true));
        } else if (ItemCustomData.has(stack)) {
            // Removes the component entirely when no other custom data remains.
            ItemCustomData.update(stack, tag -> tag.remove(BABY_TAG));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        Object provider = createFabricRenderProvider();
        if (provider != null) {
            consumer.accept((GeoRenderProvider) provider);
        }
    }

    private Object createFabricRenderProvider() {
        try {
            Class<?> renderProviderClass = Class.forName("software.bernie.geckolib.animatable.client.GeoRenderProvider");
            return Proxy.newProxyInstance(
                    MossbackItem.class.getClassLoader(),
                    new Class<?>[]{renderProviderClass},
                    (proxyInstance, method, args) -> {
                        if ("getGeoItemRenderer".equals(method.getName())) {
                            return MossbackForgeRendererHolder.renderer();
                        }
                        return null;
                    });
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static final class MossbackForgeRendererHolder {
        private static MossbackItemRenderer renderer;

        private static MossbackItemRenderer renderer() {
            if (renderer == null) {
                renderer = new MossbackItemRenderer();
            }
            return renderer;
        }
    }
}
