package com.mactso.redstonemagic.block;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks
{
	public static final Block REDSTONE_MAGIC_PYLON_MINOR= new RedstoneMagicPylonMinor(Properties.create(Material.GLASS).hardnessAndResistance(0.2F).sound(SoundType.WOOD)).setRegistryName("redstone_magic_pylon_minor");
//	public static final Block REDSTONE_POWER_BLOCK = new RedstonePowerBlock(Properties.create(Material.IRON).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)).setRegistryName("redstone_power_block");
//	public static final Block REDSTONE_ENERGY_BLOCK = new RedstoneEnergyBlock(Properties.create(Material.ROCK).hardnessAndResistance(3.5F)).setRegistryName("redstone_energy_block");

	public static void register(IForgeRegistry<Block> forgeRegistry	)
	{
		forgeRegistry.register(REDSTONE_MAGIC_PYLON_MINOR);

	}

	@OnlyIn(Dist.CLIENT)
	public static void setRenderLayer()
	{
//	RenderTypeLookup.setRenderLayer(REDSTONE_MAGIC_PYLON_MINOR, RenderType.getEntitySolid(locationIn));
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(BlockColors blockColors)
	{

//		blockColors.register((blockstate, lightreader, pos, index) -> {
//			return REDSTONE_MAGIC_PYLON_MINOR.colorMultiplier(blockstate.get(REDSTONE_MAGIC_PYLON_MINOR.POWER));
//		}, REDSTONE_MAGIC_PYLON_MINOR);
//
//		blockColors.addColorState(REDSTONE_MAGIC_PYLON_MINOR.POWER, REDSTONE_MAGIC_PYLON_MINOR);
	}
}
