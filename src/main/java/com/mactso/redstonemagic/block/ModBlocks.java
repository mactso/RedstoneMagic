package com.mactso.redstonemagic.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block RITUAL_PYLON= new RitualPylon(Properties.of(Material.GLASS).strength(0.2F).sound(SoundType.METAL)).setRegistryName("ritual_pylon");
	public static final Block GATHERER = new Gatherer(Properties.of(Material.GLASS).strength(0.2F).sound(SoundType.METAL)
			.lightLevel((state) -> {
			    return 9;
			 }))
			.setRegistryName("gatherer");
	public static final Block TEST_BLOCK2 = new Gatherer(Properties.of(Material.GLASS).strength(0.2F).sound(SoundType.WOOD)).setRegistryName("test_block2");
	public static final Block LIGHT_SPELL = new LightSpell(Properties.of(Material.GLASS, MaterialColor.COLOR_LIGHT_GRAY).noCollission().noDrops().instabreak().sound(SoundType.LANTERN).noOcclusion().lightLevel((state) -> {
			    return 14;
			 })).setRegistryName("light_spell");

	public static void register(IForgeRegistry<Block> forgeRegistry	)
	{
		forgeRegistry.register(RITUAL_PYLON);
		forgeRegistry.register(GATHERER);
		forgeRegistry.register(LIGHT_SPELL);
		forgeRegistry.register(TEST_BLOCK2);
		
	}

	@OnlyIn(Dist.CLIENT)
	public static void setRenderLayer()
	{
//	RenderTypeLookup.setRenderLayer(RITUAL_PYLON, RenderType.getEntitySolid(locationIn));
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(BlockColors blockColors)
	{

//		blockColors.register((blockstate, lightreader, pos, index) -> {
//			return RITUAL_PYLON.colorMultiplier(blockstate.get(RITUAL_PYLON.POWER));
//		}, RITUAL_PYLON);
//
//		blockColors.addColorState(RITUAL_PYLON.POWER, RITUAL_PYLON);
	}
}
