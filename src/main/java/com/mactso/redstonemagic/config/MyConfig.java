package com.mactso.redstonemagic.config;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
// @Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	public static long castTime = 0;
	public static String spellBeingCast = "";
	public static String spellPrepared = "";
	
	static
	{
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static int debugLevel;
	public static int maxChunkRedstoneMagic;
	public static int maxPlayerRedstoneMagic;
	private static int currentPlayerRedstoneMana;
	private static int currentChunkRedstoneMana;
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent)
	{
		if (configEvent.getConfig().getSpec() == MyConfig.SERVER_SPEC)
		{
			bakeConfig();
		}
	}

	public static void bakeConfig()
	{
		debugLevel = SERVER.debugLevel.get();
		maxChunkRedstoneMagic = SERVER.maxChunkRedstoneMagic.get();
		maxPlayerRedstoneMagic = SERVER.maxPlayerRedstoneMagic.get();
		SpellManager.redstoneMagicSpellInit();

	}


	public static class Server
	{

		public final IntValue debugLevel;
		public final IntValue maxChunkRedstoneMagic;
		public final IntValue maxPlayerRedstoneMagic;

		
		public Server(ForgeConfigSpec.Builder builder)
		{
			builder.push("Redstone Magic Control Values");

			debugLevel = builder
					.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel")
					.defineInRange("debugLevel", () -> 0, 0, 2);
			
			maxChunkRedstoneMagic = builder
					.comment("Max Chunk Redstone Magic Amount")
					.translation(Main.MODID + ".config." + "maxChunkRedstoneMagic")
					.defineInRange("maxChunkRedstoneMagic", () -> 256000, 1, 256000);

			maxPlayerRedstoneMagic = builder
					.comment("Max Player Redstone Magic Amount")
					.translation(Main.MODID + ".config." + "maxPlayerRedstoneMagic")
					.defineInRange("maxPlayerRedstoneMagic", () -> 255, 0, 511);
			builder.pop();
			
		}
	}

	public static  long getCastTime () {
		return castTime;
	}

	public static void setCastTime (long newCastTime) {
		castTime = newCastTime;
	}
	
	// support for debug messages
	public static void dbgPrintln(int dbgLevel, String dbgMsg) {
		if (dbgLevel <= debugLevel) {
			System.out.println (dbgMsg);
		}
	}
	public static void dbgPrintln(PlayerEntity p, String dbgMsg, int dbgLevel) {
		if (dbgLevel <= debugLevel ) {
			sendChat (p, dbgMsg, Color.func_240744_a_(TextFormatting.YELLOW));
		}
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
	
	public static void setSpellBeingCast(String newSpellBeingCast) {
		spellBeingCast = newSpellBeingCast;
 	}
	public static String getSpellBeingCast() {
		return spellBeingCast;
	}

	public static void setSpellPrepared(String newSpellPrepared) {
		spellPrepared = newSpellPrepared;
 	}
	public static String getSpellPrepared() {
		return spellPrepared;
	}
	
	// support for any color chattext
	public static void sendChat(PlayerEntity p, String chatMessage, Color color) {
		StringTextComponent component = new StringTextComponent (chatMessage);
		component.getStyle().setColor(color);
		p.sendMessage(component, p.getUniqueID());
	}
	
	// support for any color, optionally bold text.
	public static void sendBoldChat(PlayerEntity p, String chatMessage, Color color) {
		StringTextComponent component = new StringTextComponent (chatMessage);

		component.getStyle().setBold(true);
		component.getStyle().setColor(color);
		
		p.sendMessage(component, p.getUniqueID());
	}
	
}

