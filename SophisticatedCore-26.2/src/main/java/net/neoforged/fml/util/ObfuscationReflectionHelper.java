package net.neoforged.fml.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper for accessing fields and methods via reflection.
 * Simplified shim for Fabric port - uses standard Java reflection.
 */
public class ObfuscationReflectionHelper {
	private ObfuscationReflectionHelper() {
	}

	public static Field findField(Class<?> clazz, String fieldName) {
		try {
			Field f = clazz.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f;
		} catch (NoSuchFieldException e) {
			throw new UnableToFindFieldException(e);
		}
	}

	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			Method m = clazz.getDeclaredMethod(methodName, paramTypes);
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
			throw new UnableToFindMethodException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPrivateValue(Class<?> clazz, Object instance, String fieldName) {
		try {
			Field f = findField(clazz, fieldName);
			return (T) f.get(instance);
		} catch (IllegalAccessException e) {
			throw new UnableToAccessFieldException(e);
		}
	}

	public static void setPrivateValue(Class<?> clazz, Object instance, String fieldName, Object value) {
		try {
			Field f = findField(clazz, fieldName);
			f.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new UnableToAccessFieldException(e);
		}
	}

	public static class UnableToFindFieldException extends RuntimeException {
		public UnableToFindFieldException(Throwable cause) {
			super(cause);
		}
	}

	public static class UnableToFindMethodException extends RuntimeException {
		public UnableToFindMethodException(Throwable cause) {
			super(cause);
		}
	}

	public static class UnableToAccessFieldException extends RuntimeException {
		public UnableToAccessFieldException(Throwable cause) {
			super(cause);
		}
	}
}
