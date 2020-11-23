package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class GathererTileEntity extends TileEntity implements ITickableTileEntity {
	static final ItemStack RED_STAINED_GLASS_STACK = new ItemStack(Items.RED_STAINED_GLASS, 1);
	static final ItemStack RED_GLAZED_TERRACOTTA_STACK = new ItemStack(Items.RED_GLAZED_TERRACOTTA, 1);
	static final ItemStack REDSTONE_BLOCK_STACK = new ItemStack(Items.REDSTONE_BLOCK, 1);
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	
	final int NO_UPDATE = -1;
	int particleCount = 1;
	float humLevel = -0.2f;
	float humCycleDirection = -.1f;
	int roughChunkMana = 0;
	
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
		long dayTime = world.getDayTime();
		if (dayTime % 20 == 0) {
			// periodically update level of chunk mana to reduce server load.
			if (dayTime % 200 == 0) {
				Chunk baseChunk = (Chunk)world.getChunk(pos);				
				IMagicStorage cap = baseChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
				if (cap != null) {
					roughChunkMana = cap.getManaStored(); // checks for max capacity internally based on object type.
				}
			}
			doGathererHum();
			doGathererParticles(2);      
			if (dayTime%6000 == 0L) {
				processGatheredManaToChunks();
			}
		}
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
				SoundCategory.BLOCKS, 0.04f, 0.05f);	
		doGathererParticles(15);                
		Chunk baseChunk = (Chunk)world.getChunk(pos);
		// baseChunk.getEntitiesWithinAABBForEntity(entityIn, aabb, listToFill, filter);
		addManaToChunk(baseChunk,RitualPylonTileEntity.RITUAL_CHUNK_COST,4);
		int chunkX = baseChunk.getPos().x;
		int chunkZ = baseChunk.getPos().z;
		Chunk chunk = world.getChunk(chunkX+1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4,2);
		chunk = world.getChunk(chunkX-1, chunkZ);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4,2);
		chunk = world.getChunk(chunkX, chunkZ+1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4,2);
		chunk = world.getChunk(chunkX, chunkZ-1);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4,2);
		world.playSound(null, pos, ModSounds.SPELL_RESONATES,
				SoundCategory.BLOCKS, 0.04f, 0.05f);	
	}

	private void doGathererParticles(int particles) {
		((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
				0.5D + (double) pos.up(2).getY(), 0.5D + (double) pos.getZ(), particles, 0, -1.15D, 0, 0.06D);

		if (roughChunkMana < 1) {
			// nothing.
		} else if (roughChunkMana < 10) {
			createNonBasicParticle(pos.up(2), 2, new ItemParticleData(ParticleTypes.ITEM, RED_GLAZED_TERRACOTTA_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 0.35D, 0.5D + (double) pos.getZ(), 2, 0.0D, 0.2D, 0.0D, -0.04D);
		} else if (roughChunkMana < 100) {
			createNonBasicParticle(pos.up(2), 3, new ItemParticleData(ParticleTypes.ITEM, RED_STAINED_GLASS_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 0.35D, 0.5D + (double) pos.getZ(), 3, 0.0D, 0.2D, 0.0D, -0.04D);
		} else if (roughChunkMana < 1000) {
			createNonBasicParticle(pos.up(2), 4, new ItemParticleData(ParticleTypes.ITEM, REDSTONE_BLOCK_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 0.35D, 0.5D + (double) pos.getZ(), 3, 0.0D, 0.2D, 0.0D, -0.04D);
		} else {
			createNonBasicParticle(pos.up(2), 5, RedstoneParticleData.REDSTONE_DUST);
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 0.35D, 0.5D + (double) pos.getZ(), 5, 0.0D, 0.2D, 0.0D, -0.04D);
		}
	}

	private void addManaToChunk(Chunk chunk, int newManaAmount, int sparkle) {
		IMagicStorage cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			cap.addMana(newManaAmount); // checks for max capacity internally based on object type.
			if (newManaAmount == RitualPylonTileEntity.RITUAL_CHUNK_COST) {
				roughChunkMana = cap.getManaStored();
			}
		}
		int sparkleX = (int) (chunk.getPos().getXStart()+chunk.getPos().getXEnd())/2;
		int sparkleZ = (int) (chunk.getPos().getZStart()+chunk.getPos().getZEnd())/2;
		int sparkleY = pos.getY() + 2;
		BlockPos sparklePos = new BlockPos (sparkleX,sparkleY,sparkleZ);
		createNonBasicParticle(sparklePos, sparkle, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));

	}
	private void createNonBasicParticle(BlockPos pos, int particleCount, IParticleData particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerWorld) world).spawnParticle(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}	
}
