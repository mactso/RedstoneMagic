package com.mactso.redstonemagic.client.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.FireworkParticle.Overlay;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.Color;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;



//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
// @OnlyIn(value=Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class RedstoneMagicGuiEvent extends IngameGui {
	
	   private static Minecraft mc;
	   
	   private final int frameTextureHeight = 42, frameTextureWidth = 42;
	   private final int manaTextureHeight = 38, manaTextureWidth = 38;

	   private static int personalMana = 200;
	   private static int chunkMana = 23714;
	   private static float cycle;
	    
//	   private final ResourceLocation barx = new ResourceLocation (Main.MODID, resourcePathIn:"textures/gui/rmgui.png");
	   private final ResourceLocation bar = new ResourceLocation (Main.MODID, "textures/gui/rm_gui.png");
	   
	   public RedstoneMagicGuiEvent(Minecraft mc) {
	      super(mc);
	      RedstoneMagicGuiEvent.mc = mc;
	   }
	   

	   @SubscribeEvent(priority = EventPriority.NORMAL)
	   public void renderOverlay(RenderGameOverlayEvent.Post event) {

		   ElementType type = event.getType();
		   if (type != ElementType.ALL) {
			   return;
		   }

           PlayerEntity clientPlayer = mc.player;
   		   LazyOptional<IMagicStorage> optPlayer = clientPlayer.getCapability(CapabilityMagic.MAGIC);
   		   if (optPlayer.isPresent()) {
   	   		   IMagicStorage playerManaStorage = optPlayer.orElseGet(null);
   			   personalMana = playerManaStorage.getManaStored(); // checks for max capacity internally based on object type.
   		   }

	        mc.getTextureManager().bindTexture(bar);
	        RenderSystem.pushMatrix();
	        long netSpellCastingTime = 0;
	        
	        if (MyConfig.getCastTime() > 0) {
		        netSpellCastingTime = (mc.world.getGameTime()- MyConfig.getCastTime())/4;
		        if (netSpellCastingTime > 4) netSpellCastingTime = 4;
	        }
	        
	        int texWidth = 42;
	        RenderSystem.pushTextureAttributes();
	        RenderSystem.enableAlphaTest();
	        RenderSystem.enableBlend();

	        int displayScaledWidth = 128;
	        int displayScaledHeight = 128;
			int displayLeftPosX = 32; // confirmed location on screen.
	        int displayTopPosY = 32; // confirmed location on screen.
	        RenderSystem.color4f(1.0F, 1.0F, 1F, 0.5F);
	        Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY, 0.0f, 0.0f, 21, 21, displayScaledWidth, displayScaledHeight);
	        float redChannel = (float) (1.0f - (1.0f/personalMana)); 
	        RenderSystem.color4f(redChannel, 0.0F, 0.0F, 0.8F);
	        Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY, 0.5f, 21.0f, 21, 21, displayScaledWidth, displayScaledHeight);
	        RenderSystem.popAttributes();
			RenderSystem.popMatrix();

//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
	        mc.getTextureManager().bindTexture(bar);			
		   
		   
 	       FontRenderer fontRender = mc.fontRenderer;
           MainWindow scaled = mc.getMainWindow();
  		   
           int width = scaled.getScaledWidth();
           int height = scaled.getScaledHeight();
           String personalManaString = "Personal Redstone Mana : " + Integer.toString (personalMana);
           String castingSpellString = "Casting Time : " + Long.toString (netSpellCastingTime);
           String chunkManaString = "Chunk Redstone Mana : " + Integer.toString (chunkMana);
           
           int personalStringWidth = fontRender.getStringWidth(personalManaString) ;
           int spellCastingStringWidth = fontRender.getStringWidth(castingSpellString) ;
           int chunkStringWidth = fontRender.getStringWidth(chunkManaString) ;
           int spellCastTimeStartX = (width/2) - (spellCastingStringWidth / 2);
           int spellCastTimeStartY = (height/2) + 0;
           int personalStartX = (width/2) - (personalStringWidth / 2);
           int personalStartY = (height/2) + 20;
           int chunkStartX = (width/2) - (chunkStringWidth / 2);
           int chunkStartY = (height/2)+40;
           Color colour = new Color(250, 40, 40);
           GL11.glPushMatrix();
           MatrixStack ms = new MatrixStack();
	       if (MyConfig.getCastTime() > 0) {
	           fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX, (float)spellCastTimeStartY, colour.getRGB());
		   }
    	   fontRender.drawString(ms, personalManaString, (float)personalStartX, (float)personalStartY, colour.getRGB());
	       fontRender.drawString(ms, chunkManaString, (float)chunkStartX, (float)chunkStartY, colour.getRGB());

           GL11.glPopMatrix();

	   }
}
