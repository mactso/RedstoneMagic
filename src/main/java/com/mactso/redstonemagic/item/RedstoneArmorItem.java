package com.mactso.redstonemagic.item;

import java.util.Iterator;
import java.util.List;

import com.mactso.redstonemagic.Main;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class RedstoneArmorItem extends DyeableArmorItem implements IGuiRightClick 
{
	public RedstoneArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties prop, String name) {
		super(material, slot, prop);
		setRegistryName(Main.MODID, name);
	}

	@Override
	public int getColor(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getChildTag("display");
		return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 0xCAC8C8;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;  // remember to remove from vending machine in 2gmp.
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {

		if ((player instanceof ServerPlayerEntity)) {
			if ((world.getGameTime())%5 == 0) // check suit bonus only every 5th tick. 
			{
				Iterable<ItemStack> playerArmorSet = player.getArmorInventoryList();
				Iterator<ItemStack> i = playerArmorSet.iterator();
				boolean suitBonus = true;
				while (i.hasNext()) {
					ItemStack armorpiece = i.next();
					String s = armorpiece.getItem().getRegistryName().getNamespace().toString();
					if (!(s.equals("redstonemagic"))) {
						suitBonus = false;
					}
				}
				if (suitBonus) {
					EffectInstance ei = player.getActivePotionEffect(Effects.RESISTANCE);
					int effectDuration = 160; // 8 seconds
					int effectIntensity = 0;
					boolean refreshSuitBonus = false;
					if (ei == null) {
						refreshSuitBonus = true;
					}
					if (ei != null) {
						int durationLeft = ei.getDuration();
						if (ei.getAmplifier() > effectIntensity) {
							if (durationLeft <= 6) {
								player.removeActivePotionEffect(Effects.RESISTANCE);
								refreshSuitBonus = true;
							}
						}
					}
					if (refreshSuitBonus) {
						player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, effectDuration, effectIntensity, true, true));
					}

				}
				
			}
			
		}
		super.onArmorTick(stack, world, player);
	}
	@Override
	public boolean hasEffect(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getChildTag("display");
		boolean glint = compoundnbt != null && compoundnbt.contains("glint", 1) ? compoundnbt.getBoolean("glint")
				: false;
		return glint && super.hasEffect(stack);
	}

	@Override
	public void menuRightClick(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getOrCreateChildTag("display");
		boolean glint = compoundnbt.contains("glint", 1) && compoundnbt.getBoolean("glint");
		if (glint)
			compoundnbt.remove("glint");
		else
			compoundnbt.putBoolean("glint", true);
	}

}
