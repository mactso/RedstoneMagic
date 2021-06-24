package com.mactso.redstonemagic.mana;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityMagic
{
    @CapabilityInject(IMagicStorage.class)
    public static Capability<IMagicStorage> MAGIC = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMagicStorage.class, new IStorage<IMagicStorage>()
        {
            @Override
            public INBT writeNBT(Capability<IMagicStorage> capability, IMagicStorage instance, Direction side)
            {
                return IntNBT.valueOf(instance.getManaStored());
            }

            @Override
            public void readNBT(Capability<IMagicStorage> capability, IMagicStorage instance, Direction side, INBT nbt)
            {
                if (!(instance instanceof MagicStorage))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                ((MagicStorage)instance).addMana(((IntNBT)nbt).getAsInt());
            }
        },
        () -> new MagicStorage(null));
    }
}
