// 16.1 
package com.mactso.redstonemagic;


import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.events.ChunkEvent;
import com.mactso.redstonemagic.events.MyBreakEvent;
import com.mactso.redstonemagic.events.MyPlaceBlockEvent;
import com.mactso.redstonemagic.events.OnPlayerCloned;
import com.mactso.redstonemagic.events.OnPlayerLoggedIn;
import com.mactso.redstonemagic.events.OnServerPlayerEvent;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.item.crafting.RedstoneMagicRecipe;
import com.mactso.redstonemagic.network.Register;
import com.mactso.redstonemagic.proxy.ClientProxy;
import com.mactso.redstonemagic.proxy.IProxy;
import com.mactso.redstonemagic.proxy.ServerProxy;
import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.tileentity.ModTileEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("redstonemagic")
public class Main {	
	    public static Main instance;
	    public static final String MODID = "redstonemagic"; 
	    public static final String PREFIX_GUI = MODID +":"+"textures/gui/";
	    public static IProxy proxy = DistExecutor.safeRunForDist(()-> ClientProxy::new , ()-> ServerProxy::new );
	    
	    public Main()
	    {

			FMLJavaModLoadingContext.get().getModEventBus().register(this);
	        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,MyConfig.COMMON_SPEC );

	        //			MinecraftForge.EVENT_BUS.register(this);
	        if (FMLEnvironment.dist.equals(Dist.CLIENT)) {
	            instance = this;
	            ModLoadingContext modLoadingContext = ModLoadingContext.get();
	            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	            modEventBus.addListener(this::loadComplete);

	         }			
	    }

	    @SubscribeEvent
	    public void setupCommon(final FMLCommonSetupEvent event)
	    {
	        Register.initPackets();
	    }
	    

	    
	    
	   // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
		public void preInit (final FMLCommonSetupEvent event) {
			System.out.println("RedStoneMagic: Registering Handler");
			MinecraftForge.EVENT_BUS.register(new OnServerPlayerEvent());
			MinecraftForge.EVENT_BUS.register(new ChunkEvent());
			MinecraftForge.EVENT_BUS.register(new MyBreakEvent());
			MinecraftForge.EVENT_BUS.register(new MyPlaceBlockEvent());
			MinecraftForge.EVENT_BUS.register(new OnPlayerCloned());
			MinecraftForge.EVENT_BUS.register(new OnPlayerLoggedIn());
		}   

	    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	    public static class ModEvents
	    {

	        @SubscribeEvent
	        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event)
	        {
	        	ModSounds.register(event.getRegistry());
	        }
	        
		    @SubscribeEvent
		    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event)
		    {
				System.out.println("RedStoneMagic: Register Blocks");
		    	ModBlocks.register(event.getRegistry());
		    }
		    
	    	@SubscribeEvent
	    	public static void onItemsRegistry(final RegistryEvent.Register<Item> event)
	    	{
				System.out.println("RedStoneMagic: Register Items");
	    		ModItems.register(event.getRegistry());
	    	}

	        @OnlyIn(Dist.CLIENT)
	        @SubscribeEvent
	        public static void onColorsRegistry(final ColorHandlerEvent.Item event)
	        {
				System.out.println("RedStoneMagic: Registering Colors");
	        	ModItems.register(event.getItemColors());
	        }

	        @SubscribeEvent
	        public static void onRecipeRegistry(final RegistryEvent.Register<RecipeSerializer<?>> event)
	        {
				System.out.println("RedStoneMagic: Registering Shapeless Recipe");
	        	event.getRegistry().register(RedstoneMagicRecipe.CRAFTING_REDSTONEMAGIC);
	        }
	        
		    @SubscribeEvent
		    public static void onTileEntitiesRegistry(final RegistryEvent.Register<BlockEntityType<?>> event)
		    {
		        ModTileEntities.register(event.getRegistry());
		    }
		    

	    }

        
//		@SubscribeEvent
		public void loadComplete(FMLLoadCompleteEvent event) {
			MinecraftForge.EVENT_BUS.register(new RedstoneMagicGuiEvent (Minecraft.getInstance ()));
		}
		


}
