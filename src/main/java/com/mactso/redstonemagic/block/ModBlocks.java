package com.mactso.redstonemagic.block;

import com.mactso.redstonemagic.tileentity.GathererTileEntity;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block RITUAL_PYLON= new RitualPylon(Properties.create(Material.GLASS).hardnessAndResistance(0.2F).sound(SoundType.METAL)).setRegistryName("ritual_pylon");
	public static final Block GATHERER = new Gatherer(Properties.create(Material.GLASS).hardnessAndResistance(0.2F).sound(SoundType.METAL)
			.setLightLevel((state) -> {
			    return 9;
			 }))
			.setRegistryName("gatherer");
	public static final Block TEST_BLOCK2 = new Gatherer(Properties.create(Material.GLASS).hardnessAndResistance(0.2F).sound(SoundType.WOOD)).setRegistryName("test_block2");
//	public static final Block REDSTONE_POWER_BLOCK = new RedstonePowerBlock(Properties.create(Material.IRON).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)).setRegistryName("redstone_power_block");

	//	public static final Block REDSTONE_POWER_BLOCK = new RedstonePowerBlock(Properties.create(Material.IRON).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)).setRegistryName("redstone_power_block");
//	public static final Block REDSTONE_ENERGY_BLOCK = new RedstoneEnergyBlock(Properties.create(Material.ROCK).hardnessAndResistance(3.5F)).setRegistryName("redstone_energy_block");

	public static void register(IForgeRegistry<Block> forgeRegistry	)
	{
		forgeRegistry.register(RITUAL_PYLON);
		forgeRegistry.register(GATHERER);
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
