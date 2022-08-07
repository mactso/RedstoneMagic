package com.mactso.redstonemagic.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.tileentity.GathererTileEntity;
import com.mactso.redstonemagic.tileentity.ModTileEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class Gatherer extends BaseEntityBlock
{
	static int incr = 3;
	static long lastTime = 0;
	static final ItemStack REDGLASS_STACK = new ItemStack(Items.RED_STAINED_GLASS, 1);
	

	public static final IntegerProperty POWER = BlockStateProperties.LEVEL;
	
	private static VoxelShape SHAPE = Shapes.or(
			Shapes.box(0, 0, 0, 1, 0.25, 1),
			Shapes.box(0.125, 0.25, 0.125, 0.875, 0.5, 0.875),
			Shapes.box(0.25, 0.5, 0.25, 0.75, 0.75, 0.75),
			Shapes.box(0.375, 0.75, 0.375, 0.625, 1, 0.625));

	@Override // remove mana level indicator
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
		if (player instanceof ServerPlayer) {
			for (int i=2; i<=8; i++) {
				if (worldIn.getBlockState(pos.above(i)).getBlock() == ModBlocks.LIGHT_SPELL ){
					worldIn.destroyBlock(pos.above(i), false);
				}
			}
		}
		super.playerWillDestroy(worldIn, pos, state, player);
	}	
	
//	@Override
//	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
//		return super.canSurvive(state, worldIn, pos);
//	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GathererTileEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return !level.isClientSide ? createTickerHelper(type, ModTileEntities.GATHERER, Gatherer::tickEntity) : null;
	}

	private static void tickEntity(Level world, BlockPos pos, BlockState state, GathererTileEntity blockEntity) {
		blockEntity.serverTick();
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
	      return RenderShape.MODEL;
	}	
	
	public Gatherer(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult result) {

		if (!player.getAbilities().mayBuild) {
			return InteractionResult.PASS;
		} else {
			if ((worldIn instanceof ServerLevel)) {
				BlockEntity r = worldIn.getBlockEntity(pos);
				if (r instanceof GathererTileEntity) {
					if (((GathererTileEntity) r).doGathererInteraction(player, handIn) == false) {
						worldIn.playSound(null, pos, ModSounds.SPELL_FAILS, SoundSource.BLOCKS, 0.5f, 0.2f);
					}
				}
				
			}
				

			return InteractionResult.SUCCESS;
		}
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

		// StructureBlockTileEntity s;
		// RedstoneParticleData pR = new RedstoneParticleData(1.0f, 1.0f, 1.0f, 2.0f);
		worldIn.playSound(null, pos, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 0.5f, 0.2f);
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
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	public static int colorMultiplier(BlockState state) {
		int r = state.getValue(POWER) * 16;
		return r << 16;
	}

	//@Override
	public void TODOanimateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
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
