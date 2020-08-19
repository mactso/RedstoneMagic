package com.mactso.redstonemagic.mana;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MagicProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT>
{
	IMagicStorage storage;

	public MagicProvider(Chunk chunk) {
		storage = new MagicStorage(chunk);
	}

	public MagicProvider (ServerPlayerEntity serverPlayerEntity) {
		storage = new MagicStorage(serverPlayerEntity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityMagic.MAGIC)
			return (LazyOptional<T>) LazyOptional.of(() -> storage);
		return LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT ret = new CompoundNBT();
		ret.putInt("magicStored", storage.getManaStored());
		return ret;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		int magic = nbt.getInt("magicStored");
		storage.addMana(magic);
	}
}
