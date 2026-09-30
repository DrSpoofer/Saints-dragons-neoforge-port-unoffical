package com.leon.saintsdragons.common.recipe;

import com.leon.saintsdragons.common.registry.ModItems;
import com.leon.saintsdragons.common.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class DraconicCrucibleShapedRecipe implements Recipe<DraconicCrucibleInput> {
    public static final int GRID_SIZE = 9;

    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int requiredHeatLevel;
    private final int processingTime;
    private final int priority;
    // Source pattern/key, kept so the recipe can be re-encoded by its codec (absent after network sync).
    private final List<String> pattern;
    private final Map<String, Ingredient> key;

    public DraconicCrucibleShapedRecipe(int width, int height,
                                        NonNullList<Ingredient> ingredients,
                                        ItemStack result, int requiredHeatLevel, int processingTime,
                                        int priority) {
        this(width, height, ingredients, result, requiredHeatLevel, processingTime, priority, List.of(), Map.of());
    }

    private DraconicCrucibleShapedRecipe(int width, int height,
                                         NonNullList<Ingredient> ingredients,
                                         ItemStack result, int requiredHeatLevel, int processingTime,
                                         int priority, List<String> pattern, Map<String, Ingredient> key) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.requiredHeatLevel = requiredHeatLevel;
        this.processingTime = processingTime;
        this.priority = priority;
        this.pattern = pattern;
        this.key = key;
    }

    @Override
    public boolean matches(@NotNull DraconicCrucibleInput input, @NotNull Level level) {
        if (input.size() < GRID_SIZE) {
            return false;
        }
        return findMatch(input.container()) != null;
    }

    public boolean consumeInputs(Container container) {
        Match match = findMatch(container);
        if (match == null) {
            return false;
        }
        for (int row = 0; row < this.height; row++) {
            for (int column = 0; column < this.width; column++) {
                int ingredientColumn = match.mirrored ? this.width - column - 1 : column;
                if (!this.ingredients.get(ingredientColumn + row * this.width).isEmpty()) {
                    int slot = match.offsetX + column + (match.offsetY + row) * 3;
                    container.removeItem(slot, 1);
                }
            }
        }
        return true;
    }

    @Nullable
    private Match findMatch(Container container) {
        for (int offsetY = 0; offsetY <= 3 - this.height; offsetY++) {
            for (int offsetX = 0; offsetX <= 3 - this.width; offsetX++) {
                if (matchesAt(container, offsetX, offsetY, false)) {
                    return new Match(offsetX, offsetY, false);
                }
                if (matchesAt(container, offsetX, offsetY, true)) {
                    return new Match(offsetX, offsetY, true);
                }
            }
        }
        return null;
    }

    private boolean matchesAt(Container container, int offsetX, int offsetY, boolean mirrored) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int recipeX = column - offsetX;
                int recipeY = row - offsetY;
                Ingredient expected = Ingredient.EMPTY;
                if (recipeX >= 0 && recipeX < this.width && recipeY >= 0 && recipeY < this.height) {
                    int ingredientX = mirrored ? this.width - recipeX - 1 : recipeX;
                    expected = this.ingredients.get(ingredientX + recipeY * this.width);
                }

                ItemStack actual = container.getItem(column + row * 3);
                if (expected.isEmpty() ? !actual.isEmpty() : !expected.test(actual)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int requiredHeatLevel() {
        return this.requiredHeatLevel;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public ItemStack result() {
        return this.result.copy();
    }

    public int processingTime() {
        return this.processingTime;
    }

    public int priority() {
        return this.priority;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull DraconicCrucibleInput input, @NotNull HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRACONIC_CRUCIBLE_SHAPED_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.DRACONIC_CRUCIBLE_SHAPED_TYPE.get();
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(ModItems.DRACONIC_CRUCIBLE.get());
    }

    public static final class Serializer implements RecipeSerializer<DraconicCrucibleShapedRecipe> {
        private static final MapCodec<DraconicCrucibleShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("pattern").forGetter(recipe -> recipe.pattern),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(recipe -> recipe.key),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                DraconicCrucibleSmeltingRecipe.HEAT_LEVEL_CODEC.optionalFieldOf("required_heat_level", 1)
                        .forGetter(DraconicCrucibleShapedRecipe::requiredHeatLevel),
                DraconicCrucibleSmeltingRecipe.PROCESSING_TIME_CODEC.optionalFieldOf("processing_time", 200)
                        .forGetter(DraconicCrucibleShapedRecipe::processingTime),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(DraconicCrucibleShapedRecipe::priority)
        ).apply(instance, Serializer::fromPattern));

        // Same wire layout as the 1.20.1 toNetwork/fromNetwork pair.
        private static final StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleShapedRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public @NotNull MapCodec<DraconicCrucibleShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DraconicCrucibleShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DraconicCrucibleShapedRecipe fromPattern(List<String> patternRows,
                                                                Map<String, Ingredient> keyJson,
                                                                ItemStack result,
                                                                int requiredHeatLevel,
                                                                int processingTime,
                                                                int priority) {
            String[] pattern = readPattern(patternRows);
            Map<Character, Ingredient> key = readKey(keyJson);
            int height = pattern.length;
            int width = pattern[0].length();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            int occupiedSlots = 0;
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    char symbol = pattern[row].charAt(column);
                    if (symbol != ' ') {
                        Ingredient ingredient = key.get(symbol);
                        if (ingredient == null) {
                            throw new IllegalArgumentException("Pattern references undefined symbol '" + symbol + "'");
                        }
                        ingredients.set(column + row * width, ingredient);
                        occupiedSlots++;
                    }
                }
            }
            if (occupiedSlots == 0) {
                throw new IllegalArgumentException("Draconic Crucible patterns must contain at least one ingredient");
            }
            return new DraconicCrucibleShapedRecipe(width, height, ingredients, result, requiredHeatLevel,
                    processingTime, priority, List.copyOf(patternRows), Map.copyOf(keyJson));
        }

        private static DraconicCrucibleShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int slot = 0; slot < ingredients.size(); slot++) {
                ingredients.set(slot, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int requiredHeatLevel = buffer.readVarInt();
            int processingTime = buffer.readVarInt();
            int priority = buffer.readInt();
            return new DraconicCrucibleShapedRecipe(
                    width, height, ingredients, result, requiredHeatLevel, processingTime, priority);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, DraconicCrucibleShapedRecipe recipe) {
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.requiredHeatLevel);
            buffer.writeVarInt(recipe.processingTime);
            buffer.writeInt(recipe.priority);
        }

        private static String[] readPattern(List<String> patternJson) {
            if (patternJson.isEmpty() || patternJson.size() > 3) {
                throw new IllegalArgumentException("Draconic Crucible patterns must contain between 1 and 3 rows");
            }
            String[] pattern = new String[patternJson.size()];
            int width = -1;
            for (int row = 0; row < pattern.length; row++) {
                pattern[row] = patternJson.get(row);
                if (pattern[row].isEmpty() || pattern[row].length() > 3) {
                    throw new IllegalArgumentException("Each Draconic Crucible pattern row must contain between 1 and 3 characters");
                }
                if (width == -1) {
                    width = pattern[row].length();
                } else if (pattern[row].length() != width) {
                    throw new IllegalArgumentException("All Draconic Crucible pattern rows must have the same width");
                }
            }
            return pattern;
        }

        private static Map<Character, Ingredient> readKey(Map<String, Ingredient> keyJson) {
            Map<Character, Ingredient> key = new java.util.HashMap<>();
            for (Map.Entry<String, Ingredient> entry : keyJson.entrySet()) {
                if (entry.getKey().length() != 1 || entry.getKey().charAt(0) == ' ') {
                    throw new IllegalArgumentException("Recipe key symbols must be one non-space character");
                }
                key.put(entry.getKey().charAt(0), entry.getValue());
            }
            return key;
        }
    }

    private record Match(int offsetX, int offsetY, boolean mirrored) {
    }
}
