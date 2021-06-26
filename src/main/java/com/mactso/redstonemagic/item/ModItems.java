package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.block.ModBlocks;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems 
{

	public static final Item GATHERER
	 = new BlockItem(ModBlocks.GATHERER, new Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName("gatherer");
	public static final Item TEST_BLOCK2
	 = new BlockItem(ModBlocks.TEST_BLOCK2, new Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName("test_block2");

	public static final Item RITUAL_PYLON 
	 = new BlockItem(ModBlocks.RITUAL_PYLON, new Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName("ritual_pylon");
	public static final Item REDSTONE_FOCUS_ITEM	
	 = new RedstoneFocusItem(new Properties().tab(ItemGroup.TAB_REDSTONE).durability(484)).setRegistryName("redstone_focus");
	public static final IArmorMaterial REDSTONEMAGIC_MATERIAL = new RedstoneArmorMaterial(ArmorMaterial.NETHERITE,"redstonemagic");
	private static final Item REDSTONEMAGIC_HELMET = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.HEAD, new Properties().tab(ItemGroup.TAB_COMBAT).fireResistant().rarity(Rarity.EPIC), "redstonemagic_helmet");
	private static final Item REDSTONEMAGIC_CHESTPLATE = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.CHEST, new Properties().tab(ItemGroup.TAB_COMBAT).fireResistant().rarity(Rarity.EPIC), "redstonemagic_chestplate");
	private static final Item REDSTONEMAGIC_LEGGINGS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.LEGS, new Properties().tab(ItemGroup.TAB_COMBAT).fireResistant().rarity(Rarity.EPIC), "redstonemagic_leggings");
	private static final Item REDSTONEMAGIC_BOOTS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, EquipmentSlotType.FEET, new Properties().tab(ItemGroup.TAB_COMBAT).fireResistant().rarity(Rarity.EPIC), "redstonemagic_boots");

	public static final IArmorMaterial REDSTONEMAGIC_LEATHER_MATERIAL = new RedstoneArmorMaterial(ArmorMaterial.LEATHER,"redstonemagic");
	private static final Item REDSTONEMAGIC_LEATHER_HELMET = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL, EquipmentSlotType.HEAD, new Properties().tab(ItemGroup.TAB_COMBAT).rarity(Rarity.UNCOMMON).durability(160).defaultDurability(180), "redstonemagic_leather_helmet");
	private static final Item REDSTONEMAGIC_LEATHER_CHESTPLATE = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL, EquipmentSlotType.CHEST, new Properties().tab(ItemGroup.TAB_COMBAT).rarity(Rarity.UNCOMMON).durability(220).defaultDurability(240), "redstonemagic_leather_chestplate");
	private static final Item REDSTONEMAGIC_LEATHER_LEGGINGS = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL, EquipmentSlotType.LEGS, new Properties().tab(ItemGroup.TAB_COMBAT).rarity(Rarity.UNCOMMON).durability(210).defaultDurability(220), "redstonemagic_leather_leggings");
	private static final Item REDSTONEMAGIC_LEATHER_BOOTS = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL, EquipmentSlotType.FEET, new Properties().tab(ItemGroup.TAB_COMBAT).rarity(Rarity.UNCOMMON).durability(170).defaultDurability(180), "redstonemagic_leather_boots");

//	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_power_block");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(RITUAL_PYLON );
		forgeRegistry.register(REDSTONE_FOCUS_ITEM );
		forgeRegistry.register(GATHERER);	
//		forgeRegistry.register(TEST_BLOCK2);	
		forgeRegistry.registerAll(REDSTONEMAGIC_HELMET, REDSTONEMAGIC_CHESTPLATE, REDSTONEMAGIC_LEGGINGS, REDSTONEMAGIC_BOOTS);
		forgeRegistry.registerAll(REDSTONEMAGIC_LEATHER_HELMET, REDSTONEMAGIC_LEATHER_CHESTPLATE, REDSTONEMAGIC_LEATHER_LEGGINGS, REDSTONEMAGIC_LEATHER_BOOTS);

	}
	@OnlyIn(Dist.CLIENT)
	public static void register(ItemColors itemColors)
	{
		itemColors.register((itemstack, index) -> {
        	return index > 0 ? -1 : ((IDyeableArmorItem)itemstack.getItem()).getColor(itemstack);
        }, REDSTONEMAGIC_HELMET, REDSTONEMAGIC_CHESTPLATE, REDSTONEMAGIC_LEGGINGS, REDSTONEMAGIC_BOOTS
				, REDSTONEMAGIC_LEATHER_HELMET, REDSTONEMAGIC_LEATHER_CHESTPLATE, REDSTONEMAGIC_LEATHER_LEGGINGS, REDSTONEMAGIC_LEATHER_BOOTS);
	}	
}
