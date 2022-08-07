package com.mactso.redstonemagic.mana;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class CapabilityMagic
{
    public static final Capability<IMagicStorage> MAGIC = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
    	event.register(IMagicStorage.class);
    }
}
