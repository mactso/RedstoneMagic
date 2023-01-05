package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;

public class GathererTileEntity extends BlockEntity {
	static final ItemStack FIRE_CORAL_STACK = new ItemStack(Items.FIRE_CORAL_BLOCK, 1);
	static final ItemStack JACK_O_LANTERN_STACK = new ItemStack(Items.JACK_O_LANTERN, 1);
	static final ItemStack LIGHT_SPELL_STACK = new ItemStack(ModBlocks.LIGHT_SPELL, 1);
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	
	final int NO_UPDATE = -1;
	int particleCount = 1;
	float humLevel = -0.2f;
	float humCycleDirection = -.1f;
	int manaSparkle = 0;
	int rndTimeOffset = -1;
	int lastDayTime = 0;

	boolean showManaLevel = false;
	
	public GathererTileEntity(BlockPos pos, BlockState state) {
		super(ModTileEntities.GATHERER, pos, state);
	}
	

	public void serverTick() {
//		if (level == null) {
//			return;
//		}
//		if (level.isClientSide) {
//			return;
//		}
		
		long tickTime = calcTickTime();

		if (tickTime % 10 == 0) {
			((ServerLevel) level).sendParticles(ParticleTypes.WITCH, 0.5D + (double) worldPosition.getX(),
					0.5D + (double) worldPosition.above(2).getY(), 0.5D + (double) worldPosition.getZ(), 3, 0, -1.15D, 0, 0.06D);
			if (manaSparkle > 0) {
				manaSparkle--;
				createNonBasicParticle(worldPosition.above(2), 3, new ItemParticleOption(ParticleTypes.ITEM, GLOWSTONE_STACK));
			}
		}
		
		// should make different gatherers processing spread across ticks 0-20.
 		if (tickTime % 20 == 0) {


			LevelChunk baseChunk = (LevelChunk)level.getChunk(worldPosition);				
			int chunkManaPowerLevel = 0;
			IMagicStorage cap = baseChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				chunkManaPowerLevel = getManaPowerLevel(cap.getManaStored());
			}
			doGathererHum();
			doShowManaLevelDisplay(chunkManaPowerLevel);
			doGathererParticles(1); 

			if (tickTime%6000 == 0L) {
				manaSparkle = 13;
				processGatheredManaToChunks();
			}
		}

	}


	private long calcTickTime() {
		long tickTime;
		if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
			tickTime = level.getDayTime() + rndTimeOffset;
		} else {
			tickTime = level.getGameTime() + rndTimeOffset;			
		}
		return tickTime;
	}

	
	private void doShowManaLevelDisplay(int manaPowerLevel) {
		if (showManaLevel) {
			for (int i = 1;i<= 8; i++ ) {
					if (i<=manaPowerLevel) {
						if (level.getBlockState(worldPosition.above(i)).getBlock() == Blocks.AIR) {
							level.setBlockAndUpdate(worldPosition.above(i), ModBlocks.LIGHT_SPELL.defaultBlockState());
						}
					} else {
						if (level.getBlockState(worldPosition.above(i)).getBlock() == ModBlocks.LIGHT_SPELL) {
							level.setBlockAndUpdate(worldPosition.above(i), Blocks.AIR.defaultBlockState());
						}
					}
				}

		} else {
			doEraseManaLevelDisplay();
		}
	}
	
	public void doEraseManaLevelDisplay() {
		for (int i = 1;i<= 8; i++ ) {
			if (level.getBlockState(worldPosition.above(i)).getBlock() == ModBlocks.LIGHT_SPELL) {
				level.destroyBlock(worldPosition.above(i), false);
			}
		}
	}
	
	@Override
	// restore state when chunk reloads
	public void load(CompoundTag nbt) {
		super.load(nbt);
		showManaLevel = nbt.getBoolean("showManaLevel");

	}
	
	@Override
	// save state when chunk unloads
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		compound.putBoolean("showManaLevel", showManaLevel);

	}
	
	private void doGathererHum() {
		humLevel += humCycleDirection;
		if(humLevel > 1.0) {
			humCycleDirection = -0.2f;
		}
		if(humLevel < -1.0) {
			humCycleDirection = 0.2f;
		}
		float humVolume = (float) (0.4 + (0.3f * humLevel));
		float humPitch = (float) (0.5 + (0.3f * humLevel));
		level.playSound(null, worldPosition, ModSounds.GATHERER_HUMS,
				SoundSource.BLOCKS, humVolume/3, humPitch);
	}

	private void processGatheredManaToChunks() {

		level.playSound(null, worldPosition, SoundEvents.ILLUSIONER_CAST_SPELL,
				SoundSource.BLOCKS, 0.14f, 0.15f);	
		doGathererParticles(15);  
		
		LevelChunk baseChunk = (LevelChunk)level.getChunk(worldPosition);
		int newManaLevel = 0;
		newManaLevel = addManaToChunk(baseChunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/2,5);
		int chunkX = baseChunk.getPos().x;
		int chunkZ = baseChunk.getPos().z;
		LevelChunk chunk = level.getChunk(chunkX+1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = level.getChunk(chunkX-1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = level.getChunk(chunkX, chunkZ+1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = level.getChunk(chunkX, chunkZ-1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		level.playSound(null, worldPosition, ModSounds.GATHERER_GATHERS,
				SoundSource.BLOCKS, 0.2f, 0.45f);	
	}

	public boolean doGathererInteraction(Player player, InteractionHand handIn) {
		BlockPos pos = player.blockPosition();

		if (player instanceof ServerPlayer) {
			showManaLevel = !showManaLevel;
			if (showManaLevel) {
				level.playSound(null, pos, SoundEvents.NOTE_BLOCK_PLING.get(),
						SoundSource.BLOCKS, 0.2f, 0.45f);	
			}
			LevelChunk chunk = (LevelChunk) level.getChunk(pos);
			chunk.setUnsaved(true);
		}
		return true;
	}

	private int getManaPowerLevel(int chunkManaLevel) {
		if (chunkManaLevel > 16367)
			return 7;
		if (chunkManaLevel > 4096)
			return 6;
		if (chunkManaLevel > 1024)
			return 5;
		if (chunkManaLevel> 256)
			return 4;
		if (chunkManaLevel > 64)
			return 3;
		if (chunkManaLevel > 16)
			return 2;
		return 1;
	}
		
	private void doGathererParticles(int particles) {
		((ServerLevel) level).sendParticles(ParticleTypes.WITCH, 0.5D + (double) worldPosition.getX(),
				0.5D + (double) worldPosition.above(2).getY(), 0.5D + (double) worldPosition.getZ(), particles, 0, -1.15D, 0, 0.06D);
	}

	private int addManaToChunk(LevelChunk chunk, int newManaAmount, int sparkle) {
		int newManaLevel = 0;
		IMagicStorage cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			cap.addMana(newManaAmount); // checks for max capacity internally based on object type.
		}
		int sparkleX = (int) (chunk.getPos().getMinBlockX()+chunk.getPos().getMaxBlockX())/2;
		int sparkleZ = (int) (chunk.getPos().getMinBlockZ()+chunk.getPos().getMaxBlockZ())/2;
		int sparkleY = worldPosition.getY() + 2;
		BlockPos sparklePos = new BlockPos (sparkleX,sparkleY,sparkleZ);
		createNonBasicParticle(sparklePos, sparkle, new ItemParticleOption(ParticleTypes.ITEM, GLOWSTONE_STACK));
		return newManaLevel;
	}
	private void createNonBasicParticle(BlockPos pos, int particleCount, ParticleOptions particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerLevel) level).sendParticles(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}	
}
