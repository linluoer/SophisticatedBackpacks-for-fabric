package net.neoforged.neoforge.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Configuration specification builder, similar to ForgeConfigSpec.
 * Simplified shim for Fabric port - values are stored in-memory and
 * get() returns the default value (or last set value).
 */
public class ModConfigSpec {
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private final Map<String, ConfigValue<?>> values = new ConcurrentHashMap<>();
	private volatile boolean loaded;
	private volatile Path configPath;

	public ModConfigSpec() {
	}

	public boolean isLoaded() {
		return loaded;
	}

	public synchronized void save() {
		Path path = configPath;
		if (path == null) {
			return;
		}
		try {
			writeJsonAtomically(path, createJson());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to save config " + path, e);
		}
	}

	public void load() {
		loaded = true;
	}

	public synchronized void load(Path path) {
		configPath = path.toAbsolutePath().normalize();
		boolean save = false;
		try {
			if (Files.exists(configPath)) {
				try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
					JsonElement root = JsonParser.parseReader(reader);
					if (root.isJsonObject()) {
						save = loadValues(root.getAsJsonObject());
					} else {
						throw new IllegalArgumentException("Config root must be a JSON object");
					}
				}
			} else {
				resetValues();
				save = true;
			}
		} catch (IOException | RuntimeException e) {
			resetValues();
			loaded = false;
			throw new IllegalStateException("Failed to load config " + configPath, e);
		}
		if (save) {
			save();
		}
		loaded = true;
	}

	public void setConfig(Object commentedConfig) {
		loaded = true;
	}

	public Path getConfigPath() {
		return configPath;
	}

	private boolean loadValues(JsonObject root) {
		boolean corrected = false;
		for (ConfigValue<?> value : values.values()) {
			JsonElement jsonValue = getJsonValue(root, value.getPath());
			if (jsonValue == null) {
				value.reset();
				corrected = true;
				continue;
			}
			if (!value.load(jsonValue)) {
				corrected = true;
			}
		}
		return corrected;
	}

	private void resetValues() {
		values.values().forEach(ConfigValue::reset);
	}

	private JsonObject createJson() {
		JsonObject root = new JsonObject();
		values.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> setJsonValue(root, entry.getKey(), GSON.toJsonTree(entry.getValue().get())));
		return root;
	}

	private static JsonElement getJsonValue(JsonObject root, String path) {
		JsonObject current = root;
		String[] parts = path.split("\\.");
		for (int i = 0; i < parts.length - 1; i++) {
			JsonElement child = current.get(parts[i]);
			if (child == null || !child.isJsonObject()) {
				return null;
			}
			current = child.getAsJsonObject();
		}
		return current.get(parts[parts.length - 1]);
	}

	private static void setJsonValue(JsonObject root, String path, JsonElement value) {
		JsonObject current = root;
		String[] parts = path.split("\\.");
		for (int i = 0; i < parts.length - 1; i++) {
			JsonElement child = current.get(parts[i]);
			if (child == null || !child.isJsonObject()) {
				JsonObject next = new JsonObject();
				current.add(parts[i], next);
				current = next;
			} else {
				current = child.getAsJsonObject();
			}
		}
		current.add(parts[parts.length - 1], value);
	}

	private static void writeJsonAtomically(Path path, JsonObject root) throws IOException {
		Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Path temporary = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
		try {
			try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				GSON.toJson(root, writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static Object readValue(JsonElement json, Object defaultValue) {
		if (defaultValue instanceof List<?> list) {
			if (!json.isJsonArray()) {
				return null;
			}
			Class<?> elementType = list.isEmpty() || list.get(0) == null ? String.class : list.get(0).getClass();
			List<Object> result = new ArrayList<>();
			JsonArray array = json.getAsJsonArray();
			for (JsonElement element : array) {
				result.add(readValue(element, defaultValueForType(elementType)));
			}
			return result;
		}
		if (json.isJsonNull()) {
			return null;
		}
		if (defaultValue instanceof String) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString() ? json.getAsString() : null;
		}
		if (defaultValue instanceof Boolean) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean() ? json.getAsBoolean() : null;
		}
		if (defaultValue instanceof Integer) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber() ? json.getAsBigDecimal().intValueExact() : null;
		}
		if (defaultValue instanceof Long) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber() ? json.getAsBigDecimal().longValueExact() : null;
		}
		if (defaultValue instanceof Double) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber() ? json.getAsDouble() : null;
		}
		if (defaultValue instanceof Float) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber() ? json.getAsFloat() : null;
		}
		if (defaultValue instanceof Enum<?>) {
			return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()
					? Enum.valueOf((Class) defaultValue.getClass(), json.getAsString()) : null;
		}
		return GSON.fromJson(json, defaultValue.getClass());
	}

	private static Object defaultValueForType(Class<?> type) {
		if (type == String.class) {
			return "";
		}
		if (type == Boolean.class) {
			return false;
		}
		if (type == Integer.class) {
			return 0;
		}
		if (type == Long.class) {
			return 0L;
		}
		if (type == Double.class) {
			return 0D;
		}
		if (type == Float.class) {
			return 0F;
		}
		return null;
	}

	private static Predicate<Object> rangeValidator(int min, int max) {
		return value -> value instanceof Integer integer && integer >= min && integer <= max;
	}

	private static Predicate<Object> rangeValidator(long min, long max) {
		return value -> value instanceof Long number && number >= min && number <= max;
	}

	private static Predicate<Object> rangeValidator(double min, double max) {
		return value -> value instanceof Number number && Double.isFinite(number.doubleValue()) && number.doubleValue() >= min && number.doubleValue() <= max;
	}

	/**
	 * Builder for constructing a ModConfigSpec.
	 */
	public static class Builder {
		private final ModConfigSpec spec = new ModConfigSpec();
		private final List<String> currentPath = new ArrayList<>();
		private final List<String> comment = new ArrayList<>();

		public Builder comment(String comment) {
			this.comment.add(comment);
			return this;
		}

		public Builder comment(String... comments) {
			for (String c : comments) {
				this.comment.add(c);
			}
			return this;
		}

		public Builder push(String path) {
			currentPath.add(path);
			return this;
		}

		public Builder pop() {
			if (!currentPath.isEmpty()) {
				currentPath.remove(currentPath.size() - 1);
			}
			return this;
		}

		public Builder worldRestart() {
			return this;
		}

		public Builder translation(String translationKey) {
			return this;
		}


		private String fullPath(String key) {
			if (currentPath.isEmpty()) {
				return key;
			}
			return String.join(".", currentPath) + "." + key;
		}

		private void clearComment() {
			comment.clear();
		}

		public <T> ConfigValue<T> define(String path, T defaultValue) {
			return define(path, defaultValue, value -> value != null && defaultValue.getClass().isInstance(value));
		}

		public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
			ConfigValue<T> value = new ConfigValue<>(spec, fullPath(path), defaultValue, validator);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public BooleanValue define(String path, boolean defaultValue) {
			BooleanValue value = new BooleanValue(spec, fullPath(path), defaultValue);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public IntValue define(String path, int defaultValue) {
			IntValue value = new IntValue(spec, fullPath(path), defaultValue);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public DoubleValue define(String path, double defaultValue) {
			DoubleValue value = new DoubleValue(spec, fullPath(path), defaultValue);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public LongValue define(String path, long defaultValue) {
			LongValue value = new LongValue(spec, fullPath(path), defaultValue);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public IntValue defineInRange(String path, int defaultValue, int min, int max) {
			IntValue value = new IntValue(spec, fullPath(path), defaultValue, rangeValidator(min, max));
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
			DoubleValue value = new DoubleValue(spec, fullPath(path), defaultValue, rangeValidator(min, max));
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public LongValue defineInRange(String path, long defaultValue, long min, long max) {
			LongValue value = new LongValue(spec, fullPath(path), defaultValue, rangeValidator(min, max));
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public <T> ConfigValue<T> defineInList(String path, T defaultValue, List<? extends T> allowedValues) {
			return define(path, defaultValue, allowedValues::contains);
		}

		public <T extends Enum<T>> EnumValue<T> defineEnum(String path, T defaultValue) {
			EnumValue<T> value = new EnumValue<>(spec, fullPath(path), defaultValue);
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
			List<? extends T> defaultValue = defaultSupplier.get();
			ConfigValue<List<? extends T>> value = new ConfigValue<>(spec, fullPath(path), defaultValue,
					list -> list instanceof List<?> values && values.stream().allMatch(elementValidator));
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		/**
		 * 重载：直接接受 List 作为默认值，而不是 Supplier。
		 * NeoForge 原版 API 同时支持 List 和 Supplier 两种形式。
		 */
		public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
			return defineList(path, () -> defaultValue, elementValidator);
		}

		public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier,
				Supplier<T> elementDefault, Predicate<Object> elementValidator) {
			return defineList(defaultSupplier, elementValidator, path);
		}

		public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier,
				Predicate<Object> elementValidator) {
			return defineList(defaultSupplier, elementValidator, path);
		}

		private <T> ConfigValue<List<? extends T>> defineList(Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator, String path) {
			List<? extends T> defaultValue = defaultSupplier.get();
			ConfigValue<List<? extends T>> value = new ConfigValue<>(spec, fullPath(path), defaultValue,
					list -> list instanceof List<?> values && values.stream().allMatch(elementValidator));
			spec.values.put(fullPath(path), value);
			clearComment();
			return value;
		}

		public ModConfigSpec build() {
			return spec;
		}

		public <T> Pair<T, ModConfigSpec> configure(Function<Builder, T> consumer) {
			T obj = consumer.apply(this);
			return Pair.of(obj, build());
		}
	}

	public static class ConfigValue<T> implements Supplier<T> {
		private final ModConfigSpec spec;
		private final String path;
		private final T defaultValue;
		private final Predicate<Object> validator;
		private volatile T value;

		ConfigValue(ModConfigSpec spec, String path, T defaultValue, Predicate<Object> validator) {
			this.spec = spec;
			this.path = path;
			this.defaultValue = defaultValue;
			this.validator = validator;
			this.value = defaultValue;
		}

		public T get() {
			return value;
		}

		public void set(T value) {
			this.value = validator.test(value) ? value : defaultValue;
		}

		private boolean load(JsonElement json) {
			try {
				Object loadedValue = readValue(json, defaultValue);
				if (!validator.test(loadedValue)) {
					reset();
					return false;
				}
				value = (T) loadedValue;
				return true;
			} catch (RuntimeException e) {
				reset();
				return false;
			}
		}

		private void reset() {
			value = defaultValue;
		}

		public T getDefault() {
			return defaultValue;
		}

		public String getPath() {
			return path;
		}

		public ModConfigSpec getSpec() {
			return spec;
		}
	}

	public static class IntValue extends ConfigValue<Integer> implements java.util.function.IntSupplier {
		IntValue(ModConfigSpec spec, String path, Integer defaultValue) {
			this(spec, path, defaultValue, value -> value instanceof Integer);
		}

		IntValue(ModConfigSpec spec, String path, Integer defaultValue, Predicate<Object> validator) {
			super(spec, path, defaultValue, validator);
		}

		@Override
		public int getAsInt() {
			return get();
		}
	}

	public static class DoubleValue extends ConfigValue<Double> {
		DoubleValue(ModConfigSpec spec, String path, Double defaultValue) {
			this(spec, path, defaultValue, value -> value instanceof Double);
		}

		DoubleValue(ModConfigSpec spec, String path, Double defaultValue, Predicate<Object> validator) {
			super(spec, path, defaultValue, validator);
		}
	}

	public static class LongValue extends ConfigValue<Long> {
		LongValue(ModConfigSpec spec, String path, Long defaultValue) {
			this(spec, path, defaultValue, value -> value instanceof Long);
		}

		LongValue(ModConfigSpec spec, String path, Long defaultValue, Predicate<Object> validator) {
			super(spec, path, defaultValue, validator);
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean> implements java.util.function.BooleanSupplier {
		BooleanValue(ModConfigSpec spec, String path, Boolean defaultValue) {
			super(spec, path, defaultValue, value -> value instanceof Boolean);
		}

		@Override
		public boolean getAsBoolean() {
			return get();
		}
	}

	public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
		EnumValue(ModConfigSpec spec, String path, T defaultValue) {
			super(spec, path, defaultValue, value -> value != null && defaultValue.getDeclaringClass().isInstance(value));
		}
	}
}
