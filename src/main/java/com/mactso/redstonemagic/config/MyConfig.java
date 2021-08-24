package com.mactso.redstonemagic.config;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.redstonemagic.Main;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
// @Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig
{
	public static class Common
	{

		public final IntValue debugLevel;
		public final DoubleValue wakeupManaRegenPercent;
		public final DoubleValue guiPreparedSpellDisplayHeight;
		public final DoubleValue guiSpellCastingBarHeight;
		public final DoubleValue guiManaDisplayHeight;
		public final BooleanValue guiChatSpamFilter;
		public final IntValue neverBreakTools;
		public final IntValue flightTime;
		public final IntValue maxFlightSpeed;
		public final IntValue maxChunkRedstoneMagic;
		public final IntValue maxPlayerRedstoneMagic;

		// items that block use of redstonefocus.
		public final ConfigValue<String> defaultModExclusionListActual;
		public final String defaultModExclusionList6464 = 
				  "customquests;"
				+ "simplybackpacks;"
				+ "storagenetwork;"
				+ "waystones;"
				;

		public Common(ForgeConfigSpec.Builder builder)
		{
			builder.push("Redstone Magic Control Values");

			debugLevel = builder
					.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel")
					.defineInRange("debugLevel", () -> 0, 0, 2);

			wakeupManaRegenPercent = builder
					.comment("GUI wakeupManaRegenPercent: 0 = Off, 0.001-10% = % to add on waking")
					.translation(Main.MODID + ".config." + "wakeupManaRegenPercent")
					.defineInRange("wakeupManaRegenPercent", () -> 5.0, 0.0, 100.0);

			guiPreparedSpellDisplayHeight = builder
					.comment("guiPreparedSpellDisplayHeight: 8%, 0.0-50% ")
					.translation(Main.MODID + ".config." + "guiPreparedSpellDisplayHeight")
					.defineInRange("guiPreparedSpellDisplayHeight", () -> 12.0, 0.0, 50.0);

			guiSpellCastingBarHeight = builder
					.comment("guiSpellCastingBarHeight: 70%, 5.0-75%")
					.translation(Main.MODID + ".config." + "guiSpellCastingBarHeight")
					.defineInRange("guiSpellCastingBarHeight", () -> 62.0,  5.0, 75.0);

			guiManaDisplayHeight = builder
					.comment("guiManaDisplayHeight: 80%, 0.0-80%")
					.translation(Main.MODID + ".config." + "guiManaDisplayHeight")
					.defineInRange("guiManaDisplayHeight", () -> 65.0, 0.0, 80.0);
			
			guiChatSpamFilter = builder
					.comment("guiChatSpamFilter : true-supress casting and fizzle text messages.")
					.translation(Main.MODID + ".config." + "guiChatSpamFilter")
					.define("guiChatSpamFilter", true);
				
			neverBreakTools = builder
					.comment("NeverBreakTools: 0 = Off, 1-99 = % to break")
					.translation(Main.MODID + ".config." + "neverBreakTools")
					.defineInRange("neverBreakTools", () -> 0, 67, 100);

			flightTime = builder
					.comment("flightTime: Seconds of Flight per mana payment")
					.translation(Main.MODID + ".config." + "flightTime")
					.defineInRange("flightTime", () -> 6, 1, 3600);

			maxFlightSpeed = builder
					.comment("maxFlightSpeed: In Meters per Second")
					.translation(Main.MODID + ".config." + "maxflightSpeed")
					.defineInRange("maxFlightSpeed", () -> 20, 0, 36);			
			
			maxChunkRedstoneMagic = builder
					.comment("Max Chunk Redstone Magic Amount")
					.translation(Main.MODID + ".config." + "maxChunkRedstoneMagic")
					.defineInRange("maxChunkRedstoneMagic", () -> 25600, 1, 25600);

			maxPlayerRedstoneMagic = builder
					.comment("Max Player Redstone Magic Amount")
					.translation(Main.MODID + ".config." + "maxPlayerRedstoneMagic")
					.defineInRange("maxPlayerRedstoneMagic", () -> 400, 0, 511);
			builder.pop();
			
			builder.push ("Mod Exclusion List (ignore Redfish if clicking on items from these mods");
			
			defaultModExclusionListActual = builder
					.comment("Mod Exclusion List String 6464")
					.translation(Main.MODID + ".config" + "defaultModExclusionListActual")
					.define("defaultModExclusionListActual", defaultModExclusionList6464);
			builder.pop();	
		}



	}
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static long castTime = 0;

	public static final int NO_CHUNK_MANA_UPDATE = -1;
	public static final int NO_PLAYER_MANA_UPDATE = -1;
	public static final Color RED = Color.fromLegacyFormat(TextFormatting.RED);
	public static final Color GREEN = Color.fromLegacyFormat(TextFormatting.GREEN);
	public static final Color YELLOW= Color.fromLegacyFormat(TextFormatting.YELLOW);
	public static final Color DEBUG = Color.fromRgb(34234142);

	static
	{
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
	private static int debugLevel;
	private static double wakeupManaRegenPercent;
	private static double guiPreparedSpellDisplayHeight;
	private static double guiSpellCastingBarHeight;
	private static double guiManaDisplayHeight;
	private static boolean guiSpamChatFilter;
	private static int neverBreakTools;
	private static int flightTime;
	private static int maxFlightSpeed;
	private static int maxChunkRedstoneMagic;
	private static int maxPlayerRedstoneMagic;
	public static String[]  defaultModExclusionList;

	public static String    defaultModExclusionList6464;

	public static void bakeConfig()
	{
		debugLevel = COMMON.debugLevel.get();
		wakeupManaRegenPercent = COMMON.wakeupManaRegenPercent.get();
		guiPreparedSpellDisplayHeight = COMMON.guiPreparedSpellDisplayHeight.get();
		guiManaDisplayHeight = COMMON.guiManaDisplayHeight.get();
		guiSpellCastingBarHeight = COMMON.guiSpellCastingBarHeight.get();
		guiSpamChatFilter = COMMON.guiChatSpamFilter.get();
		flightTime = COMMON.flightTime.get();
		maxFlightSpeed = COMMON.maxFlightSpeed.get();
		neverBreakTools = COMMON.neverBreakTools.get();
		maxChunkRedstoneMagic = COMMON.maxChunkRedstoneMagic.get();
		maxPlayerRedstoneMagic = COMMON.maxPlayerRedstoneMagic.get();
		defaultModExclusionList6464 = COMMON.defaultModExclusionListActual.get() ;
		SpellManager.redstoneMagicSpellInit();

	}

	
	public static int getDebugLevel() {
		return debugLevel;
	}
	
	public static int getFlightTime() {
		return flightTime;
	}

	public static void setGuiPreparedSpellDisplayHeight(double preparedSpellDisplayHeight) {
		MyConfig.guiPreparedSpellDisplayHeight = preparedSpellDisplayHeight;
	}

	public static double getGuiSpellCastingBarHeight() {
		return guiSpellCastingBarHeight/100.0;
	}

	public static void setGuiSpellCastingBarHeight(double height) {
		guiSpellCastingBarHeight = height;
	}
	
	public static void setGuiManaDisplayHeight(double guiManaDisplayHeight) {
		MyConfig.guiManaDisplayHeight = guiManaDisplayHeight;
	}

	public static boolean getGuiSpamChatFilter() {
		return guiSpamChatFilter;
	}


	public static double getGuiPreparedSpellDisplayHeight() {
		if (guiPreparedSpellDisplayHeight == 0.0) 
			guiPreparedSpellDisplayHeight = 65.0;
		return (guiPreparedSpellDisplayHeight/100.0);
	}


	public static double getGuiManaDisplayHeight() {
		if (guiManaDisplayHeight == 0.0)
			guiManaDisplayHeight = 12.0;
		return (guiManaDisplayHeight/100.0);
	}
	
	public static int getMaxChunkRedstoneMagic() {
		if (maxChunkRedstoneMagic == 0) 
			maxChunkRedstoneMagic = 25600;
		return maxChunkRedstoneMagic;
	}

	public static int getMaxFlightSpeed() {
		return maxFlightSpeed;
	}
	
	public static int getMaxPlayerRedstoneMagic() {
		if (maxPlayerRedstoneMagic == 0) 
			maxPlayerRedstoneMagic = 400;
		return maxPlayerRedstoneMagic;
	}

	public static int getNeverBreakTools() {
		return neverBreakTools;
	}

	public static double getWakeupManaRegenPercent() {
		return (wakeupManaRegenPercent/100.0);
	}

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


	public static void setDebugLevel(int debugLevel) {
		MyConfig.debugLevel = debugLevel;
	}

	
	public static void setMaxChunkRedstoneMagic(int maxChunkRedstoneMagic) {
		MyConfig.maxChunkRedstoneMagic = maxChunkRedstoneMagic;
	}
	
	public static void setMaxPlayerRedstoneMagic(int maxPlayerRedstoneMagic) {
		MyConfig.maxPlayerRedstoneMagic = maxPlayerRedstoneMagic;
	}

	// support for debug messages
	public static void dbgPrintln(int dbgLevel, String dbgMsg) {
		if (dbgLevel <= debugLevel) {
			System.out.println (dbgMsg);
		}
	}

	public static void dbgPrintln(PlayerEntity p, String dbgMsg, int dbgLevel) {
		if (dbgLevel <= debugLevel ) {
			sendChat (p, dbgMsg);
		}
	}

	
	// support for any color, optionally bold text.
	public static void sendBoldChat(PlayerEntity p, String chatMessage, Color color) {
		StringTextComponent component = new StringTextComponent (chatMessage);

		component.getStyle().withBold(true);
		component.getStyle().withColor(color);
		
		p.sendMessage(component, p.getUUID());
	}

	// support for any color chattext
	public static void sendChat(PlayerEntity p, String chatMessage, Color color) {
		StringTextComponent component = new StringTextComponent (chatMessage);
		component.getStyle().withColor(color);
		p.sendMessage(component, p.getUUID());
	}

	public static void sendChat(PlayerEntity p, String chatMessage ) {
		sendChat (p, chatMessage, DEBUG);
	}
	
}

