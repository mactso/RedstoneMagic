package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;
import com.mactso.redstonemagic.magic.MagicProvider;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class onServerPlayerEvent {

	 @SubscribeEvent
	 public void onPlayer(AttachCapabilitiesEvent <ServerPlayerEntity> event)
	 {
		 event.addCapability(new ResourceLocation(Main.MODID, "magic_capability"), new MagicProvider(event.getObject()));
		    


	 //	 String message = String.format("Hello there, you have §7%d§r mana left.", (int) mana.getMana());
	 //	 player.addChatMessage(new TextComponentString(message));
	 }
}
