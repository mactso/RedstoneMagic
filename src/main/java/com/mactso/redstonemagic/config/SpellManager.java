package com.mactso.redstonemagic.config;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class SpellManager {
	public static Hashtable<String, RedstoneMagicSpellItem> redstoneMagicSpellItemHashtable = new Hashtable<>();
	
	public static RedstoneMagicSpellItem getRedstoneMagicSpellItem(String spellNumberKey) {

		if (redstoneMagicSpellItemHashtable.isEmpty()) {
			redstoneMagicSpellInit();
		}

		RedstoneMagicSpellItem s = redstoneMagicSpellItemHashtable.get(spellNumberKey);

		return s;
		
	}


	public static void redstoneMagicSpellInit() {
		// Key, Translation Key, English Comment, Cost Code, Min power, Max Damage, TargetType
		final String[] defaultSpellValues = 
				{"0,RM.NUKE,Red Bolt,1,4,6,T",
				 "1,RM.HEAL,Crimson Heal,2,4,6,B",
				 "2,RM.DOT,Sepsis,1,8,3,T",
				 "3,RM.SDOT,Crimson Cloud,3,4,2,T",
				 "4,RM.RESI,Rose Resistance,1,1,4,B",
				 "5,RM.TELE,Cardinal Call,1,1,1,S",
				 "6,RM.BUFF,Multi Buff,4,1,4,S",
				 "7,RM.RCRS,Remove Curse,4,1,1,B"
				 };
		
		List <String> dTL6464 = new ArrayList<>();
		final String[]  defaultMobBreakPercentageValues;
		
		int i = 0;


		defaultMobBreakPercentageValues = dTL6464.toArray(new String[i]);
		System.out.println("reinit spell manager");		
		i = 0;
		redstoneMagicSpellItemHashtable.clear();
		while (i < defaultSpellValues.length) {
			try {
				System.out.println(defaultSpellValues[i]);
				StringTokenizer st = new StringTokenizer(defaultSpellValues[i], ",");
				String spellKey = st.nextToken();
				String spellTranslationKey = st.nextToken();
				String spellComment = st.nextToken();
				int spellBaseCost = Integer.parseInt(st.nextToken());
				int spellMinPower = Integer.parseInt(st.nextToken());
				int spellMaxPower = Integer.parseInt(st.nextToken());
				String spellTargetType = st.nextToken();
				
				redstoneMagicSpellItemHashtable.put(spellKey, new RedstoneMagicSpellItem(spellTranslationKey, spellComment, spellBaseCost, spellMinPower, spellMaxPower, spellTargetType));
			} catch (Exception e) {
				System.out.println("RedstoneMagic :  Error on Spell: " + defaultMobBreakPercentageValues[i]);
			}
			i++;
		}
	}

	// keeps track of the spawner break percentage by mod:mob key.
	public static class RedstoneMagicSpellItem {
		String spellTranslationKey;
		String spellComment;
		int spellBaseCost;
		int spellMinPower;
		int spellMaxPower;
		String spellTargetType;

		public RedstoneMagicSpellItem(
				String spellTranslationKey,
				String spellComment,
				int spellBaseCost,
				int spellMinPower,
				int spellMaxPower,
				String spellTargetType
				) 
		{
			this.spellTranslationKey = spellTranslationKey;
			this.spellComment  		 = spellComment;
			this.spellBaseCost 	 	 = spellBaseCost;
			this.spellMinPower 		 = spellMinPower;
			this.spellMaxPower 		 = spellMaxPower;
			this.spellTargetType     = spellTargetType;
		}

		public String getSpellTranslationKey() {
			return spellTranslationKey;
		}
		
		public int getSpellBaseCost() {
			return spellBaseCost;
		}
	

		public int getSpellMinPower() {
			return spellMaxPower;
		}
		public int getSpellMaxPower() {
			return spellMaxPower;
		}
		
		public String getSpellComment() {
			return spellComment;
		}

		public String getSpellTargetType() {
			return spellTargetType;
		}

	}

}



