package com.leon.saintsdragons.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;
import net.minecraft.core.Holder;

/**
 * Abstraction over registry operations that differ per platform.
 */
public interface RegistryHelper {
    <T> RegistryWrapper<T> create(ResourceKey<? extends Registry<T>> registryKey,
                                  Supplier<Registry<T>> backingRegistry,
                                  String modId);

    interface RegistryWrapper<T> {
        <I extends T> Supplier<I> register(String name, Supplier<I> supplier);

        /**
         * Registers an entry and returns a lazily bound holder. Minecraft 1.21 APIs
         * such as armor items take registry holders instead of raw values.
         */
        <I extends T> Holder<T> registerHolder(String name, Supplier<I> supplier);

        void register();
    }
}
