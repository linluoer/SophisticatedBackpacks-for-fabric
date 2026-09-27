package net.neoforged.neoforge.client.event;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.p3pp3rf1y.sophisticatedcore.eventbus.Event;

/**
 * Compatibility shim for {@code RegisterParticleProvidersEvent}.
 * <p>
 * 调用 {@link #registerSpriteSet} / {@link #registerSpecial} 时直接通过 Fabric 的
 * {@link ParticleProviderRegistry} 完成真实注册。事件由 Fabric 客户端初始化器
 * （{@code FabricSophisticatedCoreClient}）在 {@code onInitializeClient} 中 post。
 * <p>
 * 与 NeoForge 原版行为一致：监听器在事件被 post 时执行 register 调用，
 * 注册请求立即生效。Fabric 的 {@link ParticleProviderRegistry} 接受
 * {@code SpriteAwareFactory<T>}（{@code (SpriteSet) -> ParticleProvider<T>}），
 * 与本 shim 的 {@link SpriteParticleRegistration} 函数签名相同。
 */
public class RegisterParticleProvidersEvent extends Event {

	/**
	 * Register a sprite-based particle provider factory for the given particle type.
	 * <p>
	 * 实际注册通过 {@link ParticleProviderRegistry#register(ParticleType, ParticleProviderRegistry.SpriteAwareFactory)}
	 * 完成，Fabric 会在客户端粒子系统初始化时调用 factory 创建 provider。
	 *
	 * @param <T>       the particle options type
	 * @param type      the particle type to register the provider for
	 * @param provider  functional factory that produces a {@link ParticleProvider} from a {@link SpriteSet}
	 */
	public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> provider) {
		ParticleProviderRegistry.getInstance().register(type, provider::create);
	}

	/**
	 * Register a special (non-sprite) particle provider for the given particle type.
	 *
	 * @param <T>       the particle options type
	 * @param type      the particle type to register the provider for
	 * @param provider  the provider to use
	 */
	public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
		ParticleProviderRegistry.getInstance().register(type, provider);
	}

	/**
	 * Functional interface mirroring vanilla's
	 * {@code ParticleProvider.SpriteParticleRegistration} for sprite-set based
	 * particle provider factories.
	 */
	@FunctionalInterface
	public interface SpriteParticleRegistration<T extends ParticleOptions> {
		ParticleProvider<T> create(SpriteSet spriteSet);
	}
}
