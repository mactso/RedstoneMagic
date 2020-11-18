package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OnPlayerLoggedIn {

	   @SubscribeEvent
	    public void onPlayerLogged( PlayerLoggedInEvent event )
	    {
	    	if (event.getPlayer() == null) {
	    		return;
	    	}
	    	

			if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
				return;
			}
	    	ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getPlayer();
			IMagicStorage cap = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				Network.sendToClient(new SyncClientManaPacket(cap.getManaStored(), -1) , serverPlayer);
			}
	    }

}
