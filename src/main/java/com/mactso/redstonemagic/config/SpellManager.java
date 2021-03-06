package com.mactso.redstonemagic.config;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

public class SpellManager {
	public final static String SPELL_TARGET_SELF = "S";
	public final static String SPELL_TARGET_OTHER = "O";
	public final static String SPELL_TARGET_BOTH = "B";
	public static Hashtable<String, RedstoneMagicSpellItem> redstoneMagicSpellItemHashtable = new Hashtable<>();
	
	public static RedstoneMagicSpellItem getRedstoneMagicSpellItem(String spellNumberKey) {

		if (redstoneMagicSpellItemHashtable.isEmpty()) {
			redstoneMagicSpellInit();
		}

		RedstoneMagicSpellItem s = redstoneMagicSpellItemHashtable.get(spellNumberKey);

		return s;
		
	}


	public static void redstoneMagicSpellInit() {
		// Key, Translation Key, English Comment, BaseCost, Target Type (B,O,S)
		final String[] defaultSpellValues = 
				{"0,redstonemagic.nuke,Redstone Bolt,2,O",
				 "1,redstonemagic.heal,Scarlet Heal,2,B",
				 "2,redstonemagic.dot,Sepsis,2,O",
				 "3,redstonemagic.sdot,Crimson Cloud,3,O",
				 "4,redstonemagic.resi,Ruby Shield,1,B",
				 "5,redstonemagic.tele,Recall,3,B",
				 "6,redstonemagic.buff,Ancient Blessings,2,B",
				 "7,redstonemagic.rcrs,Remove Curse,1,B"
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
				String spellTargetType = st.nextToken();
				
				redstoneMagicSpellItemHashtable.put(spellKey, new RedstoneMagicSpellItem(spellTranslationKey, spellComment, spellBaseCost, spellTargetType));
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
		String spellTargetType;

		public RedstoneMagicSpellItem(
				String spellTranslationKey,
				String spellComment,
				int spellBaseCost,
				String spellTargetType
				) 
		{
			this.spellTranslationKey = spellTranslationKey;
			this.spellComment  		 = spellComment;
			this.spellBaseCost 	 	 = spellBaseCost;
			this.spellTargetType     = spellTargetType;
		}

		public String getSpellTranslationKey() {
			return spellTranslationKey;
		}
		
		public int getSpellBaseCost() {
			return spellBaseCost;
		}
		
		public String getSpellComment() {
			return spellComment;
		}

		public String getSpellTargetType() {
			return spellTargetType;
		}

	}

}



