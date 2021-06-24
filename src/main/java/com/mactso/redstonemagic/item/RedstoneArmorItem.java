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

import net.minecraft.item.Item.Properties;

public class RedstoneArmorItem extends DyeableArmorItem implements IGuiRightClick 
{
	public RedstoneArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties prop, String name) {
		super(material, slot, prop);
		setRegistryName(Main.MODID, name);
	}

	@Override
	public int getColor(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getTagElement("display");
		return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 0xCAC8C8;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;  // remember to remove from vending machine in 2gmp.
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {

		if ((player instanceof ServerPlayerEntity)) {
			checkRedstoneArmorSuitBonus(stack, world, player);
		}
		super.onArmorTick(stack, world, player);
	}

	private void checkRedstoneArmorSuitBonus(ItemStack stack, World world, PlayerEntity player) {

		// only check suit bonus once per second
		if (world.getGameTime() % 20 == 0) {
			int suitBonus = 0;
			if (stack.getItem().getDescriptionId().contains("redstonemagic")) {
				Iterable<ItemStack> playerArmorSet = player.getArmorSlots();
				Iterator<ItemStack> i = playerArmorSet.iterator();
				while (i.hasNext()) {
					ItemStack armorpiece = i.next();
					if (armorpiece.getItem().getRegistryName().getNamespace().toString().equals("redstonemagic")) {
						suitBonus += 1;
					}
				}
				applyRedstoneArmorManaRegeneration(world, player, suitBonus);
				if (suitBonus == 4) {
					applyRedstoneArmorSuitBonusResistance(player);
				} 

			}

		}

	}

	private void applyRedstoneArmorSuitBonusResistance(PlayerEntity player) {

		boolean refreshSuitBonus = true;
		int effectDuration = 160; // 8 seconds
		int effectIntensity = 0;
		EffectInstance ei = player.getEffect(Effects.DAMAGE_RESISTANCE);
		if (ei != null) {
			if (ei.getAmplifier() > effectIntensity) {
				if (ei.getDuration() <= 15) {
					player.removeEffectNoUpdate(Effects.DAMAGE_RESISTANCE);
				} else {
					refreshSuitBonus = false;
				}
			}
		}
		if (refreshSuitBonus) {
			if (MyConfig.getDebugLevel() > 1) {
				System.out
						.println("Redstone Magic: " + player.getName().getString() + " Applying Suit Resistance Bonus");
			}
			player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, effectDuration, effectIntensity, true, true));
		}

	}

	private void applyRedstoneArmorManaRegeneration(World world, PlayerEntity player, int suitBonus) {
		final int DEFAULT_MINIMUM_MANA = 60;
		final int MANA_REGEN_PERIOD = 300; // 300 ticks... 15 seconds

		if (world.getGameTime() % MANA_REGEN_PERIOD == 0) {
			
			IMagicStorage cap = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				float maxBonusRegenTotal = (MyConfig.getMaxPlayerRedstoneMagic() * 0.01f) * (4 + suitBonus);
				if (suitBonus == 4) 
					maxBonusRegenTotal = (int) (0.20f * MyConfig.getMaxPlayerRedstoneMagic());
				
				if (maxBonusRegenTotal == 0)
					maxBonusRegenTotal = DEFAULT_MINIMUM_MANA + suitBonus * 4;

				float manaRegenRate = MyConfig.getMaxPlayerRedstoneMagic() * 0.005f;
				if (manaRegenRate < 2) {
					manaRegenRate = 2;
				}
				if (cap.getManaStored() < maxBonusRegenTotal) {
					cap.addMana((int)manaRegenRate);
					Network.sendToClient(new SyncClientManaPacket(cap.getManaStored(), MyConfig.NO_CHUNK_MANA_UPDATE),
							(ServerPlayerEntity) player);
				}
			}
		}
	}


	@Override
	public boolean isFoil(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getTagElement("display");
		boolean glint = compoundnbt != null && compoundnbt.contains("glint", 1) ? compoundnbt.getBoolean("glint")
				: false;
		return glint && super.isFoil(stack);
	}

	@Override
	public void menuRightClick(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getOrCreateTagElement("display");
		boolean glint = compoundnbt.contains("glint", 1) && compoundnbt.getBoolean("glint");
		if (glint)
			compoundnbt.remove("glint");
		else
			compoundnbt.putBoolean("glint", true);
	}

}
