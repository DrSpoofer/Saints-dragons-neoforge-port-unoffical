package com.leon.saintsdragons.forge.platform;

import com.leon.saintsdragons.platform.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;

public final class ForgeRegistryHelper implements RegistryHelper {
    @Override
    public <T> RegistryWrapper<T> create(ResourceKey<? extends Registry<T>> registryKey,
                                         Supplier<Registry<T>> backingRegistry,
                                         String modId) {
        DeferredRegister<T> deferredRegister = DeferredRegister.create(registryKey, modId);
        return new Wrapper<>(deferredRegister);
    }

    private static final class Wrapper<T> implements RegistryWrapper<T> {
        private final DeferredRegister<T> deferredRegister;
        private final List<DeferredHolder<T, ? extends T>> entries = new ArrayList<>();

        private Wrapper(DeferredRegister<T> deferredRegister) {
            this.deferredRegister = deferredRegister;
        }

        @Override
        public <I extends T> Supplier<I> register(String name, Supplier<I> supplier) {
            DeferredHolder<T, I> registryObject = deferredRegister.register(name, supplier);
            entries.add(registryObject);
            return registryObject::get;
        }

        @Override
        public <I extends T> Holder<T> registerHolder(String name, Supplier<I> supplier) {
            DeferredHolder<T, I> registryObject = deferredRegister.register(name, supplier);
            entries.add(registryObject);
            return registryObject;
        }

        @Override
        public void register() {
            deferredRegister.register(NeoForgePlatformContext.modEventBus());
        }
    }
}
