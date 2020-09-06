package com.mactso.redstonemagic.client.gui;

import java.awt.Color;


import org.lwjgl.opengl.GL11;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.item.RedstoneFocusItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
	private static int timerDisplayPreparedSpell = 40;
	public static long timerCastingSpell = 0;
	public static String spellBeingCast = "";
	public static String spellPrepared = "";
	private static int currentPlayerRedstoneMana;
	private static int currentChunkRedstoneMana;

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

		personalMana = getCurrentPlayerRedstoneMana();
		
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
		if (hasNoFocusItem) {
			return;
		}

		if (spellPrepared.equals("")) {
			ItemStack focusStack = null;
			if (mc.player.inventory.getCurrentItem().getItem() == ModItems.REDSTONE_FOCUS_ITEM)  {
				focusStack = mc.player.inventory.getCurrentItem();
			}
			if (mc.player.inventory.offHandInventory.get(0).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
				focusStack = mc.player.inventory.offHandInventory.get(0);
			}
			if (focusStack != null) {
				CompoundNBT compoundnbt = focusStack.getOrCreateTag();
				int spellNumberKey = compoundnbt != null && compoundnbt.contains("spellKeyNumber", RedstoneFocusItem.NBT_NUMBER_FIELD)
						? compoundnbt.getInt("spellKeyNumber")
						: 0;
				SpellManager.RedstoneMagicSpellItem spell = SpellManager
						.getRedstoneMagicSpellItem(Integer.toString(spellNumberKey));
				spellPrepared = spell.getSpellComment();
			}
		}

				
		mc.getTextureManager().bindTexture(bar);
		RenderSystem.pushMatrix();
		long netSpellCastingTime = 0;

		if (timerCastingSpell> 0) {
			netSpellCastingTime = (mc.world.getGameTime()- timerCastingSpell +5)/10;
			if (netSpellCastingTime > 4) netSpellCastingTime = 4;

		}

		int texWidth = 42;
		flashCycle = flashCycle + 1;
		if (flashCycle > 21) {
			flashCycle = 1;
		}
		cycle = cycle + cycleDirection;
		if (cycle>0.9) {
			cycleDirection =-0.005f;
		}
		if (cycle<0.3) {
			cycleDirection = 0.005f;
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
		int displayManaBarTopPosY = (int) (manaHeight * 0.65f); // confirmed location on screen.
		if (manaHeight > 300) {
			displayManaBarTopPosY = (int) (manaHeight * 0.75f);
		}
		int lastSpellPreparedTopPosY = (int) (manaHeight * 0.12f); // confirmed location on screen.

		float blueChannel = 1.0f;
		if (personalMana < 0) {
			personalMana = 0;
		}
		
		int manaPercent = (100 * personalMana ) / MyConfig.getMaxPlayerRedstoneMagic();
		String manaPercentString = Integer.toString(manaPercent);
		
		if (personalMana < 10) {
			blueChannel = 0.0f;
		}
		RenderSystem.color4f(1.0F, 1.0F, blueChannel, 0.9f + cycle/10.0f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayManaBarTopPosY, 0.0f, 0.0f, 23, 22, displayScaledWidth, displayScaledHeight);
		int colorValue6 = ((int)(personalMana+5)/5) * 5;
		if (colorValue6 >0) colorValue6 += 80;
		float redChannel = (float)(colorValue6/256.0); 
		if (redChannel < 0.0f) {
			redChannel = 0.0f;
		}
		RenderSystem.color4f(redChannel, 1.0F, blueChannel, cycle + 0.05f);
		Screen.blit(event.getMatrixStack(), displayLeftPosX, displayManaBarTopPosY+2, 0.5f, 23.0f, 23, 22, displayScaledWidth, displayScaledHeight);
		RenderSystem.popAttributes();
		RenderSystem.popMatrix();

		FontRenderer fontRender = mc.fontRenderer;
		MainWindow scaled = mc.getMainWindow();
		int width = scaled.getScaledWidth() - 1;
		int height = scaled.getScaledHeight();
		String castingSpellString = ""; // xxxxxxxx
		if (netSpellCastingTime >0) castingSpellString = castingTimeString.substring (0,(int)(netSpellCastingTime*2)+1);
		int spellPreparedStartX = (width/2) - (fontRender.getStringWidth(RedstoneMagicGuiEvent.getSpellPrepared())  / 2) + 1;
		int spellManaPercentStartX = (width/2) - (fontRender.getStringWidth(manaPercentString)  / 2);
		int spellCastTimeStartX = (width/2) - (fontRender.getStringWidth(castingSpellString)  / 2);
		int spellCastTimeStartY = displayManaBarTopPosY + fontRender.FONT_HEIGHT -1;
		int spellBeingCastStartX = (width/2) - (fontRender.getStringWidth(RedstoneMagicGuiEvent.getSpellBeingCast()) / 2) + 1;
		int spellBeingCastStartY = displayManaBarTopPosY - fontRender.FONT_HEIGHT*3;
		int redChannelr = 30 + (int) (netSpellCastingTime * 30);
		int spellManaPercentStartY = displayManaBarTopPosY;

		redChannelr = redChannelr + 90;
		int greenChannelg = 160 - redChannelr;
		if (redChannelr > 255) redChannelr = 255;
		if (greenChannelg < 0) greenChannelg = 0;
		Color color = new Color(redChannelr, greenChannelg, 100);
		String spellBeingCast = "";
		if (netSpellCastingTime > 0) spellBeingCast = RedstoneMagicGuiEvent.getSpellBeingCast();
		if (netSpellCastingTime == 4) {
			color = new Color(250, 40 , 40);
			if (flashCycle >10) {
				color = new Color(250, 150 , 150);
			}

		}

		Color colourBlack = new Color(0, 0, 0, 160);

		Color colourPrepared = new Color(230, 90, 100, 110);
		int lastSpellPreparedStartX = (width/2) - (fontRender.getStringWidth(spellPrepared)  / 2) + 1;
		
		RenderSystem.blendFunc(SourceFactor.CONSTANT_COLOR, DestFactor.ONE_MINUS_DST_COLOR);
		GL11.glPushMatrix();
		MatrixStack ms = new MatrixStack();
		if (!(spellBeingCast.equals(""))) {
			fontRender.drawString(ms, spellBeingCast, (float)spellBeingCastStartX+1, (float)spellBeingCastStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, spellBeingCast, (float)spellBeingCastStartX, (float)spellBeingCastStartY, color.getRGB());
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX+2, (float)spellCastTimeStartY+2, colourBlack.getRGB());
			fontRender.drawString(ms, castingSpellString , (float)spellCastTimeStartX+1, (float)spellCastTimeStartY+1, color.getRGB());
			timerDisplayPreparedSpell = 0;
		} else {
			if ((manaPercent >3) && (manaPercent<97 )) {
				fontRender.drawString(ms, manaPercentString, (float)spellManaPercentStartX+2, (float)spellCastTimeStartY+1, colourBlack.getRGB());
				Color colourPercent = new Color(230, 160, 30, 190);
				fontRender.drawString(ms, manaPercentString, (float)spellManaPercentStartX+1, (float)spellCastTimeStartY, colourPercent.getRGB());
			}
		}
		
		if (timerDisplayPreparedSpell > 0) {
			timerDisplayPreparedSpell--;
			fontRender.drawString(ms, spellPrepared, (float)spellPreparedStartX+1, (float)spellBeingCastStartY+1, colourBlack.getRGB());
			fontRender.drawString(ms, spellPrepared, (float)spellPreparedStartX, (float)spellBeingCastStartY, colourPrepared.getRGB());
		} 
		
		fontRender.drawString(ms, spellPrepared, (float)lastSpellPreparedStartX+1, (float)lastSpellPreparedTopPosY+1, colourBlack.getRGB());
		fontRender.drawString(ms, spellPrepared, (float)lastSpellPreparedStartX, (float)lastSpellPreparedTopPosY, colourPrepared.getRGB());

		
		GL11.glPopMatrix();

		//https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly		
		mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
	}

	public static void setPreparedSpellNumber (int preparedSpellNumber) {
		if ((preparedSpellNumber >=0) &&
		    (preparedSpellNumber <=7)) { 
			RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));
			TextComponent msg = new TranslationTextComponent(spell.getSpellTranslationKey());
			spellPrepared = msg.getString();
			timerDisplayPreparedSpell = 80;
			timerCastingSpell = 0;
			spellBeingCast = "";
		}
	}
	
	public static void setCastPreparedSpellNumber (int castingSpellNumber) {
		if ((castingSpellNumber >=0) &&
			    (castingSpellNumber <=7)) { 
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(castingSpellNumber));
				TextComponent msg = new TranslationTextComponent(spell.getSpellTranslationKey());
				spellBeingCast= msg.getString();
				timerDisplayPreparedSpell = 0;
				timerCastingSpell = RedstoneMagicGuiEvent.mc.world.getGameTime();
			}		
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
