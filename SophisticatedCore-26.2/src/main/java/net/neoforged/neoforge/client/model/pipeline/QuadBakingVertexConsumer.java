package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * NeoForge {@code QuadBakingVertexConsumer} 的兼容实现。
 * <p>
 * 实现 {@link VertexConsumer}，按顶点累积位置/颜色/UV/UV2/法线数据，
 * 通过 {@link #bakeQuad()} 输出原版 {@link BakedQuad}。
 * <p>
 * 与 NeoForge 原版的区别：
 * <ul>
 *     <li>{@code setAmbientOcclusion} 接受调用但忽略值（原版 {@link BakedQuad.MaterialInfo}
 *         没有 {@code ambientOcclusion} 字段）。</li>
 *     <li>{@code setSprite} 需要同时提供 {@link ChunkSectionLayer} 和 {@link RenderType}，
 *         与原版 {@link BakedQuad.MaterialInfo} 字段一一对应。</li>
 * </ul>
 */
public class QuadBakingVertexConsumer implements VertexConsumer {

	@Nullable
	private TextureAtlasSprite sprite;
	@Nullable
	private ChunkSectionLayer layer;
	@Nullable
	private RenderType itemRenderType;
	@Nullable
	private Direction direction;
	private int tintIndex = -1;
	private boolean shade = false;
	private int lightEmission = 0;

	private final Vector3f[] positions = new Vector3f[4];
	private final int[] colors = new int[4];
	private final float[][] uvs = new float[4][2];
	private final int[][] uv2s = new int[4][2];
	private final Vector3f[] normals = new Vector3f[4];

	private int currentIndex = -1;
	@Nullable
	private BakedQuad bakedQuad;

	public QuadBakingVertexConsumer setSprite(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType) {
		this.sprite = sprite;
		this.layer = layer;
		this.itemRenderType = itemRenderType;
		return this;
	}

	public QuadBakingVertexConsumer setDirection(Direction direction) {
		this.direction = direction;
		return this;
	}

	public QuadBakingVertexConsumer setTintIndex(int tintIndex) {
		this.tintIndex = tintIndex;
		return this;
	}

	public QuadBakingVertexConsumer setShade(boolean shade) {
		this.shade = shade;
		return this;
	}

	public QuadBakingVertexConsumer setLightEmission(int lightEmission) {
		this.lightEmission = lightEmission;
		return this;
	}

	/**
	 * 接受 ambientOcclusion 设置但忽略值。原版 {@link BakedQuad.MaterialInfo}
	 * 没有 ambientOcclusion 字段，此处仅为兼容调用方代码。
	 */
	public QuadBakingVertexConsumer setAmbientOcclusion(boolean ambientOcclusion) {
		return this;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		currentIndex++;
		if (currentIndex >= 4) {
			throw new IllegalStateException("QuadBakingVertexConsumer 只能接收 4 个顶点，已收到第 " + (currentIndex + 1) + " 个");
		}
		positions[currentIndex] = new Vector3f(x, y, z);
		colors[currentIndex] = 0xFFFFFFFF;
		uvs[currentIndex][0] = 0f;
		uvs[currentIndex][1] = 0f;
		uv2s[currentIndex][0] = 0;
		uv2s[currentIndex][1] = 0;
		normals[currentIndex] = new Vector3f(0, 0, 0);
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		if (currentIndex < 0) {
			return this;
		}
		colors[currentIndex] = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		if (currentIndex < 0) {
			return this;
		}
		colors[currentIndex] = color;
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (currentIndex < 0) {
			return this;
		}
		uvs[currentIndex][0] = u;
		uvs[currentIndex][1] = v;
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		// overlay 不参与 baked quad，忽略
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		if (currentIndex < 0) {
			return this;
		}
		uv2s[currentIndex][0] = u;
		uv2s[currentIndex][1] = v;
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		if (currentIndex < 0) {
			return this;
		}
		normals[currentIndex].set(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		return this;
	}

	/**
	 * 累积 4 个顶点后，构建一个 {@link BakedQuad}。
	 *
	 * @return 构建出的 baked quad
	 */
	public BakedQuad bakeQuad() {
		if (bakedQuad != null) {
			return bakedQuad;
		}
		if (currentIndex < 3) {
			throw new IllegalStateException("顶点数不足，无法构建 quad: " + (currentIndex + 1));
		}
		BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(sprite, layer, itemRenderType, tintIndex, shade, lightEmission);
		long packedUV0 = UVPair.pack(uvs[0][0], uvs[0][1]);
		long packedUV1 = UVPair.pack(uvs[1][0], uvs[1][1]);
		long packedUV2 = UVPair.pack(uvs[2][0], uvs[2][1]);
		long packedUV3 = UVPair.pack(uvs[3][0], uvs[3][1]);
		bakedQuad = new BakedQuad(positions[0], positions[1], positions[2], positions[3],
				packedUV0, packedUV1, packedUV2, packedUV3,
				direction, materialInfo);
		return bakedQuad;
	}
}
