package com.mactso.redstonemagic.sounds;

import com.mactso.redstonemagic.Main;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {
	
	public static final SoundEvent REDSTONEMAGIC_NUKE = create("redstonemagic.nuke");
	public static final SoundEvent REDSTONEMAGIC_HEAL = create("redstonemagic.heal");
	public static final SoundEvent REDSTONEMAGIC_DOT = create("redstonemagic.dot");
	public static final SoundEvent REDSTONEMAGIC_SDOT = create("redstonemagic.sdot");
	public static final SoundEvent REDSTONEMAGIC_RESI = create("redstonemagic.resi");
	public static final SoundEvent REDSTONEMAGIC_TELE = create("redstonemagic.tele");
	public static final SoundEvent REDSTONEMAGIC_BUFF = create("redstonemagic.buff");
	public static final SoundEvent REDSTONEMAGIC_RCRS = create("redstonemagic.rcrs");
	public static final SoundEvent SPELL_RESONATES = create("spell.resonates");	
	public static final SoundEvent SPELL_FAILS = create("spell.fails");	
	public static final SoundEvent GATHERER_HUMS = create("gatherer.hums");	
	public static final SoundEvent RITUAL_PYLON_THRUMS = create("ritual.pylon.hums");	
	private static SoundEvent create(String key)
	{
		ResourceLocation res = new ResourceLocation(Main.MODID, key);
		SoundEvent ret = new SoundEvent(res);
		ret.setRegistryName(res);
		return ret;
	}

	public static void register(IForgeRegistry<SoundEvent> registry)
	{
		registry.register(REDSTONEMAGIC_NUKE);
		registry.register(REDSTONEMAGIC_HEAL);
		registry.register(REDSTONEMAGIC_DOT);
		registry.register(REDSTONEMAGIC_SDOT);
		registry.register(REDSTONEMAGIC_RESI);
		registry.register(REDSTONEMAGIC_TELE);
		registry.register(REDSTONEMAGIC_BUFF);
		registry.register(REDSTONEMAGIC_RCRS);
		registry.register(SPELL_RESONATES);
		registry.register(SPELL_FAILS);
		registry.register(GATHERER_HUMS);		
		registry.register(RITUAL_PYLON_THRUMS);	
	}
}
