package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;

/**
 * IConfigScreenFactory shim - functional interface for creating mod configuration screens.
 * <p>
 * On NeoForge this is registered as an extension point on the mod container so that
 * the "Config" button in the mod list opens the mod's config screen. On Fabric this is
 * a stub since Fabric has no built-in mod-list config button.
 * <p>
 * The functional method is {@link #create}, whose signature matches the
 * {@code ConfigurationScreen(ModContainer, Screen)} constructor so that
 * {@code ConfigurationScreen::new} can be used directly as a factory.
 */
@FunctionalInterface
public interface IConfigScreenFactory {
	/**
	 * Creates the configuration screen for the mod.
	 *
	 * @param container the mod container
	 * @param parent the parent screen to return to
	 * @return the config screen
	 */
	Screen create(ModContainer container, Screen parent);
}
