package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.Main;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class RedstoneArmorMaterial implements IArmorMaterial {

	private IArmorMaterial clone;
	private String materialName;

	public RedstoneArmorMaterial(ArmorMaterial inClone, String materialPrefix) {
		clone = inClone;
		materialName = materialPrefix;
	}
//  NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
//  return Ingredient.fromItems(Items.NETHERITE_INGOT);
//  });
	private static final int[] MAX_DAMAGE_ARRAY = new int[] { 3, 6, 8, 3 };

	@Override
	public int getDurability(EquipmentSlotType slotIn) {
		return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * 37;
	}

	@Override
	public int getDamageReductionAmount(EquipmentSlotType slotIn) {
		return MAX_DAMAGE_ARRAY[slotIn.getIndex()];
	}

	@Override
	public int getEnchantability() {
		return 23;
	}

	@Override
	public SoundEvent getSoundEvent() {
		return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
	}

	@Override
	public Ingredient getRepairMaterial() {
		return Ingredient.fromItems(Items.NETHERITE_INGOT);
	}

	@Override
	public String getName() {
		return Main.MODID + ":" + materialName;
	}

	@Override
	public float getToughness() {
		return 3f;
	}

	// func_230304_f_,getKnockbackResistance,2,Gets the percentage of knockback resistance provided by armor of the material. 
	@Override
	public float getKnockbackResistance() {
		return 0.11f;
	}
}
