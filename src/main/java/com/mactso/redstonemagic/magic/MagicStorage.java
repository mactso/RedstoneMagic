package com.mactso.redstonemagic.magic;

import net.minecraft.world.chunk.Chunk;

public class MagicStorage implements IMagicStorage
{
	Chunk chunk;
	public int energy;

	public MagicStorage(Chunk chunk) {
		this.chunk = chunk;
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
