package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.mana.MagicProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkEvent {
	private static final ResourceLocation KEY = new ResourceLocation(Main.MODID, "magic_capability");

    @SubscribeEvent
    public void onChunk(AttachCapabilitiesEvent<LevelChunk> event)
    {
    	event.addCapability(KEY, new MagicProvider(event.getObject()));
    }
}
