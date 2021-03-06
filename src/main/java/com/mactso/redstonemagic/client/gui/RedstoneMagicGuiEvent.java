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
@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class RedstoneMagicGuiEvent extends IngameGui {

	private static Minecraft mc;
	private final int frameTextureHeight = 42, frameTextureWidth = 42;
	private final int manaTextureHeight = 38, manaTextureWidth = 38;
	private static int personalMana = 200;
	private static float alphaPulseModifier = 0.4f;
	private static float cycleDirection = 0.005F;
	private static long timerDisplayPreparedSpellOptions = 80;
	public static int fizzleSpamLimiter = 120;
	public static long timerCastingSpell = 0;
	public static String spellBeingCast = "";
	public static String spellUpPrepareOption = "";
	public static String spellDownPrepareOption = "";
	public static String guiSpellPrepared = "";
	public static String guiPreparedSpellTranslationKey = "";
	public static int guiPreparedSpellNumber = 0;
	private static int currentPlayerRedstoneMana;
	private static int currentChunkRedstoneMana;

	private final int RGB_BLACK = new Color(0, 0, 0, 160).getRGB();

	// private final ResourceLocation barx = new ResourceLocation (Main.MODID,
	// resourcePathIn:"textures/gui/rmgui.png");
	private final ResourceLocation bar = new ResourceLocation(Main.MODID, "textures/gui/rm_gui.png");

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

		if (!hasRedstoneFocusItem()) {
			return;
		}

		initPreparedSpell();

		personalMana = getCurrentPlayerRedstoneMana();

		fizzleSpamLimiter = fizzleSpamLimiter - 1;

		mc.getTextureManager().bind(bar);
		RenderSystem.pushMatrix();
		RenderSystem.pushTextureAttributes();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();

		MainWindow guiWindow = mc.getWindow();
		int guiScaledWidth = guiWindow.getGuiScaledWidth();
		int guiScaledCenterX = guiWindow.getGuiScaledWidth() / 2;
		int guiScaledHeight = guiWindow.getGuiScaledHeight();
		long gametime = mc.level.getGameTime();
		FontRenderer fontRenderer = mc.font;

		long netSpellCastingTime = 0;
		int spellCastingBarWidth = 0;



		int blitScaledWidth = 128;
		int blitScaledHeight = 128;
		int displayManaBarLeftPosX = (guiScaledWidth / 2) - 11; // confirmed location on screen.
		int displayManaBarTopPosY = (int) (guiScaledHeight * MyConfig.getGuiManaDisplayHeight()); // confirmed location

		if (personalMana < 0) {
			personalMana = 0;
		}

		int percentHelper = MyConfig.getMaxPlayerRedstoneMagic();
		int manaPercent = (100 * personalMana) / percentHelper;
		String manaPercentString = Integer.toString(manaPercent);

		float blueChannel = 1.0f;
		if (personalMana < 20) {
			blueChannel = 0.0f;
		}

		doAlphaTransparencyPulse();

		showManaBar(event, blitScaledWidth, blitScaledHeight, displayManaBarLeftPosX, displayManaBarTopPosY,
				blueChannel, manaPercent);

		showSpellCastingBar(event, gametime, guiScaledCenterX, guiScaledHeight, 256,
				256, displayManaBarTopPosY);

		RenderSystem.popAttributes();
		RenderSystem.popMatrix();

		int spellManaPercentStartX = (guiScaledWidth / 2) - (fontRenderer.width(manaPercentString) / 2);

		int spellCastTimeStartY = displayManaBarTopPosY + fontRenderer.lineHeight - 1;



		RenderSystem.blendFunc(SourceFactor.CONSTANT_COLOR, DestFactor.ONE_MINUS_DST_COLOR);
		GL11.glPushMatrix();
		MatrixStack ms = new MatrixStack();

		if ((manaPercent > 1) && (manaPercent < 97)) {
			fontRenderer.draw(ms, manaPercentString, (float) spellManaPercentStartX + 1, (float) spellCastTimeStartY + 1,
					RGB_BLACK);
			Color colourPercent = new Color(230, 160, 30, 190);
			fontRenderer.draw(ms, manaPercentString, (float) spellManaPercentStartX , (float) spellCastTimeStartY,
					colourPercent.getRGB());
		}

		showPreparedSpellInfo(ms, fontRenderer, gametime, guiScaledWidth, guiScaledHeight);

		GL11.glPopMatrix();

		// https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/3028795-1-15-2-render-overlay-blit-behaving-weirdly
		mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
	}

	private void doAlphaTransparencyPulse() {
		alphaPulseModifier = alphaPulseModifier + cycleDirection;
		if (alphaPulseModifier > 0.8) {
			cycleDirection = -0.003f;
		}
		if (alphaPulseModifier < 0.4) {
			cycleDirection = 0.003f;
		}
	}

	private void showManaBar(RenderGameOverlayEvent.Post event, int blitScaledWidth, int blitScaledHeight,
			int displayManaBarLeftPosX, int displayManaBarTopPosY, float blueChannel, int manaPercent) {
		RenderSystem.color4f(1.0F, 1.0F, blueChannel, 0.8f + alphaPulseModifier / 10.0f);
		Screen.blit(event.getMatrixStack(), displayManaBarLeftPosX, displayManaBarTopPosY, 0.0f, 0.0f, 23, 22,
				blitScaledWidth, blitScaledHeight);

		int colorValue6 = (manaPercent * 176 / 100);
		if (colorValue6 > 0)
			colorValue6 += 80;
		float redChannel = (float) (colorValue6 / 256.0);
		if (redChannel < 0.0f) {
			redChannel = 0.0f;
		}
		RenderSystem.color4f(redChannel, 1.0F, blueChannel, alphaPulseModifier + 0.05f);
		Screen.blit(event.getMatrixStack(), displayManaBarLeftPosX, displayManaBarTopPosY + 2, 0.0f, 23.0f, 24, 24,
				blitScaledWidth, blitScaledHeight);
	}

	private void showSpellCastingBar(RenderGameOverlayEvent.Post event, long gametime, int guiScaledCenterX, int guiScaledHeight,
			int blitScaledWidth, int blitScaledHeight, int displayManaBarTopPosY) {
		if (timerCastingSpell == 0) {
			return;
		}
		int spellCastingBarWidth = 0;
		if (timerCastingSpell > 0) { // XXZZY casting bar speed.
			spellCastingBarWidth = (int) ((gametime - timerCastingSpell) / 1.0f);
			if (spellCastingBarWidth > 42) {
				spellCastingBarWidth = 42;
			}
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.8F);
		int displayCastingBarTopPosY = (int) (guiScaledHeight * MyConfig.getGuiSpellCastingBarHeight());
		if (displayCastingBarTopPosY == 0.0) {
			displayCastingBarTopPosY = displayManaBarTopPosY - 7;
		}
		int halfBarWidth = (int) (spellCastingBarWidth/2);
//		Screen.blit(MatrixStack , int Screenx , int ScreenY, float PNGTop, float PNGLEFT, int PNGBottom, int PNGHeight, int ScaleX, int ScaleY);
		
		Screen.blit(event.getMatrixStack(), guiScaledCenterX - halfBarWidth - 1, displayCastingBarTopPosY,
					21 - halfBarWidth, 94, (int) spellCastingBarWidth, 10, 256, 256);


			if (guiPreparedSpellTranslationKey.equals("redstonemagic.tele")) {
				if (mc.player.xRot >= 0) {
					Screen.blit(event.getMatrixStack(), guiScaledCenterX - halfBarWidth , displayCastingBarTopPosY+2,
							21 - (halfBarWidth), 105, (int) spellCastingBarWidth, 11, 256, 256);
				} else {
					Screen.blit(event.getMatrixStack(), guiScaledCenterX - halfBarWidth , displayCastingBarTopPosY+2,
							21 - (halfBarWidth) +42, 105, (int) spellCastingBarWidth, 11, 256, 256);
				}
			
			}
	}

	private void showPreparedSpellInfo(MatrixStack ms, FontRenderer fontRender, long gametime, int guiScaledWidth,
			int guiScaledHeight) {

		final int RGB_PREPARED_SPELL = new Color(240, 90, 100, 170).getRGB();
		int lastSpellPreparedStartX = (guiScaledWidth / 2) - (fontRender.width(guiSpellPrepared) / 2) + 1;
		int lastSpellPreparedTopPosY = (int) (guiScaledHeight * MyConfig.getGuiPreparedSpellDisplayHeight()); // confirmed

		fontRender.draw(ms, guiSpellPrepared, (float) lastSpellPreparedStartX + 1, (float) lastSpellPreparedTopPosY + 1,
				RGB_BLACK);
		fontRender.draw(ms, guiSpellPrepared, (float) lastSpellPreparedStartX, (float) lastSpellPreparedTopPosY,
				RGB_PREPARED_SPELL);

		// short term display
		long elapsed_ticks = (gametime - getTimerDisplayPreparedSpellOptions());

		if (elapsed_ticks > 80) {
			setTimerDisplayPreparedSpellOptions(0);
			return;
		}

		int alpha_fade = (int) (elapsed_ticks - 30) / 2;
		if (alpha_fade < 1)
			alpha_fade = 1;
//		MyConfig.sendChat(mc.player, "alpha_fade:" + alpha_fade);
//		MyConfig.sendChat(mc.player, "elapsed_ticks" + elapsed_ticks);
		int RGB_FADING_BLACK = new Color(0, 0, 0, 150 / alpha_fade).getRGB();
		int RGB_TEMP_PREPARED_SPELL = new Color(240, 90, 100, 150 / alpha_fade).getRGB();
		int RGB_PREPARED_SPELL_OPTIONS = new Color(210, 180, 180, 150 / alpha_fade).getRGB();
		int lastSpellUpPreparedStartX = (guiScaledWidth / 2) - (fontRender.width(spellUpPrepareOption) / 2) + 1;
		int lastSpellDownPreparedStartX = (guiScaledWidth / 2) - (fontRender.width(spellDownPrepareOption) / 2) + 1;

		fontRender.draw(ms, spellUpPrepareOption, (float) lastSpellUpPreparedStartX + 1,
				(float) lastSpellPreparedTopPosY - fontRender.lineHeight - 3, RGB_FADING_BLACK);
		fontRender.draw(ms, spellUpPrepareOption, (float) lastSpellUpPreparedStartX,
				(float) lastSpellPreparedTopPosY - fontRender.lineHeight - 3, RGB_PREPARED_SPELL_OPTIONS);
		fontRender.draw(ms, spellDownPrepareOption, (float) lastSpellDownPreparedStartX + 1,
				(float) lastSpellPreparedTopPosY + fontRender.lineHeight + 3, RGB_FADING_BLACK);
		fontRender.draw(ms, spellDownPrepareOption, (float) lastSpellDownPreparedStartX,
				(float) lastSpellPreparedTopPosY + fontRender.lineHeight + 3, RGB_PREPARED_SPELL_OPTIONS);
		fontRender.draw(ms, guiSpellPrepared, (float) lastSpellPreparedStartX + 1,
				(float) (guiScaledHeight * MyConfig.getGuiManaDisplayHeight() - fontRender.lineHeight - 3),
				RGB_FADING_BLACK);
		fontRender.draw(ms, guiSpellPrepared, (float) lastSpellPreparedStartX,
				(float) (guiScaledHeight * MyConfig.getGuiManaDisplayHeight() - fontRender.lineHeight - 3),
				RGB_TEMP_PREPARED_SPELL);

	}

	public static long getTimerDisplayPreparedSpellOptions() {
		return timerDisplayPreparedSpellOptions;
	}

	public static void setTimerDisplayPreparedSpellOptions(long timerDisplayPreparedSpellOptions) {
		RedstoneMagicGuiEvent.timerDisplayPreparedSpellOptions = timerDisplayPreparedSpellOptions;
	}

	private boolean hasRedstoneFocusItem() {

		if (mc.player.inventory.offhand.get(0).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
			return true;
		} else {
			for (int i = 0; i < 9; i++) {
				if (mc.player.inventory.items.get(i).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
					return true;
				}
			}
		}
		return false;
	}

	// initialize Gui first time it is loaded.
	private void initPreparedSpell() {
		if (guiSpellPrepared.equals("")) {
			ItemStack focusStack = null;
			if (mc.player.inventory.getSelected().getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
				focusStack = mc.player.inventory.getSelected();
			}
			if (mc.player.inventory.offhand.get(0).getItem() == ModItems.REDSTONE_FOCUS_ITEM) {
				focusStack = mc.player.inventory.offhand.get(0);
			}
			if (focusStack != null) {
				CompoundNBT compoundnbt = focusStack.getOrCreateTag();
				int spellNumberKey = compoundnbt != null
						&& compoundnbt.contains("spellKeyNumber", RedstoneFocusItem.NBT_NUMBER_FIELD)
								? compoundnbt.getInt("spellKeyNumber")
								: 0;

				SpellManager.RedstoneMagicSpellItem spell = SpellManager
						.getRedstoneMagicSpellItem(Integer.toString(spellNumberKey));
				guiSpellPrepared = spell.getSpellComment();
				guiPreparedSpellNumber = spellNumberKey;
				setTimerDisplayPreparedSpellOptions(100);

			}
		}
	}

	public static void setPreparedSpellNumber(int preparedSpellNumber) {
		String currentlyPreparedSpell = guiSpellPrepared;
		if ((preparedSpellNumber >= 0) && (preparedSpellNumber <= 7)) {
			RedstoneMagicSpellItem spell = SpellManager
					.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));
			TextComponent msg = new TranslationTextComponent(spell.getSpellTranslationKey());
			guiSpellPrepared = msg.getString();
			guiPreparedSpellNumber = preparedSpellNumber;
			guiPreparedSpellTranslationKey = spell.getSpellTranslationKey();
			spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString((preparedSpellNumber + 7) % 8));
			msg = new TranslationTextComponent(spell.getSpellTranslationKey());
			spellUpPrepareOption = msg.getString();
			spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString((preparedSpellNumber + 9) % 8));
			msg = new TranslationTextComponent(spell.getSpellTranslationKey());
			spellDownPrepareOption = msg.getString();

			if (!(guiSpellPrepared.equals(currentlyPreparedSpell))) {
				timerDisplayPreparedSpellOptions = mc.level.getGameTime();
			}
			// timerCastingSpell = 0;
			spellBeingCast = "";
			// MyConfig.sendChat(mc.player, "setPreparedSpell, timer:"+ timerCastingSpell);
		}
	}

	public static void setCastPreparedSpellNumber(int castingSpellNumber) {
		if ((castingSpellNumber >= 0) && (castingSpellNumber <= 7)) {
			RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(castingSpellNumber));
			TextComponent msg = new TranslationTextComponent(spell.getSpellTranslationKey());
			spellBeingCast = msg.getString();

			timerCastingSpell = RedstoneMagicGuiEvent.mc.level.getGameTime();
//			MyConfig.sendChat(mc.player, "Setting setTimerCastingSpell:" + timerCastingSpell);
		}
	}

	public static void setSpellBeingCast(String newSpellBeingCast) {
		spellBeingCast = newSpellBeingCast;
	}

	public static String getSpellBeingCast() {
		return spellBeingCast;
	}

	public static void setSpellPrepared(String newSpellPrepared) {
		guiSpellPrepared = newSpellPrepared;
	}

	public static String getSpellPrepared() {
		return guiSpellPrepared;
	}

	public static String getSpellLeftPrepared() {
		return spellUpPrepareOption;
	}

	public static String getSpellRightPrepared() {
		return spellDownPrepareOption;
	}

	public static void setCurrentPlayerRedstoneMana(int newPlayerRedstoneMana) {
		currentPlayerRedstoneMana = newPlayerRedstoneMana;
	}

	public static int getCurrentPlayerRedstoneMana() {
		return currentPlayerRedstoneMana;
	}

	public static void setCurrentChunkRedstoneMana(int newChunkRedstoneMana) {
		currentChunkRedstoneMana = newChunkRedstoneMana;
//		MyConfig.sendChat(mc.player, "Mana Set to:" + currentChunkRedstoneMana);
	}

	public static int getCurrentChunkRedstoneMana() {
		return currentChunkRedstoneMana;
	}

	public static int getFizzleSpamLimiter() {
		return fizzleSpamLimiter;
	}

	public static void setFizzleSpamLimiter(int fizzleSpamLimiter) {
		RedstoneMagicGuiEvent.fizzleSpamLimiter = fizzleSpamLimiter;
	}

}
