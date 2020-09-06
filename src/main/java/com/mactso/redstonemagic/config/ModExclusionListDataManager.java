package com.mactso.redstonemagic.config;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ModExclusionListDataManager {

	public static Hashtable<String, ExcludedModListItem> excludedModListHashtable = new Hashtable<>();
	private static String defaultModExclusionListString = "no_excluded_mod";
	private static String defaultModExclusionListKey = defaultModExclusionListString;

	public static ExcludedModListItem getModExclusionListItem(String key) {
		// prevent excluding "minecraft".
		if (key.equals("minecraft")) {
			return null;
		}
		String iKey = key;
		if (excludedModListHashtable.isEmpty()) {
			excludedModsListInit();
		}

		ExcludedModListItem r = excludedModListHashtable.get(iKey);

		return r;
	}

	public static String getExcludedModsListHashAsString() {
		String returnString="";
		String excludedModName;
		for (String key:excludedModListHashtable.keySet()) {
			excludedModName = excludedModListHashtable.get(key).excludedModName;
			String tempString = key+"," + excludedModName + ";";
			returnString += tempString;
		}
		return returnString;
	
	}

	public static void excludedModsListInit() {
		
		List <String> dTL6464 = new ArrayList<>();
		
		int i = 0;
		String excludedModListLine6464 = "";
		// Forge Issue 6464 patch.
		StringTokenizer st6464 = new StringTokenizer(MyConfig.defaultModExclusionList6464, ";");

		while (st6464.hasMoreElements()) {
			excludedModListLine6464 = st6464.nextToken().trim();
			if (excludedModListLine6464.isEmpty()) continue;
			dTL6464.add(excludedModListLine6464);  
			i++;
		}

		MyConfig.defaultModExclusionList = dTL6464.toArray(new String[i]);

		i = 0;
		excludedModListHashtable.clear();
		while (i < MyConfig.defaultModExclusionList.length) {
			try {
				StringTokenizer st = new StringTokenizer(MyConfig.defaultModExclusionList[i], ",");
				String excludedModListKey = st.nextToken();
				String key = excludedModListKey;				
				excludedModListHashtable.put(key, new ExcludedModListItem(excludedModListKey));
			} catch (Exception e) {
				System.out.println("Redstone Magic Debug:  Exception thrown for mod exclusion name config : " + MyConfig.defaultModExclusionList[i]);
			}
			i++;
		}

	}

	public static class ExcludedModListItem {
		String excludedModName;  // a block like "minecraft:block" or "oby:dirt"
		
		public ExcludedModListItem(String excludedModName) {
			this.excludedModName =  excludedModName;
		}

		public String getExcludedModName() {
			return excludedModName.toLowerCase();
		}

	}
	
}
