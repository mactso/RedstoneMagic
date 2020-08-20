package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.mana.MagicProvider;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkEvent {

    @SubscribeEvent
    public void onChunk(AttachCapabilitiesEvent<Chunk> event)
    {
    	event.addCapability(new ResourceLocation(Main.MODID, "magic_capability"), new MagicProvider(event.getObject()));
    }
}
