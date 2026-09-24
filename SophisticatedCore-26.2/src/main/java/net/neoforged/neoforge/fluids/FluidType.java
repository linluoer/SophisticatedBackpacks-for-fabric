package net.neoforged.neoforge.fluids;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;

/**
 * FluidType shim - represents fluid type properties for custom fluids.
 * Registered in NeoForgeRegistries.FLUID_TYPES.
 */
public class FluidType {
	public static final int BUCKET_VOLUME = 1000;

	private final int density;
	private final int temperature;
	private final int viscosity;
	private final int lightLevel;
	private final Rarity rarity;
	private String descriptionId;

	public FluidType(Properties properties) {
		this.density = properties.density;
		this.temperature = properties.temperature;
		this.viscosity = properties.viscosity;
		this.lightLevel = properties.lightLevel;
		this.rarity = properties.rarity;
		this.descriptionId = properties.descriptionId;
	}

	public int getDensity() {
		return density;
	}

	public int getTemperature() {
		return temperature;
	}

	public int getViscosity() {
		return viscosity;
	}

	public int getLightLevel() {
		return lightLevel;
	}

	public Rarity getRarity() {
		return rarity;
	}

	/**
	 * 是否比空气轻（密度小于 0）。在 NeoForge 中，密度小于 0 的流体会向上漂浮。
	 */
	public boolean isLighterThanAir() {
		return density < 0;
	}

	public String getDescriptionId(FluidStack stack) {
		return getDescriptionId();
	}

	public String getDescriptionId() {
		return descriptionId != null ? descriptionId : "fluid_type.neoforge.empty";
	}

	public Component getDescription() {
		return Component.translatable(getDescriptionId());
	}

	public void setDescriptionId(String descriptionId) {
		this.descriptionId = descriptionId;
	}

	public static class Properties {
		private int density = 1000;
		private int temperature = 300;
		private int viscosity = 1000;
		private int lightLevel = 0;
		private Rarity rarity = Rarity.COMMON;
		private String descriptionId;

		private Properties() {
		}

		public static Properties create() {
			return new Properties();
		}

		public Properties density(int density) {
			this.density = density;
			return this;
		}

		public Properties temperature(int temperature) {
			this.temperature = temperature;
			return this;
		}

		public Properties viscosity(int viscosity) {
			this.viscosity = viscosity;
			return this;
		}

		public Properties lightLevel(int lightLevel) {
			this.lightLevel = lightLevel;
			return this;
		}

		public Properties rarity(Rarity rarity) {
			this.rarity = rarity;
			return this;
		}

		public Properties descriptionId(String descriptionId) {
			this.descriptionId = descriptionId;
			return this;
		}
	}
}
