package com.leon.saintsdragons.forge.platform;

import com.leon.saintsdragons.platform.ConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

public final class ForgeConfigHelper implements ConfigHelper {
    @Override
    public ConfigBuilder commonBuilder(String fileName) {
        return new ForgeBuilder(fileName);
    }

    private static final class ForgeBuilder implements ConfigBuilder {
        private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        private final String fileName;

        private ForgeBuilder(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void push(String category) {
            builder.push(category);
        }

        @Override
        public void pop() {
            builder.pop();
        }

        @Override
        public void comment(String comment) {
            builder.comment(comment);
        }

        @Override
        public IntValue defineInt(String key, int defaultValue, int min, int max) {
            ModConfigSpec.IntValue value = builder.defineInRange(key, defaultValue, min, max);
            return new ForgeIntValue(value);
        }

        @Override
        public DoubleValue defineDouble(String key, double defaultValue, double min, double max) {
            ModConfigSpec.DoubleValue value = builder.defineInRange(key, defaultValue, min, max);
            return new ForgeDoubleValue(value);
        }

        @Override
        public BooleanValue defineBoolean(String key, boolean defaultValue) {
            ModConfigSpec.BooleanValue value = builder.define(key, defaultValue);
            return new ForgeBooleanValue(value);
        }

        @Override
        public ListValue defineList(String key, List<String> defaultValue) {
            ModConfigSpec.ConfigValue<List<? extends String>> value =
                    builder.defineList(key, defaultValue, obj -> obj instanceof String);
            return new ForgeListValue(value);
        }

        @Override
        public void build() {
            ModConfigSpec spec = builder.build();
            NeoForgePlatformContext.modContainer().registerConfig(ModConfig.Type.COMMON, spec, fileName);
        }
    }

    private static final class ForgeIntValue implements IntValue {
        private final ModConfigSpec.IntValue value;

        private ForgeIntValue(ModConfigSpec.IntValue value) {
            this.value = value;
        }

        @Override
        public int get() {
            return value.get();
        }

        @Override
        public void set(int newValue) {
            value.set(newValue);
        }

        @Override
        public void save() {
            value.save();
        }
    }

    private static final class ForgeBooleanValue implements BooleanValue {
        private final ModConfigSpec.BooleanValue value;

        private ForgeBooleanValue(ModConfigSpec.BooleanValue value) {
            this.value = value;
        }

        @Override
        public boolean get() {
            return value.get();
        }

        @Override
        public void set(boolean newValue) {
            value.set(newValue);
        }

        @Override
        public void save() {
            value.save();
        }
    }

    private static final class ForgeDoubleValue implements DoubleValue {
        private final ModConfigSpec.DoubleValue value;

        private ForgeDoubleValue(ModConfigSpec.DoubleValue value) {
            this.value = value;
        }

        @Override
        public double get() {
            return value.get();
        }

        @Override
        public void set(double newValue) {
            value.set(newValue);
        }

        @Override
        public void save() {
            value.save();
        }
    }

    private static final class ForgeListValue implements ListValue {
        private final ModConfigSpec.ConfigValue<List<? extends String>> value;

        private ForgeListValue(ModConfigSpec.ConfigValue<List<? extends String>> value) {
            this.value = value;
        }

        @Override
        public List<String> get() {
            return new ArrayList<>(value.get());
        }

        @Override
        public void set(List<String> newValue) {
            value.set(newValue);
        }

        @Override
        public void save() {
            value.save();
        }
    }
}
