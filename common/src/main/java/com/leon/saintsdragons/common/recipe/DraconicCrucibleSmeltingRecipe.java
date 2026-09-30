package com.leon.saintsdragons.common.recipe;

import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.common.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Single-ingredient crucible recipe. In 1.21 the recipe id lives on the {@code RecipeHolder}
 * rather than on the recipe itself.
 */
public record DraconicCrucibleSmeltingRecipe(
        Ingredient ingredient,
        ItemStack result,
        int requiredHeatLevel,
        int processingTime,
        int priority
) implements Recipe<DraconicCrucibleInput> {
    @Override
    public boolean matches(@NotNull DraconicCrucibleInput input, @NotNull Level level) {
        return input.size() > 0 && this.ingredient.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull DraconicCrucibleInput input, @NotNull HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRACONIC_CRUCIBLE_SMELTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.DRACONIC_CRUCIBLE_SMELTING_TYPE.get();
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(ModItems.DRACONIC_CRUCIBLE.get());
    }

    public static final class Serializer implements RecipeSerializer<DraconicCrucibleSmeltingRecipe> {
        private static final MapCodec<DraconicCrucibleSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DraconicCrucibleSmeltingRecipe::ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DraconicCrucibleSmeltingRecipe::result),
                HEAT_LEVEL_CODEC.optionalFieldOf("required_heat_level", 1).forGetter(DraconicCrucibleSmeltingRecipe::requiredHeatLevel),
                PROCESSING_TIME_CODEC.optionalFieldOf("processing_time", 200).forGetter(DraconicCrucibleSmeltingRecipe::processingTime),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(DraconicCrucibleSmeltingRecipe::priority)
        ).apply(instance, DraconicCrucibleSmeltingRecipe::new));

        // Same wire layout as the 1.20.1 toNetwork/fromNetwork pair.
        private static final StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleSmeltingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
                    buffer.writeVarInt(recipe.requiredHeatLevel);
                    buffer.writeVarInt(recipe.processingTime);
                    buffer.writeInt(recipe.priority);
                },
                buffer -> new DraconicCrucibleSmeltingRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        ItemStack.STREAM_CODEC.decode(buffer),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readInt()));

        @Override
        public @NotNull MapCodec<DraconicCrucibleSmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleSmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    static final Codec<Integer> HEAT_LEVEL_CODEC = Codec.INT.validate(level -> level < 1 || level > 3
            ? DataResult.error(() -> "required_heat_level must be between 1 and 3")
            : DataResult.success(level));

    static final Codec<Integer> PROCESSING_TIME_CODEC = Codec.INT.validate(time -> time <= 0
            ? DataResult.error(() -> "processing_time must be greater than zero")
            : DataResult.success(time));
}
