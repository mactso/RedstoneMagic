package com.mactso.redstonemagic.mana;

import com.mactso.redstonemagic.config.MyConfig;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;

public class MagicStorage implements IMagicStorage
{
	Object object;
	private int manaStored;

	public MagicStorage(Object object) {
		this.object = object;
	}

	
	@Override
	public int getManaStored() {
		return this.manaStored;
	}

	@Override
	public void setMana(int amount) {
		this.manaStored = amount;
	}
	
	@Override
	public void addMana(int amount) {
		int max = 0;
		String objectType = "";
		
		this.manaStored += amount;
		if (object instanceof ServerPlayerEntity) {
			max = MyConfig.getMaxPlayerRedstoneMagic();
			if (max==0) {
				max = 256;
			}
			objectType = "Player";
		} else 
		if (object instanceof Chunk ){
			max = MyConfig.getMaxChunkRedstoneMagic();
			if (max==0) {
				max = 25600;
			}
			objectType = "Chunk";
		}

		if (this.manaStored > max) {
			this.manaStored = max;
		}
	}
	
	@Override
	public boolean useMana (int amount) {
		if (amount <= this.manaStored) {
			this.manaStored -= amount;
			return true;
		}
		return false;
	}



}
