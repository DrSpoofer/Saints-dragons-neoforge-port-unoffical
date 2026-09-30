package com.leon.saintsdragons.server.entity.npc.dialogue;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

/**
 * JSON conversion for dialogue text components. Minecraft 1.21 component serialization is registry
 * aware; dialogue files are loaded before any world registries are bound and only use text,
 * translation and style data, so an empty registry context reproduces the 1.20.1 behaviour.
 */
final class DialogueComponentJson {
    private DialogueComponentJson() {
    }

    @Nullable
    static Component fromJson(JsonElement element) {
        return Component.Serializer.fromJson(element, RegistryAccess.EMPTY);
    }

    static JsonElement toJsonTree(Component component) {
        return ComponentSerialization.CODEC
                .encodeStart(RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE), component)
                .getOrThrow();
    }
}
