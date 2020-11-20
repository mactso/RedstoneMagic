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

public class RedstoneMagicGathererTileEntity extends TileEntity implements ITickableTileEntity {
	final int NO_UPDATE = -1;
	int particleCount = 1;
	
	public RedstoneMagicGathererTileEntity() {
		super(ModTileEntities.REDSTONE_MAGIC_GATHERER);
	}
	
	@Override
	public void tick() {
		if (world == null) {
			return;
		}
		long dayTime = world.getDayTime();
		if (dayTime % 20 == 0) {
			float humVolume = (float) (0.05 + (0.04 * Math.sin((double) dayTime)));
			float humPitch = (float) (0.5 + (0.3 * Math.sin((double) dayTime)));
			world.playSound(null, pos, ModSounds.GATHERER_HUMS,
					SoundCategory.BLOCKS, humVolume, humPitch);
			double xV = Math.sin(dayTime);
			double zV = Math.cos(dayTime);
			if (world instanceof ServerWorld) {
				((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, 0.5D+(double)this.pos.getX(), (double) this.pos.getY()+0.5D, 0.5D+(double)this.pos.getZ(), particleCount, Math.sin(dayTime), -0.15D, Math.cos(dayTime), -0.04D);                
			}

		}
		
		if (world != null && !this.world.isRemote && dayTime%24000 == 9000L) {
			Chunk chunk = (Chunk)world.getChunk(pos);
			IMagicStorage cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			boolean manaChanged = false;
			if (cap != null) {
				int chunkMana = -1;
				int redstoneMagicIncrease = 3;
				cap.addMana(redstoneMagicIncrease); // checks for max capacity internally based on object type.
				chunkMana = cap.getManaStored();
				particleCount = (int) Math.min(Math.sqrt(chunkMana/10),13.0);
				float gatherSoundVolume = Math.min(0.25f + 0.1f*(chunkMana /100 ),0.65f);
				world.playSound(null, pos, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
						SoundCategory.BLOCKS, 0.04f, 0.05f);
		        ((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, 0.5D+(double)this.pos.getX(), (double) this.pos.getY()+0.35D, 0.5D+(double)this.pos.getZ(), particleCount*3, 0.0D, 0.1D, 0.0D, -0.04D);                
		        Network.sendToTarget(PacketDistributor.TRACKING_CHUNK.with(()->chunk),new SyncClientManaPacket(NO_UPDATE , chunkMana));
			}
		}
	}
}
