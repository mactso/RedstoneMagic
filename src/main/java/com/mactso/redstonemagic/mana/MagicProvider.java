package com.mactso.redstonemagic.mana;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class MagicProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag>
{
	IMagicStorage storage;

	public MagicProvider(LevelChunk chunk) {
		storage = new MagicStorage(chunk);
	}

	public MagicProvider (ServerPlayer serverPlayerEntity) {
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
	public CompoundTag serializeNBT() {
		CompoundTag ret = new CompoundTag();
		ret.putInt("magicStored", storage.getManaStored());
		return ret;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		int magic = nbt.getInt("magicStored");
		storage.addMana(magic);
	}
}
