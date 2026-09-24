package net.neoforged.fml;

import java.util.function.Supplier;

/**
 * Shim for neoforge InterModComms.
 * Fabric doesn't have IMC, so all methods are no-op.
 */
public class InterModComms {
	public static void sendTo(String modId, String method, Supplier<?> supplier) {
		// noop - fabric doesn't have IMC
	}
}
