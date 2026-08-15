package com.justfatlard.usefulhoe.action;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public final class TillAction {

	private static final Map<Block, BlockState> TILLABLE = Map.of(
		Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(),
		Blocks.DIRT, Blocks.FARMLAND.defaultBlockState(),
		Blocks.DIRT_PATH, Blocks.FARMLAND.defaultBlockState(),
		Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState(),
		Blocks.ROOTED_DIRT, Blocks.DIRT.defaultBlockState()
	);

	private TillAction() {}

	public static boolean canTill(Level world, BlockPos pos, BlockState state) {
		if (!TILLABLE.containsKey(state.getBlock())) {
			return false;
		}

		BlockState above = world.getBlockState(pos.above());
		return above.isAir() || above.canBeReplaced();
	}

	public static boolean execute(Level world, BlockPos pos, Player player) {
		BlockState state = world.getBlockState(pos);

		if (!canTill(world, pos, state)) {
			return false;
		}

		BlockState tilled = TILLABLE.get(state.getBlock());
		if (tilled == null) {
			return false;
		}

		// canTill admits replaceable plants above (wider than vanilla's air-only
		// rule), so the hoe must clear them: grass survives on farmland and
		// would otherwise be left standing on the fresh tilled block
		BlockState above = world.getBlockState(pos.above());
		if (!above.isAir() && above.canBeReplaced()) {
			world.destroyBlock(pos.above(), true, player);
		}

		world.setBlockAndUpdate(pos, tilled);
		world.playSound(null, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0f, 1.0f);

		return true;
	}
}
