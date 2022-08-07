package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.Main;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;

public class RedstoneArmorMaterial implements ArmorMaterial {

	private ArmorMaterial clone;
	private String materialName;
	private int enchantAbility;


	public RedstoneArmorMaterial(ArmorMaterials inClone, String materialPrefix, int enchantAbility) {
		clone = inClone;
		materialName = materialPrefix;
		this.enchantAbility = enchantAbility;
	}
	
//  FYI stats of Netherite (for when I do possible diamond suit).
//  NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
//  return Ingredient.fromItems(Items.NETHERITE_INGOT);
//  });

	@Override
	public int getDurabilityForSlot(EquipmentSlot slotIn) {
		return clone.getDurabilityForSlot(slotIn);
	}

	@Override
	public int getDefenseForSlot(EquipmentSlot slotIn) {
		// Leather 1, 2, 3, 1
		// Chain: 1, 4, 5, 2 
		// RML : 2, 4, 5, 2
		// Iron: 2, 5, 6, 2
		if (this.clone == ArmorMaterials.LEATHER) {
			if (slotIn == EquipmentSlot.HEAD) {
				return 2;
			}
			if (slotIn == EquipmentSlot.CHEST) {
				return 5;
			}
			if (slotIn == EquipmentSlot.LEGS) {
				return 5;
			}
			if (slotIn == EquipmentSlot.FEET) {
				return 2;
			}
		}
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
		if (this.clone == ArmorMaterials.LEATHER) {
			return 1.0f;
		}		
		return 3.2f;
	}

	// getKnockbackResistance,getKnockbackResistance,2,Gets the percentage of knockback resistance provided by armor of the material. 
	@Override
	public float getKnockbackResistance() {
		if (this.clone == ArmorMaterials.LEATHER) {
			return 0.11f;
		}
		return 0.13f;
	}
}
