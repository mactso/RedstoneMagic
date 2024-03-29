package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.mana.MagicProvider;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// may also need to get onClonePlayer events.
public class OnServerPlayerEvent {
	private static final ResourceLocation KEY = new ResourceLocation(Main.MODID, "magic_capability");

	 @SubscribeEvent
	 public void onPlayer(AttachCapabilitiesEvent <Entity> event)
	 {
		 ServerPlayer serverPlayerEntity;
		 if (event.getObject() instanceof ServerPlayer) {
			 serverPlayerEntity = (ServerPlayer) event.getObject();
		 } else {
			 return;
		 }
		 LazyOptional<IMagicStorage> optPlayer = serverPlayerEntity.getCapability(CapabilityMagic.MAGIC);
		 if (optPlayer.isPresent()) {
			 System.out.println ("Redstone Magic.OnPlayer(): Player already as capability and this should never happen.");
			 return;
		 }
		 else {
			 event.addCapability(KEY, new MagicProvider(serverPlayerEntity));
		 }
		    


	 //	 String message = String.format("Hello there, you have �7%d�r mana left.", (int) mana.getMana());
	 //	 player.addChatMessage(new TextComponentString(message));
	 }
}
