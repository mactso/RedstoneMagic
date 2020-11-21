package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class GathererTileEntity extends TileEntity implements ITickableTileEntity {
	final int NO_UPDATE = -1;
	int particleCount = 1;
	float humLevel = -0.2f;
	float humCycleDirection = -.1f;
	
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
					SoundCategory.BLOCKS, humVolume, humPitch);
			doGathererParticles(3);      
			if (dayTime%6000 == 0L) {
				processGatheredManaToChunks();
			}
		}
	}

	private void processGatheredManaToChunks() {
		world.playSound(null, pos, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
				SoundCategory.BLOCKS, 0.04f, 0.05f);	
		doGathererParticles(15);                
		Chunk chunk = (Chunk)world.getChunk(pos);
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST);
		chunk = (Chunk)world.getChunk(pos.north());
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4);
		chunk = (Chunk)world.getChunk(pos.east());
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4);
		chunk = (Chunk)world.getChunk(pos.west());
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4);
		chunk = (Chunk)world.getChunk(pos.south());
		addManaToChunk(chunk,RitualPylonTileEntity.RITUAL_CHUNK_COST/4);
	}

	private void doGathererParticles(int particles) {

		((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, 0.5D+(double)this.pos.getX(), (double) this.pos.getY()+1.5D, 0.5D+(double)this.pos.getZ(), particles, 0, -1.15D, 0, 0.06D);                

		((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, 0.5D+(double)this.pos.getX(), (double) this.pos.getY()+0.35D, 0.5D+(double)this.pos.getZ(), particles, 0.0D, 0.1D, 0.0D, -0.04D);                
		((ServerWorld)world).spawnParticle(ParticleTypes.CRIMSON_SPORE, 0.5D+(double)this.pos.getX(), (double) this.pos.getY()+0.35D, 0.5D+(double)this.pos.getZ(), particles/3, 0.0D, 0.1D, 0.0D, -0.04D);
	}

	private void addManaToChunk(Chunk chunk, int newManaAmount) {
		IMagicStorage cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
		int totalMana = 0;
		if (cap != null) {
			cap.addMana(newManaAmount); // checks for max capacity internally based on object type.
		}
	}
}
