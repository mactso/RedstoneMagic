package com.mactso.redstonemagic.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class LightSpell extends Block {
	protected static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 9.0D, 9.0D, 9.0D);

	public LightSpell(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	
//	@Override
//	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
//		return true;
//	}
	
	
}