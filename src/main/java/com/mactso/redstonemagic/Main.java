// 16.1 
package com.mactso.redstonemagic;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

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
		    public static void onRegister(final RegisterEvent event)
		    {
		    	@Nullable
				IForgeRegistry<Object> fr = event.getForgeRegistry();
		    	
		    	@NotNull
				ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
		    	if (key.equals(ForgeRegistries.Keys.BLOCKS))
		    		ModBlocks.register(event.getForgeRegistry());
		    	else if (key.equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
		    		ModTileEntities.register(event.getForgeRegistry());
		    	else if (key.equals(ForgeRegistries.Keys.ITEMS))
		    		ModItems.register(event.getForgeRegistry());
		    	else if (key.equals(ForgeRegistries.Keys.SOUND_EVENTS))
		    		ModSounds.register(event.getForgeRegistry());
		    	else if (key.equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
			    		event.getForgeRegistry().register(RedstoneMagicRecipe.NAME, RedstoneMagicRecipe.SERIALIZER);		    		
		    	
		    }

	        @OnlyIn(Dist.CLIENT)
	        @SubscribeEvent
	        public static void onColorsRegistry(final RegisterColorHandlersEvent.Item event)
	        {
	        	ModItems.register(event);
	        }

	    }

        
//		@SubscribeEvent
		public void loadComplete(FMLLoadCompleteEvent event) {
			MinecraftForge.EVENT_BUS.register(new RedstoneMagicGuiEvent (Minecraft.getInstance ()));
		}
		


}
