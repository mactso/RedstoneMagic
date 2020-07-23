package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MyBreakEvent {

    @SubscribeEvent
    public void onBlockBreak(BreakEvent event)
    {
    	BlockState state = event.getState();
    	Block block = state.getBlock();
    	if (block == Blocks.REDSTONE_ORE && event.getExpToDrop() > 0)
    	{
    		BlockPos pos = event.getPos();
    		IWorld world = event.getWorld();
    		IChunk ichunk = world.getChunk(pos);
    		if (ichunk instanceof Chunk)
    		{
    			Chunk chunk = (Chunk) ichunk;
    			LazyOptional<IMagicStorage> opt = chunk.getCapability(CapabilityMagic.MAGIC);
    			if (opt.isPresent())
    			{
    				IMagicStorage cap = opt.orElseGet(null);
    				cap.addMagic(5);
    			}
    		}
    	}
    }

}
