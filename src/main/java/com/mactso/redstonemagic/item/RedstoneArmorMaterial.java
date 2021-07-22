package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.Main;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;

public class RedstoneArmorMaterial implements IArmorMaterial {

	private IArmorMaterial clone;
	private String materialName;
	private int enchantAbility;


	public RedstoneArmorMaterial(ArmorMaterial inClone, String materialPrefix, int enchantAbility) {
		clone = inClone;
		materialName = materialPrefix;
		this.enchantAbility = enchantAbility;
	}
	
//  FYI stats of Netherite (for when I do possible diamond suit).
//  NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
//  return Ingredient.fromItems(Items.NETHERITE_INGOT);
//  });

	@Override
	public int getDurabilityForSlot(EquipmentSlotType slotIn) {
		return clone.getDurabilityForSlot(slotIn);
	}

	@Override
	public int getDefenseForSlot(EquipmentSlotType slotIn) {
		return clone.getDefenseForSlot(slotIn);
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantAbility;
	}

	@Override
	public SoundEvent getEquipSound() {
		return clone.getEquipSound();
	}

	@Override
	public Ingredient getRepairIngredient() {
		return clone.getRepairIngredient();
	}

	@Override
	public String getName() {
		return Main.MODID + ":" + materialName;
	}

	@Override
	public float getToughness() {
		return clone.getToughness();
	}

	// getKnockbackResistance,getKnockbackResistance,2,Gets the percentage of knockback resistance provided by armor of the material. 
	@Override
	public float getKnockbackResistance() {
		return 0.11f;
	}
}
