package com.justfatlard.usefulhoe.hoe;

import com.justfatlard.usefulhoe.config.ModConfig;
import com.justfatlard.usefulhoe.enchantment.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the affected block area for hoe actions.
 *
 * Positioning: target block sits at bottom-center of the area (closest to player).
 * Width is centered left/right of facing. Depth extends forward.
 * Slope tolerance: +1/-1 Y from target, preferring actionable blocks.
 * Area size comes from config via Reach enchantment level.
 */
public final class HoeAreaCalculator {

	private HoeAreaCalculator() {}

	public static List<BlockPos> calculateArea(BlockPos target, PlayerEntity player, ItemStack stack, World world) {
		int reachLevel = ModEnchantments.getReachLevel(world, stack);
		int[] area = ModConfig.get().getReachArea(reachLevel);

		Direction facing = player.getHorizontalFacing();
		return calculateAreaWithSlopes(target, area[0], area[1], facing, world);
	}

	/**
	 * Calculates area with slope tolerance (+1/-1 Y per position).
	 * For even widths, the extra column is biased toward the player's left.
	 */
	public static List<BlockPos> calculateAreaWithSlopes(BlockPos target, int width, int depth, Direction facing, World world) {
		List<BlockPos> positions = new ArrayList<>();

		int halfWidth = width / 2;
		int baseY = target.getY();
		Direction rightDir = facing.rotateYClockwise();

		for (int w = -halfWidth; w <= halfWidth; w++) {
			if (width % 2 == 0 && w == halfWidth) continue;

			for (int d = 0; d < depth; d++) {
				int offsetX = rightDir.getOffsetX() * w + facing.getOffsetX() * d;
				int offsetZ = rightDir.getOffsetZ() * w + facing.getOffsetZ() * d;

				BlockPos best = findFirstValidY(world, target.getX() + offsetX, baseY, target.getZ() + offsetZ);
				positions.add(best);
			}
		}

		return positions;
	}

	/**
	 * Flat area calculation (no world access, no slope handling).
	 */
	public static List<BlockPos> calculateFlatArea(BlockPos target, int width, int depth, Direction facing) {
		List<BlockPos> positions = new ArrayList<>();

		int halfWidth = width / 2;
		Direction rightDir = facing.rotateYClockwise();

		for (int w = -halfWidth; w <= halfWidth; w++) {
			if (width % 2 == 0 && w == halfWidth) continue;

			for (int d = 0; d < depth; d++) {
				int offsetX = rightDir.getOffsetX() * w + facing.getOffsetX() * d;
				int offsetZ = rightDir.getOffsetZ() * w + facing.getOffsetZ() * d;

				positions.add(target.add(offsetX, 0, offsetZ));
			}
		}

		return positions;
	}

	/**
	 * Returns the first Y level (base, -1, +1) with an actionable block,
	 * or base Y if none found.
	 */
	private static BlockPos findFirstValidY(World world, int x, int baseY, int z) {
		int[] yOffsets = {0, -1, 1};

		for (int yOffset : yOffsets) {
			BlockPos pos = new BlockPos(x, baseY + yOffset, z);
			if (isActionableBlock(world, pos)) {
				return pos;
			}
		}

		return new BlockPos(x, baseY, z);
	}

	private static boolean isActionableBlock(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		return state.isIn(BlockTags.DIRT) || block == Blocks.GRASS_BLOCK
			|| block instanceof FarmlandBlock || block == Blocks.SOUL_SAND
			|| block instanceof CropBlock || block instanceof SweetBerryBushBlock
			|| block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO
			|| block == Blocks.CACTUS || block == Blocks.KELP_PLANT;
	}

	public static int[] getDimensions(ItemStack stack, World world) {
		int reachLevel = ModEnchantments.getReachLevel(world, stack);
		return ModConfig.get().getReachArea(reachLevel);
	}
}
