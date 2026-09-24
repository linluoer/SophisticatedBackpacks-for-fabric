package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;

/**
 * ConfigurationScreen shim - a stub config screen.
 * <p>
 * On NeoForge this screen lists the mod's config files and allows editing them.
 * On Fabric there is no equivalent framework config UI, so this stub just renders
 * an empty screen. The constructor signature {@code (ModContainer, Screen)}
 * matches the {@link IConfigScreenFactory} functional interface so that
 * {@code ConfigurationScreen::new} can be used as a method reference.
 */
public class ConfigurationScreen extends Screen {
	private final Screen parent;

	public ConfigurationScreen(ModContainer container, Screen parent) {
		super(Component.translatable("sophisticatedcore.config.title"));
		this.parent = parent;
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreenAndShow(parent);
		}
	}
}
