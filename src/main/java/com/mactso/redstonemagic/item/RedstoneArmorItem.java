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
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
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
				doArmorManaRegeneration(world, player, suitBonus);
				if (suitBonus == 4 ) {
					doSuitBonuses(player);
				} 

			}

		}

	}

	private void doSuitBonuses(PlayerEntity player) {

		if (this.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
			doASuitBonus(player,Effects.DAMAGE_RESISTANCE,160,0);
		} else if (player.level.getGameTime()%6000 == 0 &&this.getMaterial() == ModItems.REDSTONEMAGIC_LEATHER_MATERIAL) {
			doASuitBonus(player,Effects.ABSORPTION,6000,0);
		}

	}

	private void doASuitBonus(PlayerEntity player, Effect effect, int effectDuration, int effectIntensity) {
		boolean refreshSuitBonus = true;
		EffectInstance ei = player.getEffect(effect);
		if (ei != null) {
			if (ei.getAmplifier() > effectIntensity) {
				if (ei.getDuration() <= 15) {
					player.removeEffectNoUpdate(effect);
				} else {
					refreshSuitBonus = false;
				}
			}
		}
		if (refreshSuitBonus) {
			MyConfig.dbgPrintln(1, "Redstone Magic: " + player.getName().getString() + " Apply Suit Bonus : " + effect.getRegistryName().getNamespace().toString());
			player.addEffect(new EffectInstance(effect, effectDuration, effectIntensity, true, true));
		}
	}

	private void doArmorManaRegeneration(World world, PlayerEntity player, int suitBonus) {
		final int MANA_REGEN_PERIOD = 160; // 160 ticks... 8 seconds
		if (world.getGameTime() % MANA_REGEN_PERIOD == 0) {
			if (this.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
				doAnArmorManaRegeneration(player, suitBonus, 0.20f,3,05);
			} else { // Leather
				doAnArmorManaRegeneration(player, suitBonus, 0.06f,1,90);
			}
			
		}
	}

	private void doAnArmorManaRegeneration(PlayerEntity player, int suitBonus, float maxSuitManaPct, int manaRegenRate, int manaRegenChance) {
		IMagicStorage cap = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			float maxNaturalMana = (MyConfig.getMaxPlayerRedstoneMagic() * 0.01f) * (4 + suitBonus);

			if (suitBonus == 4) 
				maxNaturalMana = (int) (maxSuitManaPct * MyConfig.getMaxPlayerRedstoneMagic());
			if (maxNaturalMana  == 0)
				maxNaturalMana = (int) ((suitBonus*4) + (300 * maxSuitManaPct));

			if (player.level.getRandom().nextInt(100) > manaRegenChance) {
				if (cap.getManaStored() < maxNaturalMana) {
					MyConfig.sendChat(player,"Regen Mana: "+ cap.getManaStored() + ", max:" +maxNaturalMana + ", regen:"+ manaRegenRate, Color.fromLegacyFormat(TextFormatting.RED));
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
