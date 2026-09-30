package com.leon.saintsdragons.server.entity.npc.trade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.ItemCost;

public final class IvyTradeRegistry {
    private static volatile List<OfferSource> datapackSources = List.of();
    private static volatile long revision;

    private IvyTradeRegistry() {
    }

    public static void fillOffers(AbstractVillager trader, RandomSource random, MerchantOffers offers) {
        for (OfferSource source : datapackSources) {
            source.addOffers(trader, random, offers);
        }
    }

    static void replaceDatapackTrades(List<OfferSource> sources) {
        datapackSources = List.copyOf(sources);
        revision++;
    }

    public static long currentRevision() {
        return revision;
    }

    static List<OfferSource> parseOfferSources(ResourceLocation fileId, JsonObject root) {
        List<OfferSource> result = new ArrayList<>();
        if (root.has("trades")) {
            JsonArray trades = GsonHelper.getAsJsonArray(root, "trades");
            for (int i = 0; i < trades.size(); i++) {
                JsonObject trade = GsonHelper.convertToJsonObject(trades.get(i), fileId + " trade " + i);
                result.add(new FixedTrade(parseTrade(fileId, trade)));
            }
        }
        if (root.has("pools")) {
            JsonArray pools = GsonHelper.getAsJsonArray(root, "pools");
            for (int i = 0; i < pools.size(); i++) {
                JsonObject pool = GsonHelper.convertToJsonObject(pools.get(i), fileId + " pool " + i);
                result.add(parsePool(fileId, pool));
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException(fileId + " contains no trades or pools");
        }
        return result;
    }

    private static TradePool parsePool(ResourceLocation fileId, JsonObject pool) {
        int rolls = GsonHelper.getAsInt(pool, "rolls", 1);
        if (rolls < 1) {
            throw new IllegalArgumentException(fileId + " has a trade pool with fewer than one roll");
        }

        JsonArray entriesJson = GsonHelper.getAsJsonArray(pool, "entries");
        List<WeightedTrade> entries = new ArrayList<>();
        for (int i = 0; i < entriesJson.size(); i++) {
            JsonObject entry = GsonHelper.convertToJsonObject(entriesJson.get(i), fileId + " pool entry " + i);
            int weight = GsonHelper.getAsInt(entry, "weight", 1);
            if (weight < 1) {
                throw new IllegalArgumentException(fileId + " has a trade pool entry with non-positive weight");
            }
            JsonObject trade = entry.has("trade")
                    ? GsonHelper.getAsJsonObject(entry, "trade")
                    : entry;
            entries.add(new WeightedTrade(parseTrade(fileId, trade), weight));
        }
        if (entries.isEmpty()) {
            throw new IllegalArgumentException(fileId + " has an empty trade pool");
        }
        if (rolls > entries.size()) {
            throw new IllegalArgumentException(fileId + " requests more trade pool rolls than it has entries");
        }
        return new TradePool(rolls, entries);
    }

    private static VillagerTrades.ItemListing parseTrade(ResourceLocation fileId, JsonObject trade) {
        StackFactory costA = parseStack(GsonHelper.getAsJsonObject(trade, "cost_a"), fileId);
        StackFactory costB = trade.has("cost_b")
                ? parseStack(GsonHelper.getAsJsonObject(trade, "cost_b"), fileId)
                : StackFactory.EMPTY;
        ResultFactory result = trade.has("results")
                ? parseResultPool(GsonHelper.getAsJsonArray(trade, "results"), fileId)
                : parseStack(GsonHelper.getAsJsonObject(trade, "result"), fileId);
        int maxUses = GsonHelper.getAsInt(trade, "max_uses", 5);
        int xp = GsonHelper.getAsInt(trade, "xp", 0);
        float priceMultiplier = GsonHelper.getAsFloat(trade, "price_multiplier", 0.05F);
        return (trader, random) -> {
            HolderLookup.Provider registries = trader.registryAccess();
            ItemStack secondCost = costB.create(random, registries);
            if (secondCost.isEmpty()) {
                return new MerchantOffer(toCost(costA.create(random, registries)), result.create(random, registries),
                        maxUses, xp, priceMultiplier);
            }
            return new MerchantOffer(
                    toCost(costA.create(random, registries)),
                    Optional.of(toCost(secondCost)),
                    result.create(random, registries),
                    maxUses,
                    xp,
                    priceMultiplier);
        };
    }

    /**
     * 1.21 merchant costs are {@link ItemCost}s. 1.20.1 accepted any stack of the cost item whose NBT
     * contained the cost stack's NBT; requiring the cost stack's explicit components keeps that rule.
     */
    private static ItemCost toCost(ItemStack stack) {
        DataComponentPredicate.Builder predicate = DataComponentPredicate.builder();
        for (var entry : stack.getComponentsPatch().entrySet()) {
            entry.getValue().ifPresent(value -> expect(predicate, entry.getKey(), value));
        }
        return new ItemCost(stack.getItemHolder(), stack.getCount(), predicate.build());
    }

    @SuppressWarnings("unchecked")
    private static <T> void expect(DataComponentPredicate.Builder predicate, DataComponentType<T> type, Object value) {
        predicate.expect(type, (T) value);
    }

    private static ResultPool parseResultPool(JsonArray array, ResourceLocation fileId) {
        List<WeightedResult> entries = new ArrayList<>();
        int totalWeight = 0;
        for (int i = 0; i < array.size(); i++) {
            JsonObject entry = GsonHelper.convertToJsonObject(array.get(i), fileId + " result " + i);
            int weight = GsonHelper.getAsInt(entry, "weight", 1);
            if (weight < 1) {
                throw new IllegalArgumentException(fileId + " has a result with non-positive weight");
            }
            totalWeight += weight;
            entries.add(new WeightedResult(parseStack(entry, fileId), weight));
        }
        if (entries.isEmpty()) {
            throw new IllegalArgumentException(fileId + " has an empty results pool");
        }
        return new ResultPool(entries, totalWeight);
    }

    private static StackFactory parseStack(JsonObject object, ResourceLocation fileId) {
        ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(object, "item"));
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
        if (item.isEmpty()) {
            throw new IllegalArgumentException("Unknown item " + itemId + " in " + fileId);
        }
        CountRange count = parseCount(object);
        List<EnchantmentEntry> enchantments = parseEnchantments(object, fileId);
        return (random, registries) -> {
            ItemStack stack = new ItemStack(item.get(), count.roll(random));
            for (EnchantmentEntry enchantment : enchantments) {
                if (random.nextFloat() <= enchantment.chance()) {
                    // Enchantments are a data-driven registry in 1.21, resolved from the trader's world.
                    registries.lookupOrThrow(Registries.ENCHANTMENT).get(enchantment.enchantment()).ifPresentOrElse(
                            holder -> stack.enchant(holder, enchantment.level()),
                            () -> SaintsDragonsCommon.LOGGER.warn("Unknown enchantment {} in Ivy trade data",
                                    enchantment.enchantment().location()));
                }
            }
            return stack;
        };
    }

    private static CountRange parseCount(JsonObject object) {
        if (!object.has("count")) {
            return new CountRange(1, 1);
        }
        JsonElement element = object.get("count");
        if (element.isJsonObject()) {
            JsonObject count = element.getAsJsonObject();
            int min = GsonHelper.getAsInt(count, "min", 1);
            int max = GsonHelper.getAsInt(count, "max", min);
            return new CountRange(min, max);
        }
        int count = GsonHelper.convertToInt(element, "count");
        return new CountRange(count, count);
    }

    private static List<EnchantmentEntry> parseEnchantments(JsonObject object, ResourceLocation fileId) {
        if (!object.has("enchantments")) {
            return List.of();
        }
        List<EnchantmentEntry> result = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(object, "enchantments");
        for (JsonElement element : array) {
            JsonObject enchantmentJson = GsonHelper.convertToJsonObject(element, fileId + " enchantment");
            ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(enchantmentJson, "id"));
            ResourceKey<Enchantment> enchantment = ResourceKey.create(Registries.ENCHANTMENT, id);
            int level = GsonHelper.getAsInt(enchantmentJson, "level", 1);
            float chance = GsonHelper.getAsFloat(enchantmentJson, "chance", 1.0F);
            result.add(new EnchantmentEntry(enchantment, level, chance));
        }
        return List.copyOf(result);
    }

    interface OfferSource {
        void addOffers(AbstractVillager trader, RandomSource random, MerchantOffers offers);
    }

    private record FixedTrade(VillagerTrades.ItemListing listing) implements OfferSource {
        @Override
        public void addOffers(AbstractVillager trader, RandomSource random, MerchantOffers offers) {
            addOffer(listing, trader, random, offers);
        }
    }

    private record TradePool(int rolls, List<WeightedTrade> entries) implements OfferSource {
        private TradePool {
            entries = List.copyOf(entries);
        }

        @Override
        public void addOffers(AbstractVillager trader, RandomSource random, MerchantOffers offers) {
            List<WeightedTrade> available = new ArrayList<>(entries);
            for (int rollIndex = 0; rollIndex < rolls; rollIndex++) {
                int totalWeight = available.stream().mapToInt(WeightedTrade::weight).sum();
                int roll = random.nextInt(totalWeight);
                int selectedIndex = 0;
                for (; selectedIndex < available.size(); selectedIndex++) {
                    roll -= available.get(selectedIndex).weight();
                    if (roll < 0) {
                        break;
                    }
                }
                WeightedTrade selected = available.remove(Math.min(selectedIndex, available.size() - 1));
                addOffer(selected.listing(), trader, random, offers);
            }
        }
    }

    private static void addOffer(VillagerTrades.ItemListing listing,
                                 AbstractVillager trader,
                                 RandomSource random,
                                 MerchantOffers offers) {
        MerchantOffer offer = listing.getOffer(trader, random);
        if (offer != null) {
            offers.add(offer);
        }
    }

    private interface ResultFactory {
        ItemStack create(RandomSource random, HolderLookup.Provider registries);
    }

    private interface StackFactory extends ResultFactory {
        StackFactory EMPTY = (random, registries) -> ItemStack.EMPTY;
    }

    private record CountRange(int min, int max) {
        private CountRange {
            if (min < 0 || max < min) {
                throw new IllegalArgumentException("Invalid count range " + min + ".." + max);
            }
        }

        private int roll(RandomSource random) {
            if (min == max) {
                return min;
            }
            return min + random.nextInt(max - min + 1);
        }
    }

    private record EnchantmentEntry(ResourceKey<Enchantment> enchantment, int level, float chance) {
    }

    private record WeightedTrade(VillagerTrades.ItemListing listing, int weight) {
    }

    private record WeightedResult(StackFactory factory, int weight) {
    }

    private record ResultPool(List<WeightedResult> entries, int totalWeight) implements ResultFactory {
        @Override
        public ItemStack create(RandomSource random, HolderLookup.Provider registries) {
            int roll = random.nextInt(totalWeight);
            for (WeightedResult entry : entries) {
                roll -= entry.weight();
                if (roll < 0) {
                    return entry.factory().create(random, registries);
                }
            }
            return entries.get(entries.size() - 1).factory().create(random, registries);
        }
    }
}
