package com.mactso.redstonemagic.block;

import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.tileentity.RitualPylonTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RitualPylon extends ContainerBlock
{
//	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	   
	public RitualPylon(Properties properties) {
		super(properties);
//		this.setDefaultState(
//				this.stateContainer.getBaseState()
//				.with(POWER, Integer.valueOf(4)));
	}
	
	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

		// StructureBlockTileEntity s;
		worldIn.playSound(null, pos, SoundEvents.ENDER_EYE_DEATH, SoundCategory.BLOCKS, 0.5f, 0.2f);

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
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {

		if (!player.abilities.mayBuild) {
			return ActionResultType.PASS;
		} else {
			if ((worldIn instanceof ServerWorld)) {
				TileEntity r = worldIn.getBlockEntity(pos);
				if (r instanceof RitualPylonTileEntity) {
					if (((RitualPylonTileEntity) r).doRitualPylonInteraction(player, handIn) == false) {
						worldIn.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
						for (int i=0; i<6; i++) {
							double oX = worldIn.getRandom().nextDouble()-0.5D;
							double oY = worldIn.getRandom().nextDouble()-0.2D;
							double oZ = worldIn.getRandom().nextDouble()-0.5D;
							((ServerWorld) worldIn).sendParticles(ParticleTypes.SQUID_INK, 0.5D + (double) pos.getX(),
									(double) pos.getY() + 0.35D , 0.5D + (double) pos.getZ(), 2, 0.0D+oX, 0.1D+ oY, 0.0D+oZ, -0.04D);

						
						}
					}
				}
				
			}
				

			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		      return BlockRenderType.MODEL;
	}	
	
	
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new RitualPylonTileEntity();
	}
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
