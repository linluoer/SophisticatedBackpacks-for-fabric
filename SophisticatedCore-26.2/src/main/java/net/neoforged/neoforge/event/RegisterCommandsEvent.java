package net.neoforged.neoforge.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * 命令注册事件 - 在服务器命令dispatcher 初始化时触发。
 * <p>
 * 兼容性 shim 用于 Fabric 移植。
 */
public class RegisterCommandsEvent extends Event {
	private final CommandDispatcher<CommandSourceStack> dispatcher;
	private final CommandBuildContext buildContext;

	public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher) {
		this.dispatcher = dispatcher;
		this.buildContext = null;
	}

	public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		this.dispatcher = dispatcher;
		this.buildContext = buildContext;
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return dispatcher;
	}

	/**
	 * 获取命令构建上下文。在 Fabric 简化实现中可能返回 null。
	 */
	public CommandBuildContext getBuildContext() {
		return buildContext;
	}
}
