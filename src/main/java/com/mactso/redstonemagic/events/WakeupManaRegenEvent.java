package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.item.ModItems;
import com.mactso.redstonemagic.item.RedstoneArmorItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class WakeupManaRegenEvent {

	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
		if (event.getPlayer().level instanceof ServerLevel) {

			Iterable<ItemStack> ar = event.getPlayer().getArmorSlots();
			float armorModifier = 1.0f;
			for (ItemStack is : ar) {
				armorModifier -= 0.24f;
				if (is.getItem() instanceof RedstoneArmorItem) {
					RedstoneArmorItem r = (RedstoneArmorItem) is.getItem();
					if (r.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
						armorModifier += 0.23f;
					} else if (r.getMaterial() == ModItems.REDSTONEMAGIC_LEATHER_MATERIAL) {
						armorModifier += 0.13f;
					}
				}
//				MyConfig.sendChat(event.getPlayer(),  "Wakeup Regen ArmorModifier:" + armorModifier 	);
			}
			IMagicStorage pMS = event.getPlayer().getCapability(CapabilityMagic.MAGIC).orElse(null);
			int maxMana = MyConfig.getMaxPlayerRedstoneMagic();
			float netModifier = (float) (armorModifier * MyConfig.getWakeupManaRegenPercent());
			float wakeupManaRegenAmount = (maxMana * netModifier);
			if (wakeupManaRegenAmount != 0 && wakeupManaRegenAmount < 1) {
				wakeupManaRegenAmount = 1;
			}
			pMS.addMana((int) wakeupManaRegenAmount );
			Network.sendToClient(new SyncClientManaPacket(pMS.getManaStored(), MyConfig.NO_CHUNK_MANA_UPDATE),
					(ServerPlayer) event.getPlayer());
//			MyConfig.sendChat(event.getPlayer(),  "Wakeup Regen:" + wakeupManaRegenAmount +
//					" ArmorModifier:" + armorModifier +
//					" netModifier:" + netModifier);
		}
	}
}
