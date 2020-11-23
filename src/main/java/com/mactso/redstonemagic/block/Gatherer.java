package com.mactso.redstonemagic.block;

import java.util.Random;

import com.mactso.redstonemagic.tileentity.GathererTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class Gatherer extends ContainerBlock
{
	static int incr = 3;
	static long lastTime = 0;

	public static final IntegerProperty POWER = BlockStateProperties.LEVEL_0_15;
	

	private static VoxelShape SHAPE = VoxelShapes.or(
			VoxelShapes.create(0, 0, 0, 1, 0.25, 1),
			VoxelShapes.create(0.125, 0.25, 0.125, 0.875, 0.5, 0.875),
			VoxelShapes.create(0.25, 0.5, 0.25, 0.75, 0.75, 0.75),
			VoxelShapes.create(0.375, 0.75, 0.375, 0.625, 1, 0.625));

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return super.isValidPosition(state, worldIn, pos);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new GathererTileEntity();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
	      return BlockRenderType.MODEL;
	}	
	
	public Gatherer(Properties properties) {
		super(properties);
		setDefaultState(stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
	}

//	public static int getLightLevel(BlockState b) {
////TODO get from tile entity
//		return lightlevel;
//	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	public static int colorMultiplier(BlockState state) {
		int r = state.get(POWER) * 16;
		return r << 16;
	}

	//@Override
	public void TODOanimateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		int level = stateIn.get(POWER);
		long time = worldIn.getGameTime();
		if (time - lastTime > 20L || time < lastTime)
		{
			lastTime = time;
			level += incr;
			if (incr > 0)
			{
				if (level > 15)
				{
					level = 12;
					incr = -3;
				}
			}
			else
			{
				if (level < 0)
				{
					level = 3;
					incr = 3;
				}
			}
			worldIn.setBlockState(pos, stateIn.with(POWER, Integer.valueOf(level)));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}
}
