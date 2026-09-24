package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 注册客户端 Tooltip 组件工厂事件 shim。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。{@link #register} 方法将工厂存储到静态映射中，
 * 供 {@code MixinClientTooltipComponent} 在 {@code ClientTooltipComponent.create()} 中查找。
 */
public class RegisterClientTooltipComponentFactoriesEvent extends Event {

	private static final Map<Class<?>, Function<TooltipComponent, ClientTooltipComponent>> FACTORIES = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends TooltipComponent> void register(Class<T> componentClass, Function<? super T, ? extends ClientTooltipComponent> factory) {
		FACTORIES.put(componentClass, (Function<TooltipComponent, ClientTooltipComponent>) factory);
	}

	/**
	 * 查找并创建自定义 tooltip 组件。
	 *
	 * @param component 服务端 tooltip 组件
	 * @return 对应的客户端 tooltip 组件，如果未注册则返回 null
	 */
	@Nullable
	public static ClientTooltipComponent createCustom(TooltipComponent component) {
		Function<TooltipComponent, ClientTooltipComponent> factory = FACTORIES.get(component.getClass());
		return factory != null ? factory.apply(component) : null;
	}
}
