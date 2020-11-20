package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities {

	public static final TileEntityType<RedstoneMagicPylonMinorTileEntity> REDSTONE_MAGIC_PYLON_MINOR = create(
			"redstone_magic_pylon_minor", TileEntityType.Builder
					.create(RedstoneMagicPylonMinorTileEntity::new, ModBlocks.REDSTONE_MAGIC_PYLON_MINOR).build(null));

	public static final TileEntityType<RedstoneMagicGathererTileEntity> REDSTONE_MAGIC_GATHERER = create(
			"gatherer", TileEntityType.Builder
					.create(RedstoneMagicGathererTileEntity::new, ModBlocks.REDSTONE_MAGIC_GATHERER).build(null));

	public static <T extends TileEntity> TileEntityType<T> create(String key, TileEntityType<T> type) {
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<TileEntityType<?>> forgeRegistry) {
		forgeRegistry.register(REDSTONE_MAGIC_PYLON_MINOR);
		forgeRegistry.register(REDSTONE_MAGIC_GATHERER);
	}
}
