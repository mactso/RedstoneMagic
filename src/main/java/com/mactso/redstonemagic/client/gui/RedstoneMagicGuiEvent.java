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
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.item.RedstoneFocusItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.Color;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
	private static float cycleDirection = 0.01F;
	private static String castingTimeString = "*********************";
	static ItemStack REDSTONE_FOCUS_STACK = new ItemStack (ModItems.REDSTONE_FOCUS_ITEM);

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

		personalMana = MyConfig.getCurrentPlayerRedstoneMana();

		boolean hasNoFocusItem = true;

		if (mc.player.inventory.offHandInventory.get(0).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
			hasNoFocusItem = false;
		} else {
			for (int i=0;i<9;i++) {
				if (mc.player.inventory.mainInventory.get(i).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
					hasNoFocusItem = false;
					break;
				};
			}
		}
		//				mc.player.inventory.mainInventory.get(p_get_1_) 
		int debugv = 1;


		if (hasNoFocusItem) {
			return;
		}				
		mc.getTextureManager().bindTexture(bar);
		RenderSystem.pushMatrix();
		long netSpellCastingTime = 0;

		if (MyConfig.getCastTime() > 0) {
			netSpellCastingTime = (mc.world.getGameTime()- MyConfig.getCastTime())/4;
			if (netSpellCastingTime > 4) netSpellCastingTime = 4;
		}


		int texWidth = 42;
		cycle = cycle + cycleDirection;
		if (cycle>0.7) {
			cycleDirection =-0.004f;
		}
		if (cycle<0.4) {
			cycleDirection = 0.004f;
		}
		RenderSystem.pushTextureAttributes();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		MainWindow manaScaled = mc.getMainWindow();
		int manaWidth = manaScaled.getScaledWidth();
		int manaHeight = manaScaled.getScaledHeight();
		int displayScaledWidth = 128;
		int displayScaledHeight = 128;
		int displayLeftPosX = (manaWidth/2)-11; // confirmed location on screen.
		int displayTopPosY = (int) (manaHeight * 0.75f); // confirmed location on screen.
		if (manaHeight > 500) {
			displayTopPosY = (int) (manaHeight * 0.85f);
		}
	
		RenderSystem.color4f(1.0F, 1.0F, 1F, 0.7f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY, 0.0f, 0.0f, 23, 23, displayScaledWidth, displayScaledHeight);
		int colorValue16 = ((int)(personalMana+15)/16) * 16;
		if (colorValue16 >0) colorValue16 += 64;
		float redChannel = (float)(colorValue16/256.0); 
		RenderSystem.color4f(redChannel, 0.0F, 0.0F, cycle + 0.1f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY+2, 0.5f, 23.0f, 23, 23, displayScaledWidth, displayScaledHeight);
		RenderSystem.popAttributes();
		RenderSystem.popMatrix();

		FontRenderer fontRender = mc.fontRenderer;
		MainWindow scaled = mc.getMainWindow();
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
//		String personalManaString = "Personal Redstone Mana : " + Integer.toString (personalMana);
//		int personalStringWidth = fontRender.getStringWidth(personalManaString) ;
//		int chunkStringWidth = fontRender.getStringWidth(chunkManaString) ;
//		int personalStartX = (width/2) - (personalStringWidth / 2);
//		int personalStartY = (height/2) + 20;
//		String chunkManaString = "Chunk Redstone Mana : " + Integer.toString (chunkMana);
//		int chunkStartX = (width/2) - (chunkStringWidth / 2);
//		int chunkStartY = (height/2)+40;

//		String castingSpellString = "Casting Time : " + Long.toString (netSpellCastingTime);
		String castingSpellString = "";
		if (netSpellCastingTime >0) {
			castingSpellString = castingTimeString.substring (0,(int)(netSpellCastingTime*2)+1);
		}
		int spellCastingStringWidth = fontRender.getStringWidth(castingSpellString) ;
		int spellCastTimeStartX = (width/2) - (spellCastingStringWidth / 2) + 1;
		int spellCastTimeStartY = displayTopPosY + fontRender.FONT_HEIGHT;

		Color colour = new Color(250, 30, 90);
		Color colourBlack = new Color(0, 0, 0);
		RenderSystem.blendFunc(SourceFactor.CONSTANT_COLOR, DestFactor.ONE_MINUS_DST_COLOR);
		GL11.glPushMatrix();
		MatrixStack ms = new MatrixStack();
		if (MyConfig.getCastTime() > 0) {
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX+1, (float)spellCastTimeStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX, (float)spellCastTimeStartY, colour.getRGB());
		}
//		fontRender.drawString(ms, personalManaString, (float)personalStartX, (float)personalStartY, colour.getRGB());
//		fontRender.drawString(ms, chunkManaString, (float)chunkStartX, (float)chunkStartY, colour.getRGB());

		GL11.glPopMatrix();

		//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
		mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);



	}
}
