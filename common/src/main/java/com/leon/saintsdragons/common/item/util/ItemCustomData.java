package com.leon.saintsdragons.common.item.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Minecraft 1.20.5 replaced free-form ItemStack NBT with data components. Saint's Dragons keeps
 * its per-stack state (binder captures, plushie bindings, baby flags) in {@code minecraft:custom_data},
 * which is also where vanilla's data fixer moves unrecognised 1.20.1 item NBT, so stacks from older
 * worlds keep their stored data after upgrading.
 */
public final class ItemCustomData {
    private ItemCustomData() {
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Returns a copy of the stack's custom tag, or {@code null} when it has none (1.20.1 {@code getTag()}).
     * Mutating the returned tag does not change the stack; use {@link #update} or {@link #set}.
     */
    @Nullable
    public static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    /** Replaces the custom tag; a null or empty tag removes it (1.20.1 {@code setTag}). */
    public static void set(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /** Edits the custom tag in place, creating it if needed and removing it if left empty. */
    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
