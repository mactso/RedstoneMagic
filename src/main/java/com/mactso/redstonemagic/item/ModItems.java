package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.block.ModBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item.Properties;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

	public static final Item REDSTONE_MAGIC_PYLON_MINOR 
	 = new BlockItem(ModBlocks.REDSTONE_MAGIC_PYLON_MINOR, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_magic_pylon_minor");
	public static final Item REDSTONE_MAGIC_WAND	
	 = new RedstoneMagicWand(new Properties().group(ItemGroup.REDSTONE).maxDamage(484)).setRegistryName("redstone_magic_wand");

//	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_power_block");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(REDSTONE_MAGIC_PYLON_MINOR );
		forgeRegistry.register(REDSTONE_MAGIC_WAND );
	}
	
}
