package com.mactso.redstonemagic.sounds;

import com.mactso.redstonemagic.Main;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {
	
	public static final SoundEvent REDSTONEMAGIC_NUKE = create("redstonemagic.nuke");
	public static final SoundEvent REDSTONEMAGIC_NUKE_ICY = create("redstonemagic.nuke.icy");
	public static final SoundEvent REDSTONEMAGIC_HEAL = create("redstonemagic.heal");
	public static final SoundEvent REDSTONEMAGIC_DOT = create("redstonemagic.dot");
	public static final SoundEvent REDSTONEMAGIC_SDOT = create("redstonemagic.sdot");
	public static final SoundEvent REDSTONEMAGIC_RESI = create("redstonemagic.resi");
	public static final SoundEvent REDSTONEMAGIC_TELE = create("redstonemagic.tele");
	public static final SoundEvent REDSTONEMAGIC_TELE_START = create("redstonemagic.tele.start");
	public static final SoundEvent REDSTONEMAGIC_BUFF = create("redstonemagic.buff");
	public static final SoundEvent REDSTONEMAGIC_RCRS = create("redstonemagic.rcrs");
	
	public static final SoundEvent REDSTONEMAGIC_FLY = create("redstonemagic.fly");
	public static final SoundEvent REDSTONEMAGIC_LIGHT = create("redstonemagic.light");
	
	public static final SoundEvent SPELL_RESONATES = create("spell.resonates");	
	public static final SoundEvent SPELL_FAILS = create("spell.fails");	
	public static final SoundEvent GATHERER_HUMS = create("gatherer.hums");	
	public static final SoundEvent GATHERER_GATHERS = create("gatherer.gathers");	
	public static final SoundEvent RITUAL_PYLON_THRUMS = create("ritual.pylon.thrums");	
	public static final SoundEvent RED_SPIRIT_WORKS = create("red.spirit.works");	
	public static final SoundEvent RITUAL_BEGINS = create("ritual.begins");	
	public static final SoundEvent RITUAL_ENDS = create("ritual.ends");	
	
	private static SoundEvent create(String key)
	{
		ResourceLocation res = new ResourceLocation(Main.MODID, key);
		SoundEvent ret = new SoundEvent(res);
		return ret;
	}

	
	
	public static void registerHelper(IForgeRegistry<SoundEvent> registry, SoundEvent s)
	{
		registry.register(s.getLocation(), s);
	}

	
	
	public static void register(IForgeRegistry<SoundEvent> registry)
	{
		registerHelper(registry,REDSTONEMAGIC_NUKE);
		registerHelper(registry,REDSTONEMAGIC_NUKE_ICY);
		registerHelper(registry,REDSTONEMAGIC_HEAL);
		registerHelper(registry,REDSTONEMAGIC_DOT);
		registerHelper(registry,REDSTONEMAGIC_SDOT);
		registerHelper(registry,REDSTONEMAGIC_RESI);
		registerHelper(registry,REDSTONEMAGIC_TELE);
		registerHelper(registry,REDSTONEMAGIC_TELE_START);
		registerHelper(registry,REDSTONEMAGIC_BUFF);
		registerHelper(registry,REDSTONEMAGIC_RCRS);
		registerHelper(registry,REDSTONEMAGIC_FLY);

		registerHelper(registry,SPELL_RESONATES);
		registerHelper(registry,SPELL_FAILS);
		registerHelper(registry,GATHERER_HUMS);		
		registerHelper(registry,GATHERER_GATHERS);	
		registerHelper(registry,RITUAL_PYLON_THRUMS);	
		registerHelper(registry,RITUAL_BEGINS);	
		registerHelper(registry,RITUAL_ENDS);	
		registerHelper(registry,RED_SPIRIT_WORKS);	
	}
}
