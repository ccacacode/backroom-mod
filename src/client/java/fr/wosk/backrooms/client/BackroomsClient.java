package fr.wosk.backrooms.client;

import fr.wosk.backrooms.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class BackroomsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlocks.GLITCH_BLOCK_ENTITY, GlitchBlockEntityRenderer::new);
	}
}
