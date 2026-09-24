package net.p3pp3rf1y.sophisticatedbackpacks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.permissions.Permissions;
import net.p3pp3rf1y.sophisticatedcore.eventbus.IEventBus;
import net.p3pp3rf1y.sophisticatedcore.compat.ScNeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedcore.util.ArgumentTypeInfosHelper;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class BackpackCommand {
	private static final DeferredRegister<@NonNull ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE,
			SophisticatedBackpacks.MOD_ID);

	private static final Supplier<SingletonArgumentInfo<@NonNull BackpackUUIDArgumentType>> BACKPACK_UUID_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
			.register("backpack_uuid", () -> ArgumentTypeInfosHelper.registerByClass(BackpackUUIDArgumentType.class,
					SingletonArgumentInfo.contextFree(BackpackUUIDArgumentType::backpackUuid)));
	private static final Supplier<SingletonArgumentInfo<@NonNull BackpackPlayerArgumentType>> PLAYER_NAME_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
			.register("player_name", () -> ArgumentTypeInfosHelper.registerByClass(BackpackPlayerArgumentType.class,
					SingletonArgumentInfo.contextFree(BackpackPlayerArgumentType::playerName)));
	private static final Supplier<SingletonArgumentInfo<@NonNull BackpackTemplateArgumentType>> TEMPLATE_NAME_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
			.register("template_name", () -> ArgumentTypeInfosHelper.registerByClass(BackpackTemplateArgumentType.class,
					SingletonArgumentInfo.contextFree(BackpackTemplateArgumentType::templateName)));
	private static final Supplier<SingletonArgumentInfo<@NonNull BackpackItemArgumentType>> BACKPACK_ITEM_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register(
			"backpack_item",
			() -> ArgumentTypeInfosHelper.registerByClass(BackpackItemArgumentType.class, SingletonArgumentInfo.contextAware(BackpackItemArgumentType::item)));
	private static final Supplier<SingletonArgumentInfo<@NonNull BackpackUpgradeItemArgumentType>> BACKPACK_UPGRADE_ITEM_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
			.register("backpack_upgrade_item", () -> ArgumentTypeInfosHelper.registerByClass(BackpackUpgradeItemArgumentType.class,
					SingletonArgumentInfo.contextAware(BackpackUpgradeItemArgumentType::item)));

	private BackpackCommand() {
	}

	public static void init(IEventBus modBus) {
		COMMAND_ARGUMENT_TYPES.register(modBus);

		ScNeoForge.EVENT_BUS.addListener(BackpackCommand::registerCommands);
	}

	private static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		LiteralCommandNode<CommandSourceStack> mainNode = dispatcher
				.register(Commands.literal("sb").requires(cs -> cs.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).then(ListCommand.register())
						.then(GiveCommand.register()).then(RemoveNonPlayerCommand.register()).then(TemplateCommand.register(event)));
		dispatcher.register(
				Commands.literal("sophisticatedbackpacks").requires(cs -> cs.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).redirect(mainNode));
	}
}
