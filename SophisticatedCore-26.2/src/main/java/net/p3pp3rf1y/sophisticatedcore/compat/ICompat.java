package net.p3pp3rf1y.sophisticatedcore.compat;

import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;

public interface ICompat {
	default void init(IEventBus modBus) {
		// noop
	}
	void setup();
}
