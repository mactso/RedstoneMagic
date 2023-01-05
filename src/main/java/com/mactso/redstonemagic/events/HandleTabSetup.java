package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.item.ModItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Bus.MOD)
public class HandleTabSetup {

	@SubscribeEvent
	public static void handleTabSetup(CreativeModeTabEvent.BuildContents event) {

		if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(ModItems.FLYING_REAGENT);
			event.accept(ModItems.GATHERER);
			event.accept(ModItems.REDSTONE_FOCUS_ITEM);
			event.accept(ModItems.RITUAL_PYLON);
		} else if (event.getTab() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(ModItems.GATHERER);
			event.accept(ModItems.RITUAL_PYLON);
		} else if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
			event.accept(ModItems.REDSTONE_FOCUS_ITEM);
			event.accept(ModItems.FLYING_REAGENT);
		} else if (event.getTab() == CreativeModeTabs.COMBAT) {
			event.accept(ModItems.REDSTONE_FOCUS_ITEM);
			event.accept(ModItems.REDSTONEMAGIC_HELMET);
			event.accept(ModItems.REDSTONEMAGIC_CHESTPLATE);
			event.accept(ModItems.REDSTONEMAGIC_LEGGINGS);
			event.accept(ModItems.REDSTONEMAGIC_BOOTS);
			event.accept(ModItems.REDSTONEMAGIC_LEATHER_HELMET);
			event.accept(ModItems.REDSTONEMAGIC_LEATHER_CHESTPLATE);
			event.accept(ModItems.REDSTONEMAGIC_LEATHER_LEGGINGS);
			event.accept(ModItems.REDSTONEMAGIC_LEATHER_BOOTS);
		}
	}
}
