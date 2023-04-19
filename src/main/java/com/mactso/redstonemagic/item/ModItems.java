package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.block.ModBlocks;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

	public static final Item GATHERER = new BlockItem(ModBlocks.GATHERER, new Properties().rarity(Rarity.RARE));
	public static final Item FLYING_REAGENT = new Item((new Item.Properties()).rarity(Rarity.RARE));
	public static final Item RITUAL_PYLON = new BlockItem(ModBlocks.RITUAL_PYLON, new Properties().rarity(Rarity.RARE));
	public static final Item REDSTONE_FOCUS_ITEM = new RedstoneFocusItem(
			new Properties().rarity(Rarity.RARE).durability(484));
	public static final ArmorMaterial REDSTONEMAGIC_MATERIAL = new RedstoneArmorMaterial(ArmorMaterials.NETHERITE,
			"redstonemagic", 17);
	public static final Item REDSTONEMAGIC_HELMET = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, ArmorItem.Type.HELMET,
			new Properties().fireResistant().rarity(Rarity.EPIC));
	public static final Item REDSTONEMAGIC_CHESTPLATE = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL,
			ArmorItem.Type.CHESTPLATE, new Properties().fireResistant().rarity(Rarity.EPIC));
	public static final Item REDSTONEMAGIC_LEGGINGS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, ArmorItem.Type.LEGGINGS,
			new Properties().fireResistant().rarity(Rarity.EPIC));
	public static final Item REDSTONEMAGIC_BOOTS = new RedstoneArmorItem(REDSTONEMAGIC_MATERIAL, ArmorItem.Type.BOOTS,
			new Properties().fireResistant().rarity(Rarity.EPIC));

	public static final ArmorMaterial REDSTONEMAGIC_LEATHER_MATERIAL = new RedstoneArmorMaterial(ArmorMaterials.LEATHER,
			"redstonemagic", 25);
	public static final Item REDSTONEMAGIC_LEATHER_HELMET = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL,
			ArmorItem.Type.HELMET, new Properties().rarity(Rarity.UNCOMMON).durability(160).defaultDurability(180));
	public static final Item REDSTONEMAGIC_LEATHER_CHESTPLATE = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL,
			ArmorItem.Type.CHESTPLATE, new Properties().rarity(Rarity.UNCOMMON).durability(220).defaultDurability(240));
	public static final Item REDSTONEMAGIC_LEATHER_LEGGINGS = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL,
			ArmorItem.Type.LEGGINGS, new Properties().rarity(Rarity.UNCOMMON).durability(210).defaultDurability(220));
	public static final Item REDSTONEMAGIC_LEATHER_BOOTS = new RedstoneArmorItem(REDSTONEMAGIC_LEATHER_MATERIAL,
			ArmorItem.Type.BOOTS, new Properties().rarity(Rarity.UNCOMMON).durability(170).defaultDurability(180));

//	public static final Item REDSTONE_POWER_BLOCK = new BlockItem(ModBlocks.REDSTONE_POWER_BLOCK, new Properties().group(ItemGroup.REDSTONE)).setRegistryName("redstone_power_block");

	public static void register(IForgeRegistry<Item> forgeRegistry) {
		forgeRegistry.register("ritual_pylon", RITUAL_PYLON);
		forgeRegistry.register("redstone_focus", REDSTONE_FOCUS_ITEM);
		forgeRegistry.register("gatherer", GATHERER);
		forgeRegistry.register("flying_reagent", FLYING_REAGENT);

		forgeRegistry.register("redstonemagic_leather_helmet", REDSTONEMAGIC_LEATHER_HELMET);
		forgeRegistry.register("redstonemagic_leather_chestplate", REDSTONEMAGIC_LEATHER_CHESTPLATE);
		forgeRegistry.register("redstonemagic_leather_leggings", REDSTONEMAGIC_LEATHER_LEGGINGS);
		forgeRegistry.register("redstonemagic_leather_boots", REDSTONEMAGIC_LEATHER_BOOTS);

		forgeRegistry.register("redstonemagic_helmet", REDSTONEMAGIC_HELMET);
		forgeRegistry.register("redstonemagic_chestplate", REDSTONEMAGIC_CHESTPLATE);
		forgeRegistry.register("redstonemagic_leggings", REDSTONEMAGIC_LEGGINGS);
		forgeRegistry.register("redstonemagic_boots", REDSTONEMAGIC_BOOTS);

	}

	@OnlyIn(Dist.CLIENT)
	public static void register(RegisterColorHandlersEvent.Item event) {
		event.register((itemstack, index) -> {
			return index > 0 ? -1 : ((DyeableLeatherItem) itemstack.getItem()).getColor(itemstack);
		}, REDSTONEMAGIC_HELMET, REDSTONEMAGIC_CHESTPLATE, REDSTONEMAGIC_LEGGINGS, REDSTONEMAGIC_BOOTS,
				REDSTONEMAGIC_LEATHER_HELMET, REDSTONEMAGIC_LEATHER_CHESTPLATE, REDSTONEMAGIC_LEATHER_LEGGINGS,
				REDSTONEMAGIC_LEATHER_BOOTS);
	}
}
