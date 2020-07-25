// 16.1 
package com.mactso.redstonemagic;


import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.events.ChunkEvent;
import com.mactso.redstonemagic.events.MyBreakEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("redstonemagic")
public class Main {

	    public static final String MODID = "redstonemagic"; 
	    
	    public Main()
	    {

			FMLJavaModLoadingContext.get().getModEventBus().register(this);
	        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,MyConfig.SERVER_SPEC );
			MinecraftForge.EVENT_BUS.register(this);
	    }

	   // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
		public void preInit (final FMLCommonSetupEvent event) {
			System.out.println("RedStoneMagic: Registering Handler");
			MinecraftForge.EVENT_BUS.register(new ChunkEvent());
			MinecraftForge.EVENT_BUS.register(new MyBreakEvent());			
		}       

		// in 14.4 and later, config file loads when the server starts when the world starts.
		@SubscribeEvent 
		public void onServerStarting (FMLServerStartingEvent event) {
//			VillagerRespawnCommands.register(event.getCommandDispatcher());
		}
}
