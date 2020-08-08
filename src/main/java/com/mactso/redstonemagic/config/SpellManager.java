package com.mactso.redstonemagic.config;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class SpellManager {
	public static Hashtable<String, RedstoneMagicSpellItem> redstoneMagicSpellItemHashtable = new Hashtable<>();
	
	public static RedstoneMagicSpellItem getRedstoneMagicSpellItem(String spellKey) {

		if (redstoneMagicSpellItemHashtable.isEmpty()) {
			redstoneMagicSpellInit();
		}

		RedstoneMagicSpellItem s = redstoneMagicSpellItemHashtable.get(spellKey);

		return s;
	}


	public static void redstoneMagicSpellInit() {
		// Key, Translation Key, English Comment, Cost Code, Max Power/Duration, Max Damage
		final String[] defaultSpellValues = 
				{"1,RM.DMG,Red Bolt Damage,C2,4,6;",
				 "2,RM.HEAL,Crimson Heal,C2,4,6;",
				 "3,RM.DOT,Blood Loss DoT,C1,8,3",
				 "4,RM.TWSP,Cardinal Call WorldSpawn,CDis,1,1;",
				 "5,RM.THOM,Ruby Slippers Home,CDis,1,1;",
				 "6,RM.RESI,Chromatic Protection Resistance,C1,1,4;",
				 "7,RM.RCRS,Blood Clean Remove BadEffects,C4,1,1;",
				 "8,RM.SDOT,Blood Snare Slow & DoT,C3,4,2"
				 };
		
		List <String> dTL6464 = new ArrayList<>();
		final String[]  defaultMobBreakPercentageValues;
		
		int i = 0;


		defaultMobBreakPercentageValues = dTL6464.toArray(new String[i]);
		
		i = 0;
		redstoneMagicSpellItemHashtable.clear();
		while (i < defaultSpellValues.length) {
			try {
				StringTokenizer st = new StringTokenizer(defaultSpellValues[i], ",");
				String spellKey = st.nextToken();
				String spellTranslationKey = st.nextToken();
				String spellComment = st.nextToken();
				String spellCostCode = st.nextToken();
				String spellMaxPower = st.nextToken();

				redstoneMagicSpellItemHashtable.put(spellKey, new RedstoneMagicSpellItem(spellTranslationKey, spellComment, spellCostCode, spellMaxPower));
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
		String spellCostCode;
		String spellMaxPower;

		public RedstoneMagicSpellItem(
				String spellTranslationKey,
				String spellComment,
				String spellCostCode,
				String spellMaxPower
				) 
		{
			this.spellTranslationKey = spellTranslationKey;
			this.spellComment  		 = spellComment;
			this.spellCostCode 	 	 = spellCostCode;
			this.spellMaxPower 		 = spellMaxPower;
		}

		public String getSpellTranslationKey() {
			return spellTranslationKey;
		}
		
		public String getSpellCostCode() {
			return spellCostCode;
		}
		
		public String getSpellMaxPower() {
			return spellMaxPower;
		}
		
		public String getSpellComment() {
			return spellComment;
		}

	}

}



