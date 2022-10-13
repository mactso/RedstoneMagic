package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OnPlayerCloned {
    @SubscribeEvent
    public void onPlayerCloned(Clone event)
    {

		if (!(event.getEntity() instanceof ServerPlayer)) {
			return;
		}
	
    	ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
    	ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();
    	IMagicStorage capNew = newPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
    	IMagicStorage capOld = oldPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
    	int manaStored = capOld.getManaStored();
    	capNew.setMana(manaStored);
		Network.sendToClient(new SyncClientManaPacket(manaStored, -1) , newPlayer);
    }
}
