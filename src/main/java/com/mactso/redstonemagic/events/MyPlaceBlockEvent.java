package com.mactso.redstonemagic.events;


import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.tileentity.GathererTileEntity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MyPlaceBlockEvent {

	@SubscribeEvent
	public void onPlaceBlock(EntityPlaceEvent event) {

		if (event.getPlacedBlock().getBlock() == ModBlocks.GATHERER) {
			LevelChunk chunk = (LevelChunk) event.getEntity().getCommandSenderWorld().getChunk(event.getPos());
			int x = 0;
			for (BlockEntity t : chunk.getBlockEntities().values()) {
				if (t instanceof GathererTileEntity) {
					x++;
					if (x > 2) {
						if (event.getLevel() instanceof ServerLevel) {
							ServerLevel serverWorld = (ServerLevel) event.getLevel();
							serverWorld.playSound(null, event.getPos(), ModSounds.SPELL_FAILS, SoundSource.AMBIENT, 0.6f, 0.8f);
					        serverWorld.sendParticles(ParticleTypes.POOF, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 3, 0.5, 0.5, 0.5, -0.14D);

						}

						event.setCanceled(true);
					}
				}
			}
		}
	}

}
