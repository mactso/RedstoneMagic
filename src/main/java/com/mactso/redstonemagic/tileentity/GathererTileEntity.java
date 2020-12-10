package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class GathererTileEntity extends TileEntity implements ITickableTileEntity {
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

	boolean showManaLevel = false;
	
	public GathererTileEntity() {
		super(ModTileEntities.GATHERER);
	}
	

	@Override
	public void tick() {
		if (world == null) {
			return;
		}
		if (world.isRemote) {
			return;
		}
		
		if (rndTimeOffset < 0) rndTimeOffset = this.world.rand.nextInt(20);
		
		long dayTime = world.getDayTime() + rndTimeOffset;
		
		if (dayTime % 10 == 0) {
			((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
					0.5D + (double) pos.up(2).getY(), 0.5D + (double) pos.getZ(), 3, 0, -1.15D, 0, 0.06D);
			if (manaSparkle > 0) {
				manaSparkle--;
				createNonBasicParticle(pos.up(2), 3, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));
			}
		}
		
		// should make different gatherers processing spread across ticks 0-20.

 		if (dayTime % 20 == 0) {
			Chunk baseChunk = (Chunk)world.getChunk(pos);				
			int chunkManaPowerLevel = 0;
			IMagicStorage cap = baseChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				chunkManaPowerLevel = getManaPowerLevel(cap.getManaStored());
			}
			doGathererHum();
			doShowManaLevelDisplay(chunkManaPowerLevel);
			doGathererParticles(1);      
			if (dayTime%6000 == 0L) {
				manaSparkle = 17;
				processGatheredManaToChunks();
			}
		}

	}

	private void doShowManaLevelDisplay(int manaPowerLevel) {
		if (showManaLevel) {
			for (int i = 1;i<= 8; i++ ) {
					if (i<=manaPowerLevel) {
						if (world.getBlockState(pos.up(i)).getBlock() == Blocks.AIR) {
							world.setBlockState(pos.up(i), ModBlocks.LIGHT_SPELL.getDefaultState());
						}
					} else {
						if (world.getBlockState(pos.up(i)).getBlock() == ModBlocks.LIGHT_SPELL) {
							world.setBlockState(pos.up(i), Blocks.AIR.getDefaultState());
						}
					}
				}

		} else {
			doEraseManaLevelDisplay();
		}
	}
	
	public void doEraseManaLevelDisplay() {
		for (int i = 1;i<= 8; i++ ) {
			if (world.getBlockState(pos.up(i)).getBlock() == ModBlocks.LIGHT_SPELL) {
				world.destroyBlock(pos.up(i), false);
			}
		}
	}
	
	@Override
	// restore state when chunk reloads
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		showManaLevel = nbt.getBoolean("showManaLevel");
	}
	
	@Override
	// save state when chunk unloads
	public CompoundNBT write(CompoundNBT compound) {

		compound.putBoolean("showManaLevel", showManaLevel);

		return super.write(compound);
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
		world.playSound(null, pos, ModSounds.GATHERER_HUMS,
				SoundCategory.BLOCKS, humVolume/3, humPitch);
	}

	private void processGatheredManaToChunks() {

		world.playSound(null, pos, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
				SoundCategory.BLOCKS, 0.14f, 0.15f);	
		doGathererParticles(15);  
		
		Chunk baseChunk = (Chunk)world.getChunk(pos);
		int newManaLevel = 0;
		newManaLevel = addManaToChunk(baseChunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/2,5);
		int chunkX = baseChunk.getPos().x;
		int chunkZ = baseChunk.getPos().z;
		Chunk chunk = world.getChunk(chunkX+1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = world.getChunk(chunkX-1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = world.getChunk(chunkX, chunkZ+1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		chunk = world.getChunk(chunkX, chunkZ-1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/8,3);
		world.playSound(null, pos, ModSounds.GATHERER_GATHERS,
				SoundCategory.BLOCKS, 0.2f, 0.45f);	
	}

	public boolean doGathererInteraction(PlayerEntity player, Hand handIn) {
		if (player instanceof ServerPlayerEntity) {
			showManaLevel = !showManaLevel;
			if (showManaLevel) {
				world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_PLING,
						SoundCategory.BLOCKS, 0.2f, 0.45f);	
			}
			Chunk chunk = (Chunk) world.getChunk(pos);
			chunk.markDirty();
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
		((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
				0.5D + (double) pos.up(2).getY(), 0.5D + (double) pos.getZ(), particles, 0, -1.15D, 0, 0.06D);
	}

	private int addManaToChunk(Chunk chunk, int newManaAmount, int sparkle) {
		int newManaLevel = 0;
		IMagicStorage cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			cap.addMana(newManaAmount); // checks for max capacity internally based on object type.
		}
		int sparkleX = (int) (chunk.getPos().getXStart()+chunk.getPos().getXEnd())/2;
		int sparkleZ = (int) (chunk.getPos().getZStart()+chunk.getPos().getZEnd())/2;
		int sparkleY = pos.getY() + 2;
		BlockPos sparklePos = new BlockPos (sparkleX,sparkleY,sparkleZ);
		createNonBasicParticle(sparklePos, sparkle, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));
		return newManaLevel;
	}
	private void createNonBasicParticle(BlockPos pos, int particleCount, IParticleData particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerWorld) world).spawnParticle(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}	
}
