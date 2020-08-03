package com.mactso.redstonemagic.magic;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;

public class MagicStorage implements IMagicStorage
{
	Object object;
	public int energy;

	public MagicStorage(Object object) {
		this.object = object;
	}

	
	@Override
	public int getMagicStored() {
		return energy;
	}

	@Override
	public void addMagic(int amount) {
		energy += amount;
	}
}
