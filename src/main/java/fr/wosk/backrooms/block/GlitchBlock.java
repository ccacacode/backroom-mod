package fr.wosk.backrooms.block;

import com.mojang.serialization.MapCodec;
import fr.wosk.backrooms.GlitchEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GlitchBlock extends BaseEntityBlock {
	public static final MapCodec<GlitchBlock> CODEC = simpleCodec(GlitchBlock::new);

	public GlitchBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GlitchBlockEntity(pos, state);
	}

	// BaseEntityBlock rend le bloc invisible par défaut : on remet le modèle normal
	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (!level.isClientSide && entity instanceof ServerPlayer player) {
			// Déclenche la séquence : noclip -> noir -> sons -> lumières
			if (GlitchEvents.startSequence(player)) {
				level.removeBlock(pos, false); // le bloc disparaît une fois utilisé
			}
		}
		super.stepOn(level, pos, state, entity);
	}
}
