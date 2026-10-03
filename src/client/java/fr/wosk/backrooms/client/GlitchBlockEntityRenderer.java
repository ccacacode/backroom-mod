package fr.wosk.backrooms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.wosk.backrooms.block.GlitchBlockEntity;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Dessine des copies du bloc qui tremblent et se déforment en permanence : l'effet "glitch". */
public class GlitchBlockEntityRenderer implements BlockEntityRenderer<GlitchBlockEntity> {
	public GlitchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(GlitchBlockEntity blockEntity, float partialTick, PoseStack poseStack,
					   MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		Level level = blockEntity.getLevel();
		if (level == null) {
			return;
		}

		// Le glitch change toutes les 2 ticks (très nerveux)
		long step = level.getGameTime() / 2;
		Random random = new Random(blockEntity.getBlockPos().asLong() * 31L + step);

		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockState state = blockEntity.getBlockState();

		int copies = 2 + random.nextInt(3);
		for (int i = 0; i < copies; i++) {
			poseStack.pushPose();

			// De temps en temps, un gros décalage violent
			float amplitude = random.nextInt(8) == 0 ? 1.0f : 0.4f;
			poseStack.translate(
				(random.nextFloat() - 0.5f) * amplitude,
				(random.nextFloat() - 0.5f) * amplitude,
				(random.nextFloat() - 0.5f) * amplitude
			);

			// Déformation (étirement) autour du centre du bloc
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.scale(
				0.6f + random.nextFloat() * 0.9f,
				0.6f + random.nextFloat() * 0.9f,
				0.6f + random.nextFloat() * 0.9f
			);
			poseStack.translate(-0.5, -0.5, -0.5);

			dispatcher.renderSingleBlock(state, poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
			poseStack.popPose();
		}
	}
}
