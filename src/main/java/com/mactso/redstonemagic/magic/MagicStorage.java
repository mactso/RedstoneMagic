package com.mactso.redstonemagic.magic;

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
	public void addMana(int amount) {
		int max = 0;
		String objectType = "";
		if (object instanceof ServerPlayerEntity) {
			max = MyConfig.maxPlayerRedstoneMagic;
			if (MyConfig.debugLevel > 1 ) {
				objectType = "Player";
				System.out.println("Add " + amount + " mana to " + objectType + ".");    			
			}
		} else 
		if (object instanceof Chunk ){
			max = MyConfig.maxChunkRedstoneMagic;
			if (MyConfig.debugLevel > 1 ) {
				objectType = "Chunk";
				System.out.println("Add " + amount + " mana to " + objectType + ".");    			
			}
		}
		if ( this.manaStored + amount  < max ) {
			this.manaStored += amount;
			if (MyConfig.debugLevel > 1 ) {
				System.out.println(objectType + " mana increased by " + amount + " to " + manaStored + ".");    			
			}
		} else {
			manaStored = max;
			if (MyConfig.debugLevel > 1 ) {
				System.out.println(objectType + " mana increased by " + amount + " to " + manaStored + ".");    			
			}
		}
	}
	
	@Override
	public boolean useMana (int amount) {
		if (amount<= manaStored) {
			manaStored = manaStored - amount;
			System.out.println("Spell cast for "+amount+"mana, leaving "+manaStored+" mana..");    
			return true;
		}
		if (MyConfig.debugLevel > 1 ) {
			System.out.println(manaStored + " Not enough mana to cast spell.");    			
		}
		return false;
	}



}
