package com.mactso.redstonemagic.block;

import net.minecraft.block.Block;

public class RedstoneMagicPylonMinor extends Block
{
//	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	public RedstoneMagicPylonMinor(Properties properties) {
		super(properties);
//		this.setDefaultState(
//				this.stateContainer.getBaseState()
//				.with(POWER, Integer.valueOf(4)));
	}

//	@Override
//	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
//			Hand handIn, BlockRayTraceResult result) {
//		if (!player.abilities.allowEdit) {
//			return ActionResultType.PASS;
//		} else {
//			int i = state.get(POWER);
//			if (player.isSneaking()) {
//				i = (i > 0) ? i - 1 : 15; 
//			}
//			else {
//				i = (i < 15) ? i + 1 : 0;
//			}
//			worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 3);
//			return ActionResultType.SUCCESS;
//		}
//	}
//
//	@Override
//	public boolean canProvidePower(BlockState state) {
//		return true;
//	}
//
//	@Override
//	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//		return getWeakPower(blockState, blockAccess, pos, side);
//	}
//
//	@Override
//	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//		return blockState.get(POWER);
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	public static int colorMultiplier(int power) {
//		float f = (float) power / 15.0F;
//		float f1 = f * 0.6F + 0.4F;
//		if (power == 0) {
//			f1 = 0.3F;
//		}
//
//		float f2 = f * f * 0.7F - 0.5F;
//		float f3 = f * f * 0.6F - 0.7F;
//
//		int i = MathHelper.clamp((int) (f1 * 255.0F), 0, 255);
//		int j = MathHelper.clamp((int) (f2 * 255.0F), 0, 255);
//		int k = MathHelper.clamp((int) (f3 * 255.0F), 0, 255);
//		return -16777216 | i << 16 | j << 8 | k;
//	}
//
//	@Override
//	protected void fillStateContainer(Builder<Block, BlockState> builder) {
//		builder.add(POWER);
//	}
}
