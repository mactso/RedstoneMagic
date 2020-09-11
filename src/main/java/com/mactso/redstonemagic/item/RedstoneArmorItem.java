package com.mactso.redstonemagic.item;

import java.util.Iterator;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.IArmorMaterial;
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

			if (stack.getItem().getTranslationKey().equals("item.redstonemagic.redstonemagic_chestplate")) {
				if ((world.getGameTime())%20 == 0) // check suit bonus once per second. 
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
						if ((world.getGameTime()) % 300 == 0) {

							IMagicStorage cap = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
							if (cap != null) {
								if (MyConfig.getDebugLevel() > 1) {
									System.out.println("Redstone Magic: "+player.getName().getString() +" ("+ cap.getManaStored()+") Full Suit Mana Regen");
								}
								int maxMana = MyConfig.getMaxPlayerRedstoneMagic()/5;
								if (maxMana == 0) {
									maxMana = 60;
								}
								if (cap.getManaStored() < maxMana ) {
									cap.addMana(2); // checks for max capacity internally based on object type.
									Network.sendToClient(new SyncClientManaPacket(cap.getManaStored(), MyConfig.NO_CHUNK_MANA_UPDATE),
											(ServerPlayerEntity) player);			}						
										
								}
						}
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
								if (durationLeft <= 10) {
									player.removeActivePotionEffect(Effects.RESISTANCE);
									refreshSuitBonus = true;
								}
							}
						}
						if (refreshSuitBonus) {
							if (MyConfig.getDebugLevel() > 1) {
								System.out.println("Redstone Magic: "+player.getName().getString()+" Applying Suit Resistance Bonus");
							}
							
							player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, effectDuration, effectIntensity, true, true));
						}

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
