package com.leon.saintsdragons.common.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.platform.RegistryHelper;
import com.leon.saintsdragons.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import java.util.EnumMap;
import java.util.List;

/**
 * Saint's Dragons armor material stats. Minecraft 1.21 turned {@link ArmorMaterial}
 * into a registry-backed record and moved durability onto the item properties, so
 * each constant registers its record and exposes its durability multiplier.
 */
public enum ModArmorMaterials {
    DRACONIAN_FLESH(
            "draconian_armor",
            25,
            3,
            7,
            5,
            3,
            18,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            1.0F,
            0.0F,
            () -> Ingredient.of(ModItems.DRACONIAN_FLESH.get())
    ),
    DRAGONHEART_CHUNK(
            "dragonheart_chunk",
            45,
            4,
            9,
            7,
            4,
            18,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F,
            0.15F,
            () -> Ingredient.of(ModItems.DRAGONHEART_CHUNK.get())
    ),
    DRAGONHEART_ALLOY(
            "dragonheart_alloy",
            55,
            5,
            10,
            8,
            5,
            20,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            5.0F,
            0.0F,
            () -> Ingredient.of(ModItems.DRAGONHEART_ALLOY.get())
    );

    private final String name;
    private final int durabilityMultiplier;
    private final int bootsDefense;
    private final int chestplateDefense;
    private final int leggingsDefense;
    private final int helmetDefense;
    private final int enchantmentValue;
    private final Holder<SoundEvent> equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;
    private Holder<ArmorMaterial> holder;

    ModArmorMaterials(
            String name,
            int durabilityMultiplier,
            int bootsDefense,
            int chestplateDefense,
            int leggingsDefense,
            int helmetDefense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.bootsDefense = bootsDefense;
        this.chestplateDefense = chestplateDefense;
        this.leggingsDefense = leggingsDefense;
        this.helmetDefense = helmetDefense;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    /** Registered material holder used by {@link ArmorItem}. */
    public Holder<ArmorMaterial> holder() {
        Registration.ensureLoaded();
        return this.holder;
    }

    /** Durability for a piece, using the vanilla per-slot base values (13/15/16/11). */
    public int durability(ArmorItem.Type type) {
        return type.getDurability(this.durabilityMultiplier);
    }

    public int getDurabilityForType(ArmorItem.Type type) {
        return durability(type);
    }

    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> this.bootsDefense;
            case LEGGINGS -> this.leggingsDefense;
            case CHESTPLATE -> this.chestplateDefense;
            case HELMET -> this.helmetDefense;
            case BODY -> 0;
        };
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Holder<SoundEvent> getEquipSound() {
        return this.equipSound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return "saintsdragons:" + this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    private ArmorMaterial createMaterial() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, this.bootsDefense);
        defense.put(ArmorItem.Type.LEGGINGS, this.leggingsDefense);
        defense.put(ArmorItem.Type.CHESTPLATE, this.chestplateDefense);
        defense.put(ArmorItem.Type.HELMET, this.helmetDefense);
        return new ArmorMaterial(
                defense,
                this.enchantmentValue,
                this.equipSound,
                this.repairIngredient,
                List.of(new ArmorMaterial.Layer(SaintsDragonsCommon.rl(this.name))),
                this.toughness,
                this.knockbackResistance
        );
    }

    public static void register() {
        Registration.REGISTER.register();
    }

    private static final class Registration {
        private static final RegistryHelper.RegistryWrapper<ArmorMaterial> REGISTER =
                Services.PLATFORM.getRegistryHelper()
                        .create(Registries.ARMOR_MATERIAL, () -> BuiltInRegistries.ARMOR_MATERIAL,
                                SaintsDragonsCommon.MOD_ID);

        static {
            for (ModArmorMaterials material : values()) {
                material.holder = REGISTER.registerHolder(material.name, material::createMaterial);
            }
        }

        private static void ensureLoaded() {
        }
    }
}
