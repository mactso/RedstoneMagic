// 16.1 
package com.mactso.redstonemagic;


import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.events.ChunkEvent;
import com.mactso.redstonemagic.events.MyBreakEvent;
import com.mactso.redstonemagic.events.onServerPlayerEvent;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.network.Register;

@Mod("redstonemagic")
public class Main {

	    public static final String MODID = "redstonemagic"; 
	    
	    public Main()
	    {

			FMLJavaModLoadingContext.get().getModEventBus().register(this);
	        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,MyConfig.SERVER_SPEC );

	        //			MinecraftForge.EVENT_BUS.register(this);
			
	    }

	    @SubscribeEvent
	    public void setupCommon(final FMLCommonSetupEvent event)
	    {
	        Register.initPackets();
	    }
	    
	    @SubscribeEvent
	    public void onItemsRegistry(final RegistryEvent.Register<Item> event)
	    {
	        ModItems.register(event.getRegistry());
	    }

	    @SubscribeEvent
	    public void onBlocksRegistry(final RegistryEvent.Register<Block> event)
	    {
	    	ModBlocks.register(event.getRegistry());
	    }
	    
	    
	   // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
		public void preInit (final FMLCommonSetupEvent event) {
			System.out.println("RedStoneMagic: Registering Handler");
			MinecraftForge.EVENT_BUS.register(new onServerPlayerEvent());
			MinecraftForge.EVENT_BUS.register(new ChunkEvent());
			MinecraftForge.EVENT_BUS.register(new MyBreakEvent());			
		}       


}
