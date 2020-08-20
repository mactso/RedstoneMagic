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
		return manaStored;
	}

	@Override
	public void setMana(int amount) {
		manaStored = amount;
	}
	
	@Override
	public void addMana(int amount) {
		int max = 0;
		String objectType = "";
		
		this.manaStored += amount;
		if (object instanceof ServerPlayerEntity) {
			max = MyConfig.maxPlayerRedstoneMagic;
			objectType = "Player";
		} else 
		if (object instanceof Chunk ){
			max = MyConfig.maxChunkRedstoneMagic;
			objectType = "Chunk";
		}

		if (this.manaStored > max) {
			this.manaStored = max;
		}
		MyConfig.dbgPrintln(objectType + " mana increased by " + amount + " to " + manaStored + ".");    			
	}
	
	@Override
	public boolean useMana (int amount) {
		if (amount<= manaStored) {
			manaStored = manaStored - amount;
			MyConfig.dbgPrintln("Spell cast for "+amount+"mana, leaving "+manaStored+" mana..");    
			return true;
		}
		MyConfig.dbgPrintln(manaStored + " Not enough mana to cast spell.");    			
		return false;
	}



}
