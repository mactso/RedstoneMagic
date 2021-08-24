package com.mactso.redstonemagic.item;

import java.util.Iterator;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class RedstoneArmorItem extends DyeableArmorItem implements IGuiRightClick {
	private final EquipmentSlotType slotType;
	private final boolean isChestSlotType;

	public RedstoneArmorItem(IArmorMaterial material, EquipmentSlotType slotType, Properties prop, String name) {
		super(material, slotType, prop);
		this.slotType = slotType;
		if (slotType == EquipmentSlotType.CHEST) {
			isChestSlotType = true;
		} else {
			isChestSlotType = false;
		}
		setRegistryName(Main.MODID, name);
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return true;
	}

	@Override
	public int getColor(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getTagElement("display");
		return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 0xCAC8C8;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC; // remember to remove from vending machine in 2gmp.
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
		final int ONE_SECOND = 20;  
		final int MANA_REGEN_PERIOD = 160; // 160 ticks... 8 seconds
		final int ARMOR_MEND_PERIOD = 80; // 80 ticks... 4 seconds

		if ((player instanceof ServerPlayerEntity)) {
			ServerPlayerEntity sPlayer = (ServerPlayerEntity) player;

			long gameTime = sPlayer.level.getGameTime();
			boolean isFullSuit = isFullSuit(stack, sPlayer);

			if (gameTime % ONE_SECOND == 0) {
				if (isFullSuit && isChestSlotType) {
					doSuitBonuses(sPlayer, gameTime);
				}


				if ((gameTime % MANA_REGEN_PERIOD == 0) 
						&& (!RedstoneFocusItem.getIsFlying(player))) {
					Block blockBelow = sPlayer.level.getBlockState(sPlayer.blockPosition().below()).getBlock();
					if (!RedstoneFocusItem.NO_FLY_LIST.contains(blockBelow) && blockBelow != Blocks.AIR) {
						doArmorManaRegeneration(sPlayer, isFullSuit, maxManaRegenAmount(stack, sPlayer));
					}
				}


				if (gameTime % ARMOR_MEND_PERIOD == 0 && isFoil(stack)) {
					doArmorMending(stack, sPlayer);
				}

			}
		}
		super.onArmorTick(stack, world, player);
	}

	private boolean isFullSuit(ItemStack stack, ServerPlayerEntity sPlayer) {
		int suitBonus = 0;
		if (stack.getItem().getDescriptionId().contains("redstonemagic")) {
			Iterable<ItemStack> playerArmorSet = sPlayer.getArmorSlots();
			Iterator<ItemStack> i = playerArmorSet.iterator();
			while (i.hasNext()) {
				ItemStack armorpiece = i.next();
				if (armorpiece.getItem().getRegistryName().getNamespace().toString().equals("redstonemagic")) {
					suitBonus += 1;
				}
			}
		}
		if (suitBonus >= 4) {
			return true;
		}
		return false;
	}

	private int maxManaRegenAmount(ItemStack stack, ServerPlayerEntity sPlayer) {
		int amt = 0;
		if (stack.getItem().getDescriptionId().contains("redstonemagic")) {
			Iterable<ItemStack> playerArmorSet = sPlayer.getArmorSlots();
			Iterator<ItemStack> i = playerArmorSet.iterator();
			while (i.hasNext()) {
				ItemStack armorpiece = i.next();
				if (armorpiece.getItem().getRegistryName().getNamespace().toString().equals("redstonemagic")) {
					if (armorpiece.getItem() instanceof RedstoneArmorItem) {
						RedstoneArmorItem r = (RedstoneArmorItem) armorpiece.getItem();
						amt += r.getDefense();
					}
				}
			}

		}
		return amt;
	}

	private void doArmorMending(ItemStack stack, ServerPlayerEntity sPlayer) {
		if (stack.isDamaged()) {
			Chunk baseChunk = sPlayer.level.getChunkAt(sPlayer.blockPosition());
			IMagicStorage cap = baseChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				int chunkMana = cap.getManaStored();
				if (chunkMana > 64) {
					cap.useMana(4);
					stack.setDamageValue(stack.getDamageValue() - 1);
				}
				Network.sendToClient(new SyncClientManaPacket(MyConfig.NO_PLAYER_MANA_UPDATE, chunkMana), sPlayer);
			}
		}
	}

	private void doSuitBonuses(ServerPlayerEntity sPlayer, long gameTime) {
		final int RESISTANCE_DURATION = 160;
		final int ABSORPTION_DURATION = 6000;

		if (gameTime % RESISTANCE_DURATION == 0 && this.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
			doASuitBonus(sPlayer, Effects.DAMAGE_RESISTANCE, RESISTANCE_DURATION, 0);
		}
		if (gameTime % ABSORPTION_DURATION == 0 && this.getMaterial() == ModItems.REDSTONEMAGIC_LEATHER_MATERIAL) {
			doASuitBonus(sPlayer, Effects.ABSORPTION, ABSORPTION_DURATION, 0);
		}

	}

	private void doASuitBonus(ServerPlayerEntity sPlayer, Effect effect, int effectDuration, int effectIntensity) {
		boolean refreshSuitBonus = true;
		EffectInstance ei = sPlayer.getEffect(effect);
		if (ei != null) {
			if (ei.getAmplifier() > effectIntensity) {
				if (ei.getDuration() <= 15) {
					sPlayer.removeEffectNoUpdate(effect);
				} else {
					refreshSuitBonus = false;
				}
			}
		}
		if (refreshSuitBonus) {
			MyConfig.dbgPrintln(1, "Redstone Magic: " + sPlayer.getName().getString() + " Apply Suit Bonus : "
					+ effect.getRegistryName().getNamespace().toString());
			sPlayer.addEffect(new EffectInstance(effect, effectDuration, effectIntensity, true, true));
		}
	}

	private void doArmorManaRegeneration(ServerPlayerEntity sPlayer, boolean isFullSuit, int suitMaxRegenLevel) {
		float maxManaRegenPct = (float) suitMaxRegenLevel / 100;

		doAnArmorManaRegeneration(sPlayer, maxManaRegenPct, isFullSuit);

	}

	private void doAnArmorManaRegeneration(ServerPlayerEntity sPlayer, float maxSuitManaPct, boolean isFullSuit) {

		int d100Roll = sPlayer.level.getRandom().nextInt(100);
		int manaRegenRate = 0;
		if (this.getMaterial() == ModItems.REDSTONEMAGIC_LEATHER_MATERIAL) {
			if (d100Roll <= 40) {
				return;
			}
			manaRegenRate = 1;
		}
		if (this.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
			if (d100Roll <= 5) {
				return;
			}
			manaRegenRate = 2;
		}
		if (isFullSuit) {
			manaRegenRate += 1;
		}


		
		IMagicStorage cap = sPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			int currentMana = cap.getManaStored();
			int maxNaturalTotalMana = (int) (MyConfig.getMaxPlayerRedstoneMagic() * maxSuitManaPct);
			// MyConfig.sendChat(sPlayer, "Armor Regen: " + manaRegenRate + " maxNaturalRate:" + maxNaturalTotalMana);
			if (cap.getManaStored() < maxNaturalTotalMana) {
				cap.addMana((int) manaRegenRate);
				Network.sendToClient(new SyncClientManaPacket(cap.getManaStored(), MyConfig.NO_CHUNK_MANA_UPDATE),
						(ServerPlayerEntity) sPlayer);
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
