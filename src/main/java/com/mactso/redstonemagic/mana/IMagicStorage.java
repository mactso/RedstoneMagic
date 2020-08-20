package com.mactso.redstonemagic.mana;

public interface IMagicStorage
{
	public int getManaStored();
	public void addMana(int amount);
	public void setMana(int amount);
	// this takes a positive integer.  It returns true if there was enough magical power
	public boolean useMana(int amount);
}
