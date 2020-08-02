package com.mactso.redstonemagic.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TestBlock2 extends Block
{
	static int incr = 1;
	static int skip = 0;
	static int delay = 4;
	public static final IntegerProperty POWER = BlockStateProperties.LEVEL_0_15;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static VoxelShape SHAPE = VoxelShapes.or(
			VoxelShapes.create(0, 0, 0, 1, 0.25, 1),
			VoxelShapes.create(0.125, 0.25, 0.125, 0.875, 0.5, 0.875),
			VoxelShapes.create(0.25, 0.5, 0.25, 0.75, 0.75, 0.75),
			VoxelShapes.create(0.375, 0.75, 0.375, 0.625, 1, 0.625));

	public TestBlock2(Properties properties) {
		super(properties);
		setDefaultState(stateContainer.getBaseState()
				.with(POWER, Integer.valueOf(0))
				.with(POWERED, false));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (!state.get(POWERED))
		{
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, delay, TickPriority.VERY_HIGH);
		}
		worldIn.setBlockState(pos, state.func_235896_a_(POWERED));
		return ActionResultType.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
		//return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return SHAPE;
	}

	public static int colorMultiplier(BlockState state) {
		int r = state.get(POWER) * 16;
		return r << 16;
	}

	@Override
	public void tick(BlockState stateIn, ServerWorld worldIn, BlockPos pos, Random rand) {
		int level = stateIn.get(POWER);
		level += incr;
		if (incr > 0)
		{
			if (level > 15)
			{
				level = 14;
				incr = -1;
			}
		}
		else
		{
			if (level < 0)
			{
				level = 1;
				incr = 1;
			}
		}
		worldIn.setBlockState(pos, stateIn.with(POWER, Integer.valueOf(level)));
		if (stateIn.get(POWERED))
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, delay, TickPriority.VERY_HIGH);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWER, POWERED);
	}
}
