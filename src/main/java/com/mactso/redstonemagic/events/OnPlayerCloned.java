package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OnPlayerCloned {
    @SubscribeEvent
    public void onPlayerCloned(Clone event)
    {

		if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
			return;
		}
	
    	ServerPlayerEntity newPlayer = (ServerPlayerEntity) event.getPlayer();
    	ServerPlayerEntity oldPlayer = (ServerPlayerEntity) event.getOriginal();
    	IMagicStorage capNew = newPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
    	IMagicStorage capOld = oldPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
    	int manaStored = capOld.getManaStored();
    	capNew.setMana(manaStored);
		Network.sendToClient(new SyncClientManaPacket(manaStored, -1) , newPlayer);
    }
}
