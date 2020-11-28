package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.item.ModItems;
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
	static final ItemStack FIRE_CORAL_STACK = new ItemStack(Items.FIRE_CORAL_BLOCK, 1);
	static final ItemStack JACK_O_LANTERN_STACK = new ItemStack(Items.JACK_O_LANTERN, 1);
	static final ItemStack LIGHT_SPELL_STACK = new ItemStack(ModBlocks.LIGHT_SPELL, 1);
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	
	final int NO_UPDATE = -1;
	int particleCount = 1;
	float humLevel = -0.2f;
	float humCycleDirection = -.1f;
	int roughChunkMana = 0;
	int manaSparkle = 0;
	
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
		if (dayTime % 10 == 0) {
			((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
					0.5D + (double) pos.up(2).getY(), 0.5D + (double) pos.getZ(), 3, 0, -1.15D, 0, 0.06D);
			if (manaSparkle > 0) {
				manaSparkle--;
				createNonBasicParticle(pos.up(2), 3, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));
			}
		}
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
			doGathererParticles(1);      
			if (dayTime%6000 == 0L) {
				manaSparkle = 11;
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
				SoundCategory.BLOCKS, 0.14f, 0.15f);	
		doGathererParticles(15);                
		Chunk baseChunk = (Chunk)world.getChunk(pos);
		// baseChunk.getEntitiesWithinAABBForEntity(entityIn, aabb, listToFill, filter);
		addManaToChunk(baseChunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/2,5);
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

	private void doGathererParticles(int particles) {
		
		
		((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
				0.5D + (double) pos.up(2).getY(), 0.5D + (double) pos.getZ(), particles, 0, -1.15D, 0, 0.06D);

		
		if (roughChunkMana > 64) {
			createNonBasicParticle(pos.up(2), particles, new ItemParticleData(ParticleTypes.ITEM, LIGHT_SPELL_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 2.35D, 0.5D + (double) pos.getZ(), particles, 0.0D, 0.2D, 0.0D, -0.04D);
		}
		if (roughChunkMana > 256) {
			createNonBasicParticle(pos.up(3), particles, new ItemParticleData(ParticleTypes.ITEM, FIRE_CORAL_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 3.35D, 0.5D + (double) pos.getZ(), particles, 0.0D, 0.2D, 0.0D, -0.04D);
		} 
		if (roughChunkMana > 1024) {
			createNonBasicParticle(pos.up(4), particles, new ItemParticleData(ParticleTypes.ITEM, JACK_O_LANTERN_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 4.35D, 0.5D + (double) pos.getZ(), particles, 0.0D, 0.2D, 0.0D, -0.04D);
		} 
		if (roughChunkMana > 4096) {
			createNonBasicParticle(pos.up(5), particles, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 5.35D, 0.5D + (double) pos.getZ(), particles, 0.0D, 0.2D, 0.0D, -0.04D);
		}
		if (roughChunkMana > 16367) {
			createNonBasicParticle(pos.up(particles+1), particles+2, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));
			((ServerWorld) world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(),
					(double) pos.getY() + 5.35D, 0.5D + (double) pos.getZ(), particles, 0.0D, 0.2D, 0.0D, -0.04D);
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
