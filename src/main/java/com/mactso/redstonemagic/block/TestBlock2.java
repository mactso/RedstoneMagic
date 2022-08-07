package com.mactso.redstonemagic.block;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class TestBlock2 extends Block
{
	static int incr = 1;
	static int skip = 0;
	static int delay = 4;
	public static final IntegerProperty POWER = BlockStateProperties.LEVEL;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static VoxelShape SHAPE = Shapes.or(
			Shapes.box(0, 0, 0, 1, 0.25, 1),
			Shapes.box(0.125, 0.25, 0.125, 0.875, 0.5, 0.875),
			Shapes.box(0.25, 0.5, 0.25, 0.75, 0.75, 0.75),
			Shapes.box(0.375, 0.75, 0.375, 0.625, 1, 0.625));

	public TestBlock2(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(POWER, Integer.valueOf(0))
				.setValue(POWERED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if (!state.getValue(POWERED))
		{
			worldIn.getBlockTicks().schedule(new ScheduledTick<Block>(this, pos, delay, TickPriority.VERY_HIGH, worldIn.nextSubTickCount()));
		}
		worldIn.setBlockAndUpdate(pos, state.cycle(POWERED));
		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
		//return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos,
			CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return SHAPE;
	}

	public static int colorMultiplier(BlockState state) {
		int r = state.getValue(POWER) * 16;
		return r << 16;
	}

	@Override
	public void tick(BlockState stateIn, ServerLevel worldIn, BlockPos pos, Random rand) {
		int level = stateIn.getValue(POWER);
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
		worldIn.setBlockAndUpdate(pos, stateIn.setValue(POWER, Integer.valueOf(level)));
		if (stateIn.getValue(POWERED))
			worldIn.getBlockTicks().schedule(new ScheduledTick<Block>(this, pos, delay, TickPriority.VERY_HIGH, worldIn.nextSubTickCount()));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWER, POWERED);
	}
}
