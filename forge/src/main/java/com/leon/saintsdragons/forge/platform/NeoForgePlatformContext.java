package com.leon.saintsdragons.forge.platform;

import java.util.Objects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

/** Loader-owned objects supplied before the shared initialization resolves services. */
public final class NeoForgePlatformContext {
    private static IEventBus modEventBus;
    private static ModContainer modContainer;

    private NeoForgePlatformContext() {}

    public static void initialize(IEventBus eventBus, ModContainer container) {
        modEventBus = Objects.requireNonNull(eventBus);
        modContainer = Objects.requireNonNull(container);
    }

    public static IEventBus modEventBus() {
        return Objects.requireNonNull(modEventBus, "Saint's Dragons mod event bus is not initialized");
    }

    public static ModContainer modContainer() {
        return Objects.requireNonNull(modContainer, "Saint's Dragons mod container is not initialized");
    }
}
