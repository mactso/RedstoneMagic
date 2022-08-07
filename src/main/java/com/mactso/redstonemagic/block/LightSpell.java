package com.mactso.redstonemagic.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class LightSpell extends Block {
	protected static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 9.0D, 9.0D, 9.0D);

	public LightSpell(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	
//	@Override
//	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
//		return true;
//	}
	
	
}