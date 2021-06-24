package com.mactso.redstonemagic.block;

import java.util.Random;

import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.tileentity.GathererTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class Gatherer extends ContainerBlock
{
	static int incr = 3;
	static long lastTime = 0;

	public static final IntegerProperty POWER = BlockStateProperties.LEVEL;
	
    
	
	private static VoxelShape SHAPE = VoxelShapes.or(
			VoxelShapes.box(0, 0, 0, 1, 0.25, 1),
			VoxelShapes.box(0.125, 0.25, 0.125, 0.875, 0.5, 0.875),
			VoxelShapes.box(0.25, 0.5, 0.25, 0.75, 0.75, 0.75),
			VoxelShapes.box(0.375, 0.75, 0.375, 0.625, 1, 0.625));

	@Override // remove mana level indicator
	public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			for (int i=2; i<=8; i++) {
				if (worldIn.getBlockState(pos.above(i)).getBlock() == ModBlocks.LIGHT_SPELL ){
					worldIn.destroyBlock(pos.above(i), false);
				}
			}
		}
		super.playerWillDestroy(worldIn, pos, state, player);
	}	
	
	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return super.canSurvive(state, worldIn, pos);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new GathererTileEntity();
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
	      return BlockRenderType.MODEL;
	}	
	
	public Gatherer(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
	}

//	@Override
//	public Class<GathererTileEntity> getTileEntityClass() {
//		return GatherTileEntity.class;
//	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		// TODO Auto-generated method stub
		return super.createTileEntity(state, world);
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {

		if (!player.abilities.mayBuild) {
			return ActionResultType.PASS;
		} else {
			if ((worldIn instanceof ServerWorld)) {
				TileEntity r = worldIn.getBlockEntity(pos);
				if (r instanceof GathererTileEntity) {
					if (((GathererTileEntity) r).doGathererInteraction(player, handIn) == false) {
						worldIn.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
					}
				}
				
			}
				

			return ActionResultType.SUCCESS;
		}
	}
	
	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

		// StructureBlockTileEntity s;
		// RedstoneParticleData pR = new RedstoneParticleData(1.0f, 1.0f, 1.0f, 2.0f);
		worldIn.playSound(null, pos, SoundEvents.ENDER_EYE_DEATH, SoundCategory.BLOCKS, 0.5f, 0.2f);
//		System.out.println("Gatherer Placed at ("+pos.getX()+", "+pos.getY()+", "+pos.getZ()+")");
		if (worldIn.isClientSide()) {
			int iY = pos.getY() + 1;
			int sX = worldIn.getChunk(pos).getPos().getMinBlockX();
			int sZ = worldIn.getChunk(pos).getPos().getMinBlockZ();

			for (int iX = 0; iX <= 15; iX++) {
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + iX + sX, 0.35D + iY, 0.5D + sZ,
						0.0D, 0.001D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + iX + sX, 0.35D + iY, 0.5D + sZ +15,
						0.0D, 0.001D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + iX + sX, 0.5D + iY, 0.5D + sZ,
						0.0D, 0.05D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + iX + sX, 0.5D + iY, 0.5D + sZ +15,
						0.0D, 0.05D, 0.0D);

			}

			for (int iZ = 0; iZ <= 15; iZ++) {
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + sX, 0.35D + iY, 0.5D + iZ + sZ,
						0.0D, 0.001D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + sX +15, 0.35D + iY, 0.5D + iZ + sZ,
						0.0D, 0.001D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + sX, 0.5D + iY, 0.5D + iZ + sZ,
						0.0D, 0.05D, 0.0D);
				worldIn.addParticle(ParticleTypes.END_ROD, 0.5D + sX +15, 0.5D + iY, 0.5D + iZ + sZ,
						0.0D, 0.05D, 0.0D);

			}

		}
		super.setPlacedBy(worldIn, pos, state, placer, stack);

	}	
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	public static int colorMultiplier(BlockState state) {
		int r = state.getValue(POWER) * 16;
		return r << 16;
	}

	//@Override
	public void TODOanimateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		int level = stateIn.getValue(POWER);
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
			worldIn.setBlockAndUpdate(pos, stateIn.setValue(POWER, Integer.valueOf(level)));
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}
}
