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
import com.mactso.redstonemagic.util.helpers.KeyboardHelper;
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
//https://github.com/KurodaAkira/RPG-Hud/tree/MC-Forge-1.16.1/src/main/java/net/spellcraftgaming/rpghud
//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
@OnlyIn(value=Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class RedstoneMagicGuiEvent extends IngameGui {

	private static Minecraft mc;
	private final int frameTextureHeight = 42, frameTextureWidth = 42;
	private final int manaTextureHeight = 38, manaTextureWidth = 38;
	private static int personalMana = 200;
	private static int chunkMana = 23714;
	private static float cycle=0.3f;
	private static int flashCycle;
	private static float cycleDirection = 0.01F;
	private static String castingTimeString = "*********************";
	private static ItemStack REDSTONE_FOCUS_STACK = new ItemStack (ModItems.REDSTONE_FOCUS_ITEM);
	private static String lastSpellPrepared = "";
	private static int timerSpellPreparedDisplay = 40;
	public static long castTime = 0;
	public static String spellBeingCast = "";
	public static String spellPrepared = "";


	//	   private final ResourceLocation barx = new ResourceLocation (Main.MODID, resourcePathIn:"textures/gui/rmgui.png");
	private final ResourceLocation bar = new ResourceLocation (Main.MODID, "textures/gui/rm_gui.png");

	public RedstoneMagicGuiEvent(Minecraft mc) {
		super(mc);
		RedstoneMagicGuiEvent.mc = mc;
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void renderOverlay(RenderGameOverlayEvent.Post event) {

		ElementType type = event.getType();
		if (type != ElementType.ALL) {
			return;
		}

		personalMana = Main.getCurrentPlayerRedstoneMana();
		
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

		if (RedstoneMagicGuiEvent.getCastTime() > 0) {
			netSpellCastingTime = (mc.world.getGameTime()- RedstoneMagicGuiEvent.getCastTime()+5)/10;
			if (netSpellCastingTime > 4) netSpellCastingTime = 4;

		}


		int texWidth = 42;
		flashCycle = flashCycle + 1;
		if (flashCycle > 21) {
			flashCycle = 1;
		}
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
		int lastSpellPreparedTopPosY = (int) (manaHeight * 0.05f); // confirmed location on screen.

		if (manaHeight > 500) {
			displayTopPosY = (int) (manaHeight * 0.85f);
		}
	
		RenderSystem.color4f(1.0F, 1.0F, 1F, 0.7f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY, 0.0f, 0.0f, 23, 23, displayScaledWidth, displayScaledHeight);
		int colorValue6 = ((int)(personalMana+5)/5) * 5;
		if (colorValue6 >0) colorValue6 += 80;
		float redChannel = (float)(colorValue6/256.0); 
		RenderSystem.color4f(redChannel, 0.0F, 0.0F, cycle + 0.1f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayTopPosY+2, 0.5f, 23.0f, 23, 23, displayScaledWidth, displayScaledHeight);
		RenderSystem.popAttributes();
		RenderSystem.popMatrix();

		FontRenderer fontRender = mc.fontRenderer;
		MainWindow scaled = mc.getMainWindow();
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
		String castingSpellString = ""; // xxxxxxxx
		if (netSpellCastingTime >0) castingSpellString = castingTimeString.substring (0,(int)(netSpellCastingTime*2)+1);
		int spellPreparedStartX = (width/2) - (fontRender.getStringWidth(RedstoneMagicGuiEvent.getSpellPrepared())  / 2) + 1;
		int spellCastTimeStartX = (width/2) - (fontRender.getStringWidth(castingSpellString)  / 2) + 1;
		int spellCastTimeStartY = displayTopPosY + fontRender.FONT_HEIGHT;
		int spellBeingCastStartX = (width/2) - (fontRender.getStringWidth(RedstoneMagicGuiEvent.getSpellBeingCast()) / 2) + 1;
		int spellBeingCastStartY = displayTopPosY - fontRender.FONT_HEIGHT*3;
		int redChannelr = 30 + (int) (netSpellCastingTime * 30);
		Color color = new Color(redChannelr + 90, 160-redChannelr , 100);
		String spellBeingCast = "";
		if (netSpellCastingTime > 0) spellBeingCast = RedstoneMagicGuiEvent.getSpellBeingCast();
		if (netSpellCastingTime == 4) {
			color = new Color(250, 40 , 40);
			if (flashCycle >10) {
				color = new Color(250, 150 , 150);
			}

		}

		Color colourBlack = new Color(0, 0, 0);
		if (!(lastSpellPrepared.equals(RedstoneMagicGuiEvent.getSpellPrepared()))) {
			lastSpellPrepared = RedstoneMagicGuiEvent.getSpellPrepared();
			timerSpellPreparedDisplay = 80;
		}
		Color colourPrepared = new Color(230, 80, 100);
		int lastSpellPreparedStartX = (width/2) - (fontRender.getStringWidth(RedstoneMagicGuiEvent.lastSpellPrepared)  / 2) + 1;
		
		RenderSystem.blendFunc(SourceFactor.CONSTANT_COLOR, DestFactor.ONE_MINUS_DST_COLOR);
		GL11.glPushMatrix();
		MatrixStack ms = new MatrixStack();
		if (RedstoneMagicGuiEvent.getCastTime() > 0) {
			fontRender.drawString(ms, spellBeingCast, (float)spellBeingCastStartX+1, (float)spellBeingCastStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, spellBeingCast, (float)spellBeingCastStartX, (float)spellBeingCastStartY, color.getRGB());
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX+1, (float)spellCastTimeStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX, (float)spellCastTimeStartY, color.getRGB());
			timerSpellPreparedDisplay = 0;
		}
		
		if (timerSpellPreparedDisplay > 0) {
			timerSpellPreparedDisplay--;
			fontRender.drawString(ms, spellPrepared, (float)spellPreparedStartX+1, (float)spellBeingCastStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, spellPrepared, (float)spellPreparedStartX, (float)spellBeingCastStartY, colourPrepared.getRGB());
		}
		
		fontRender.drawString(ms, lastSpellPrepared, (float)lastSpellPreparedStartX+1, (float)lastSpellPreparedTopPosY+1, colourBlack.getRGB());
		fontRender.drawString(ms, lastSpellPrepared, (float)lastSpellPreparedStartX, (float)lastSpellPreparedTopPosY, colourPrepared.getRGB());

		
		GL11.glPopMatrix();

		//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
		mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
	}
	
	public static  long getCastTime () {
		return castTime;
	}

	public static void setCastTime (long newCastTime) {
		castTime = newCastTime;
	}
	
	
	public static void setSpellBeingCast(String newSpellBeingCast) {
		spellBeingCast = newSpellBeingCast;
 	}
	public static String getSpellBeingCast() {
		return spellBeingCast;
	}

	public static void setSpellPrepared(String newSpellPrepared) {
		MyConfig.dbgPrintln(1, "prepared " + newSpellPrepared + "." );
		spellPrepared = newSpellPrepared;
 	}
	public static String getSpellPrepared() {
		return spellPrepared;
	}

}
