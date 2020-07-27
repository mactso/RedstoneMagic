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
//	 = register("redstone_magic_pylon_minor", new BlockItem(AbstractBlock.Properties.create(Material.GLASS).func_235861_h_().tickRandomly().func_235838_a_(func_235420_a_(9)).hardnessAndResistance(3.0F, 3.0F)));

//	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_power_block");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(REDSTONE_MAGIC_PYLON_MINOR );
	}
	
}
