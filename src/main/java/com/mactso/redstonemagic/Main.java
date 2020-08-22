// 16.1 
package com.mactso.redstonemagic;


import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.events.*;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.network.Register;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod("redstonemagic")
public class Main {
	    public static Main instance;
	    public static final String MODID = "redstonemagic"; 
	    public static final String PREFIX_GUI = MODID +":"+"textures/gui/";
		private static int currentPlayerRedstoneMana;
		private static int currentChunkRedstoneMana;
	    
	    public Main()
	    {

			FMLJavaModLoadingContext.get().getModEventBus().register(this);
	        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,MyConfig.SERVER_SPEC );

	        //			MinecraftForge.EVENT_BUS.register(this);
	        if (FMLEnvironment.dist.equals(Dist.CLIENT)) {
	            instance = this;
	            ModLoadingContext modLoadingContext = ModLoadingContext.get();
	            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	            modEventBus.addListener(this::loadComplete);
//	            modLoadingContext.registerConfig(Type.COMMON, ConfigHandler.spec);
	         }			
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
			MinecraftForge.EVENT_BUS.register(new OnServerPlayerEvent());
			MinecraftForge.EVENT_BUS.register(new ChunkEvent());
			MinecraftForge.EVENT_BUS.register(new MyBreakEvent());
			MinecraftForge.EVENT_BUS.register(new OnPlayerCloned());
			MinecraftForge.EVENT_BUS.register(new OnPlayerLoggedIn());
		}       

//		@SubscribeEvent
		public void loadComplete(FMLLoadCompleteEvent event) {
			MinecraftForge.EVENT_BUS.register(new RedstoneMagicGuiEvent (Minecraft.getInstance ()));
		}
		
		public static void setCurrentPlayerRedstoneMana(int newPlayerRedstoneMana) {
			currentPlayerRedstoneMana = newPlayerRedstoneMana;
	 	}
		public static int getCurrentPlayerRedstoneMana() {
			return currentPlayerRedstoneMana;
		}

		public static void setCurrentChunkRedstoneMana(int newChunkRedstoneMana) {
			currentChunkRedstoneMana = newChunkRedstoneMana;
	 	}
		public static int getCurrentChunkRedstoneMana() {
			return currentChunkRedstoneMana;
		}

}
