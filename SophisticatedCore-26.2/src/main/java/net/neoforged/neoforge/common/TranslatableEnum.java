package net.neoforged.neoforge.common;

import net.minecraft.network.chat.Component;

/**
 * TranslatableEnum shim - interface implemented by enums that wish to provide
 * a translated display name for config screens and similar UIs.
 * <p>
 * Mods implement this on their enum constants and override
 * {@link #getTranslatedName()} to return a translatable {@link Component}.
 */
public interface TranslatableEnum {
	/**
	 * Returns the translated display name of this enum constant.
	 *
	 * @return a translatable component representing this enum value
	 */
	Component getTranslatedName();

	/**
	 * Returns the translation key for this enum constant, derived from the
	 * enum name by default. Subclasses may override for custom keys.
	 */
	default String getTranslationKey() {
		return ((Enum<?>) this).name().toLowerCase(java.util.Locale.ROOT);
	}
}
