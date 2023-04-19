package com.mactso.redstonemagic.item;

import com.mactso.redstonemagic.Main;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;

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
	public int getDurabilityForType(ArmorItem.Type typeIn) {
		return clone.getDurabilityForType(typeIn);
	}

	@Override
	public int getDefenseForType(ArmorItem.Type typeIn) {
		// Leather 1, 2, 3, 1
		// Chain: 1, 4, 5, 2 
		// RML : 2, 4, 5, 2
		// Iron: 2, 5, 6, 2
		if (this.clone == ArmorMaterials.LEATHER) {
			if (typeIn == ArmorItem.Type.HELMET) {
				return 2;
			}
			if (typeIn == ArmorItem.Type.CHESTPLATE) {
				return 5;
			}
			if (typeIn == ArmorItem.Type.LEGGINGS) {
				return 5;
			}
			if (typeIn == ArmorItem.Type.BOOTS) {
				return 2;
			}
		}
		return clone.getDefenseForType(typeIn);
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
