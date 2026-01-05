package com.justfatlard.usefulhoe.hoe;

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
 * Calculates affected area for hoe actions with asymmetric shapes.
 * The target block is at the bottom-center of the area (closest to player),
 * with the area extending forward in the player's facing direction.
 * Area size is determined solely by Reach enchantment level.
 * Supports sloped terrain (+1/-1 Y variance).
 */
public final class HoeAreaCalculator {

	private HoeAreaCalculator() {}

	/**
	 * Calculates all block positions affected by a hoe action.
	 * Area size is based on Reach enchantment level only.
	 * Supports sloped terrain by checking Y-1, Y, Y+1 for each position.
	 */
	public static List<BlockPos> calculateArea(BlockPos target, PlayerEntity player, ItemStack stack, World world) {
		int reachLevel = ModEnchantments.getReachLevel(world, stack);
		HoeTier tier = HoeTier.fromReachLevel(reachLevel);

		Direction facing = player.getHorizontalFacing();
		return calculateAsymmetricArea(target, tier.width, tier.depth, facing, world);
	}

	/**
	 * Calculates area with target at bottom-center, supporting slopes.
	 * Width is centered (left/right), depth extends forward from target.
	 * For each X/Z, finds the best Y level within +1/-1 of target.
	 */
	public static List<BlockPos> calculateAsymmetricArea(BlockPos target, int width, int depth, Direction facing, World world) {
		List<BlockPos> positions = new ArrayList<>();

		int halfWidth = width / 2;
		int baseY = target.getY();

		// Determine axis directions based on facing
		Direction rightDir = facing.rotateYClockwise();

		for (int w = -halfWidth; w <= halfWidth; w++) {
			if (width % 2 == 0 && w == halfWidth) continue;

			for (int d = 0; d < depth; d++) {
				int offsetX = rightDir.getOffsetX() * w + facing.getOffsetX() * d;
				int offsetZ = rightDir.getOffsetZ() * w + facing.getOffsetZ() * d;

				// Find best Y level for this X/Z position
				BlockPos bestPos = findBestYLevel(world, target.getX() + offsetX, baseY, target.getZ() + offsetZ);
				positions.add(bestPos);
			}
		}

		return positions;
	}

	/**
	 * Finds the best Y level for a position by checking Y-1, Y, Y+1.
	 * Prefers positions with actionable blocks (farmland, tillable, crops).
	 */
	private static BlockPos findBestYLevel(World world, int x, int baseY, int z) {
		// Check in order: base Y, Y-1, Y+1
		int[] yOffsets = {0, -1, 1};

		for (int yOffset : yOffsets) {
			BlockPos pos = new BlockPos(x, baseY + yOffset, z);
			if (isPotentialTarget(world, pos)) {
				return pos;
			}
		}

		// Default to base Y if nothing found
		return new BlockPos(x, baseY, z);
	}

	/**
	 * Checks if a position could be a target for any hoe action.
	 */
	private static boolean isPotentialTarget(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		// Tillable ground
		if (state.isIn(BlockTags.DIRT) || block == Blocks.GRASS_BLOCK) {
			return true;
		}

		// Farmland (for planting or has crop above)
		if (block instanceof FarmlandBlock || block == Blocks.SOUL_SAND) {
			return true;
		}

		// Crops
		if (block instanceof CropBlock || block instanceof SweetBerryBushBlock) {
			return true;
		}

		// Vertical crops
		if (block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO ||
			block == Blocks.CACTUS || block == Blocks.KELP_PLANT) {
			return true;
		}

		return false;
	}

	/**
	 * Overload for preview rendering (no world access needed for flat preview).
	 */
	public static List<BlockPos> calculateAsymmetricArea(BlockPos target, int width, int depth, Direction facing) {
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
	 * Gets dimensions based on reach enchantment level.
	 */
	public static int[] getDimensions(ItemStack stack, World world) {
		int reachLevel = ModEnchantments.getReachLevel(world, stack);
		HoeTier tier = HoeTier.fromReachLevel(reachLevel);

		return new int[] {
			tier.width,
			tier.depth
		};
	}
}
