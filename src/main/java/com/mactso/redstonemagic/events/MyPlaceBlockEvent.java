package com.mactso.redstonemagic.events;


import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.spells.CastSpells;
import com.mactso.redstonemagic.tileentity.GathererTileEntity;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MyPlaceBlockEvent {

	@SubscribeEvent
	public void onPlaceBlock(EntityPlaceEvent event) {

		if (event.getPlacedBlock().getBlock() == ModBlocks.GATHERER) {
			Chunk chunk = (Chunk) event.getEntity().getEntityWorld().getChunk(event.getPos());
			int x = 0;
			for (TileEntity t : chunk.getTileEntityMap().values()) {
				if (t instanceof GathererTileEntity) {
					x++;
					if (x > 2) {
						if (event.getWorld() instanceof ServerWorld) {
							ServerWorld serverWorld = (ServerWorld) event.getWorld();
							serverWorld.playSound(null, event.getPos(), ModSounds.SPELL_FAILS, SoundCategory.AMBIENT, 0.6f, 0.8f);
					        serverWorld.spawnParticle(ParticleTypes.POOF, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 3, 0.5, 0.5, 0.5, -0.14D);

						}

						event.setCanceled(true);
					}
				}
			}
		}
	}

}
