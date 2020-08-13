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
	static
	{
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static int debugLevel;
	public static int maxChunkRedstoneMagic;
	public static int maxPlayerRedstoneMagic;
	
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
					.defineInRange("maxChunkRedstoneMagic", () -> 60, 1, 3600);

			maxPlayerRedstoneMagic = builder
					.comment("Max Player Redstone Magic Amount")
					.translation(Main.MODID + ".config." + "maxPlayerRedstoneMagic")
					.defineInRange("maxPlayerRedstoneMagic", () -> 255, 0, 511);
			builder.pop();
			
		}
	}
	

	// support for debug messages
	public static void dbgPrintln(String dbgMsg) {
		if (debugLevel > 0) {
			System.out.println (dbgMsg);
		}
	}
	public static void dbgPrintln(PlayerEntity p, String dbgMsg) {
		if (debugLevel > 1) {
			sendChat (p, dbgMsg, Color.func_240744_a_(TextFormatting.YELLOW));
		}
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

