package com.mactso.redstonemagic.magic;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;

public class MagicStorage implements IMagicStorage
{
	Chunk chunk;
	ServerPlayerEntity serverPlayerEntity;
	public int energy;

	public MagicStorage(Chunk chunk) {
		this.chunk = chunk;
	}

	public MagicStorage(ServerPlayerEntity serverPlayerEntity) {
		this.serverPlayerEntity = serverPlayerEntity;
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
