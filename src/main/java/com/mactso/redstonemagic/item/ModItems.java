package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.block.ModBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems 
{

	public static final Item REDSTONE_MAGIC_PYLON_MINOR 
	 = new BlockItem(ModBlocks.REDSTONE_MAGIC_PYLON_MINOR, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_magic_pylon_minor");
	public static final Item REDSTONE_FOCUS_ITEM	
	 = new RedstoneFocusItem(new Properties().group(ItemGroup.REDSTONE).maxDamage(484)).setRegistryName("redstone_focus");
	private static final IArmorMaterial REDSTONEMAGIC_MATERIAL = new RedstoneArmorMaterial(ArmorMaterial.NETHERITE,"redstonemagic");
	private static final Item REDSTONEMAGIC_HELMET = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.HEAD, new Properties().group(ItemGroup.COMBAT).isBurnable(), "redstonemagic_helmet");
	private static final Item REDSTONEMAGIC_CHESTPLATE = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.CHEST, new Properties().group(ItemGroup.COMBAT).isBurnable(), "redstonemagic_chestplate");
	private static final Item REDSTONEMAGIC_LEGGINGS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.LEGS, new Properties().group(ItemGroup.COMBAT).isBurnable(), "redstonemagic_leggings");
	private static final Item REDSTONEMAGIC_BOOTS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.FEET, new Properties().group(ItemGroup.COMBAT).isBurnable(), "redstonemagic_boots");

//	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_power_block");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(REDSTONE_MAGIC_PYLON_MINOR );
		forgeRegistry.register(REDSTONE_FOCUS_ITEM );
		forgeRegistry.registerAll(REDSTONEMAGIC_HELMET, REDSTONEMAGIC_CHESTPLATE, REDSTONEMAGIC_LEGGINGS, REDSTONEMAGIC_BOOTS);
	}
	@OnlyIn(Dist.CLIENT)
	public static void register(ItemColors itemColors)
	{
		itemColors.register((itemstack, index) -> {
        	return index > 0 ? -1 : ((IDyeableArmorItem)itemstack.getItem()).getColor(itemstack);
        }, REDSTONEMAGIC_HELMET, REDSTONEMAGIC_CHESTPLATE, REDSTONEMAGIC_LEGGINGS, REDSTONEMAGIC_BOOTS);
	}	
}
