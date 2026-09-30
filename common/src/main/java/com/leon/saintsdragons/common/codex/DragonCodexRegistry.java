package com.leon.saintsdragons.common.codex;

import com.leon.saintsdragons.common.registry.ModTags;
import com.leon.saintsdragons.common.registry.ModSounds;
import com.leon.saintsdragons.server.entity.base.DragonEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DragonCodexRegistry {
    private static final Map<ResourceLocation, Definition> ENTRIES = new ConcurrentHashMap<>();

    static {
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/ignivorus.txt"),
                "saintsdragons.gui.draconic_codex.ecology.ignivorus.page1",
                ModTags.Items.IGNIVORUS_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus_tooth"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus_heart"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus_egg"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "ignivorus_wing_hide")
                ),
                new Care(true, true, true), () -> ModSounds.IGNIVORUS_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "atroxiia"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/atroxiia.txt"),
                "saintsdragons.gui.draconic_codex.ecology.atroxiia.page1",
                ModTags.Items.ATROXIIA_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "atroxiia_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "atroxiia_egg")
                ),
                new Care(true, true, true), () -> ModSounds.ATROXIIA_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "raevyx"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/raevyx.txt"),
                "saintsdragons.gui.draconic_codex.ecology.raevyx.page1",
                ModTags.Items.RAEVYX_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "raevyx_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "raevyx_egg"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "raevyx_wing_hide"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "raevyx_wingtalon")
                ),
                new Care(true, true, true), () -> ModSounds.RAEVYX_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "varasuchus"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/varasuchus.txt"),
                "saintsdragons.gui.draconic_codex.ecology.varasuchus.page1",
                ModTags.Items.VARASUCHUS_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "varasuchus_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "varasuchus_egg")
                ),
                new Care(true, true, true), () -> ModSounds.VARASUCHUS_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "cindervane"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/cindervane.txt"),
                "saintsdragons.gui.draconic_codex.ecology.cindervane.page1",
                ModTags.Items.CINDERVANE_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "cindervane_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "cindervane_egg")
                ),
                new Care(true, true, true), () -> ModSounds.CINDERVANE_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "stegonaut"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/stegonaut.txt"),
                "saintsdragons.gui.draconic_codex.ecology.stegonaut.page1",
                ModTags.Items.STEGONAUT_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "stegonaut_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "stegonaut_egg")
                ),
                new Care(true, true, true), () -> ModSounds.STEGONAUT_GRUMBLE_1.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "volitans"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/volitans.txt"),
                "saintsdragons.gui.draconic_codex.ecology.volitans.page1",
                ModTags.Items.VOLITANS_FOODS, List.of(
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "volitans_scale"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "volitans_spine"),
                    ResourceLocation.fromNamespaceAndPath("saintsdragons", "volitans_egg"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "salmon"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "cod"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "tropical_fish"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pufferfish")
                ),
                new Care(true, true, true), () -> ModSounds.VOLITANS_GRUMBLE_3.get()));
        register(ResourceLocation.fromNamespaceAndPath("saintsdragons", "nulljaw"), new Definition(
                ResourceLocation.fromNamespaceAndPath("saintsdragons", "ecology/nulljaw.txt"),
                "saintsdragons.gui.draconic_codex.ecology.nulljaw.page1",
                null, List.of(),
                new Care(true, true, false), () -> ModSounds.NULLJAW_GRUMBLE_1.get()));
    }

    private DragonCodexRegistry() {
    }

    public static void register(ResourceLocation entityTypeId, Definition definition) {
        Objects.requireNonNull(entityTypeId, "entityTypeId");
        Objects.requireNonNull(definition, "definition");
        if (ENTRIES.putIfAbsent(entityTypeId, definition) != null) {
            throw new IllegalArgumentException("Duplicate Codex species: " + entityTypeId);
        }
    }

    public static ResourceLocation speciesId(String savedType) {
        ResourceLocation id = savedType == null ? null : ResourceLocation.tryParse(
                savedType.contains(":") ? savedType : "saintsdragons:" + savedType);
        return id == null ? ResourceLocation.fromNamespaceAndPath("saintsdragons", "unknown") : id;
    }

    public static String speciesKey(DragonEntity dragon) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType());
        return id.getNamespace().equals("saintsdragons") ? id.getPath() : id.toString();
    }

    @Nullable
    public static Definition get(String savedType) {
        return ENTRIES.get(speciesId(savedType));
    }

    public record Care(boolean hunger, boolean happiness, boolean brushing) {
        public static final Care NONE = new Care(false, false, false);
    }

    public record Definition(ResourceLocation ecologyText, String ecologyTranslationKey,
                             @Nullable TagKey<Item> favoriteFoods, List<ResourceLocation> drops,
                             Care care, @Nullable Supplier<SoundEvent> selectionSound) {
        public Definition {
            Objects.requireNonNull(ecologyText, "ecologyText");
            Objects.requireNonNull(ecologyTranslationKey, "ecologyTranslationKey");
            drops = List.copyOf(drops);
            Objects.requireNonNull(care, "care");
        }
    }
}
