package fr.wosk.backrooms.block;

import fr.wosk.backrooms.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Ne stocke rien : sert uniquement à afficher l'effet de glitch côté client. */
public class GlitchBlockEntity extends BlockEntity {
	public GlitchBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.GLITCH_BLOCK_ENTITY, pos, state);
	}
}
