package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OnPlayerLoggedIn {

	   @SubscribeEvent
	    public void onPlayerLogged( PlayerLoggedInEvent event )
	    {
	    	if (event.getEntity() == null) {
	    		return;
	    	}
	    	

			if (!(event.getEntity() instanceof ServerPlayer)) {
				return;
			}
	    	ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
			IMagicStorage cap = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				Network.sendToClient(new SyncClientManaPacket(cap.getManaStored(), -1) , serverPlayer);
			}
	    }

}
