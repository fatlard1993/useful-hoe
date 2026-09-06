package com.justfatlard.usefulhoe.mixin;

import com.justfatlard.usefulhoe.crop.RainGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catches every block the game random-ticks, so rain can reach any crop rather than only the ones
 * that happen to extend a vanilla class.
 *
 * <p>Standing here rather than on {@code CropBlock} because a crop is not a class. A slab crop
 * extends {@code CropBlock}, but a slab melon stem extends {@code VegetationBlock}, and sugar cane
 * and bamboo extend {@code Block} outright - naming classes would have quietly covered some of a
 * mod's crops and not others. {@code ServerLevel.tickChunk} dispatches every random tick through
 * this one method, so it is the only place that sees all of them.
 *
 * <p>The cost of sitting in that path is one field read on a block that is not in the rain, which is
 * nearly all of them; see {@link RainGrowth} for the order the questions are asked in.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class RandomTickRainMixin {

	@Inject(method = "randomTick", at = @At("TAIL"))
	private void usefulhoe$rainGrowth(ServerLevel level, BlockPos pos, RandomSource random,
			CallbackInfo ci) {
		// Every instance of BlockStateBase is a BlockState; the split exists for generics, not for
		// two kinds of object.
		RainGrowth.afterRandomTick((BlockState) (Object) this, level, pos, random);
	}
}
