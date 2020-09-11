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
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
// @Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static long castTime = 0;
	public static final int NO_CHUNK_MANA_UPDATE = -1;

	
	static
	{
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}



	private static int debugLevel;
	private static int maxChunkRedstoneMagic;
	private static int maxPlayerRedstoneMagic;
	public static String[]  defaultModExclusionList;
	public static String    defaultModExclusionList6464;

	
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent)
	{
		if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC)
		{
			bakeConfig();
			ModExclusionListDataManager.excludedModsListInit();
		}
	}

	public static void pushValues() {
		COMMON.defaultModExclusionListActual.set(ModExclusionListDataManager.getExcludedModsListHashAsString());
	}

	public static void bakeConfig()
	{
		debugLevel = COMMON.debugLevel.get();
		maxChunkRedstoneMagic = COMMON.maxChunkRedstoneMagic.get();
		maxPlayerRedstoneMagic = COMMON.maxPlayerRedstoneMagic.get();
		defaultModExclusionList6464 = COMMON.defaultModExclusionListActual.get() ;
		SpellManager.redstoneMagicSpellInit();

	}


	public static class Common
	{

		public final IntValue debugLevel;
		public final IntValue maxChunkRedstoneMagic;
		public final IntValue maxPlayerRedstoneMagic;

		// blocks walls can be built on
		public final ConfigValue<String> defaultModExclusionListActual;
		public final String defaultModExclusionList6464 = 
				  "customquests;"
				+ "simplybackpacks;"
				;
		
		public Common(ForgeConfigSpec.Builder builder)
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
					.defineInRange("maxPlayerRedstoneMagic", () -> 396, 0, 511);
			builder.pop();
			
			builder.push ("Regrowth Wall Foundations 6464");
			
			defaultModExclusionListActual = builder
					.comment("Mod Exclusion List String 6464")
					.translation(Main.MODID + ".config" + "defaultModExclusionListActual")
					.define("defaultModExclusionListActual", defaultModExclusionList6464);
			builder.pop();	
		}
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
	
	public static int getDebugLevel() {
		return debugLevel;
	}

	public static void setDebugLevel(int debugLevel) {
		MyConfig.debugLevel = debugLevel;
	}

	public static int getMaxChunkRedstoneMagic() {
		return maxChunkRedstoneMagic;
	}

	public static void setMaxChunkRedstoneMagic(int maxChunkRedstoneMagic) {
		MyConfig.maxChunkRedstoneMagic = maxChunkRedstoneMagic;
	}

	public static int getMaxPlayerRedstoneMagic() {
		return maxPlayerRedstoneMagic;
	}

	public static void setMaxPlayerRedstoneMagic(int maxPlayerRedstoneMagic) {
		MyConfig.maxPlayerRedstoneMagic = maxPlayerRedstoneMagic;
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

