package net.p3pp3rf1y.sophisticatedcore.util;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Helper for registering custom argument types by class.
 * Vanilla {@link ArgumentTypeInfos} keeps the BY_CLASS map private and only updates it
 * inside its private {@code register} method which also registers in the registry.
 * NeoForge exposes {@code registerByClass} that only updates the BY_CLASS map,
 * letting the caller handle the registry registration separately (e.g. via DeferredRegister).
 *
 * This shim mimics that behavior using reflection on the private BY_CLASS map.
 */
public final class ArgumentTypeInfosHelper {
	private ArgumentTypeInfosHelper() {
	}

	private static volatile Map<Class<?>, ArgumentTypeInfo<?, ?>> byClassCache;

	@SuppressWarnings("unchecked")
	private static Map<Class<?>, ArgumentTypeInfo<?, ?>> getByClassMap() {
		if (byClassCache == null) {
			synchronized (ArgumentTypeInfosHelper.class) {
				if (byClassCache == null) {
					try {
						Field field = ArgumentTypeInfos.class.getDeclaredField("BY_CLASS");
						field.setAccessible(true);
						byClassCache = (Map<Class<?>, ArgumentTypeInfo<?, ?>>) field.get(null);
					} catch (NoSuchFieldException | IllegalAccessException e) {
						throw new IllegalStateException("Failed to access ArgumentTypeInfos.BY_CLASS map", e);
					}
				}
			}
		}
		return byClassCache;
	}

	/**
	 * Registers the association between an argument type class and its info in the
	 * vanilla BY_CLASS map, without registering in any registry.
	 * Returns the info unchanged so it can be used inline.
	 */
	public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> I registerByClass(
			Class<? extends A> brigadierType, I info) {
		getByClassMap().put(brigadierType, info);
		return info;
	}
}
