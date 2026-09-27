package net.neoforged.neoforge.client.event;

import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for the NeoForge {@code InputEvent} hierarchy.
 * <p>
 * This stub provides the base class plus the
 * {@link InteractionKeyMappingTriggered} sub-event used by listeners that
 * need to intercept client input events. The Fabric port bridges to its own
 * input handling hooks via mixins.
 */
public abstract class InputEvent extends Event {

	/**
	 * Fired when an interaction key mapping is triggered (attack, use item, or
	 * pick block). The shim exposes the query methods used by listeners to
	 * determine which action was triggered.
	 * <p>
	 * 字段通过构造器初始化，由 {@code MixinMinecraftPickBlock} 等桥接代码在触发事件时填充。
	 */
	public static class InteractionKeyMappingTriggered extends InputEvent {
		private final boolean attack;
		private final boolean useItem;
		private final boolean pickBlock;

		/**
		 * 无参构造器，所有标志位默认为 false。保留用于向后兼容。
		 */
		public InteractionKeyMappingTriggered() {
			this(false, false, false);
		}

		/**
		 * 创建一个携带具体触发标志的事件实例。
		 *
		 * @param attack    是否为攻击键触发
		 * @param useItem   是否为使用物品键触发
		 * @param pickBlock 是否为拾取方块键触发
		 */
		public InteractionKeyMappingTriggered(boolean attack, boolean useItem, boolean pickBlock) {
			this.attack = attack;
			this.useItem = useItem;
			this.pickBlock = pickBlock;
		}

		/**
		 * Return whether the triggered action is an attack.
		 *
		 * @return {@code true} if the attack key was triggered
		 */
		public boolean isAttack() {
			return attack;
		}

		/**
		 * Return whether the triggered action is a use-item action.
		 *
		 * @return {@code true} if the use-item key was triggered
		 */
		public boolean isUseItem() {
			return useItem;
		}

		/**
		 * Return whether the triggered action is a pick-block action.
		 *
		 * @return {@code true} if the pick-block key was triggered
		 */
		public boolean isPickBlock() {
			return pickBlock;
		}
	}
}
