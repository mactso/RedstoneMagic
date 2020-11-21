package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities {

	public static final TileEntityType<RitualPylonTileEntity> RITUAL_PYLON = create(
			"ritual_pylon", TileEntityType.Builder
					.create(RitualPylonTileEntity::new, ModBlocks.RITUAL_PYLON).build(null));

	public static final TileEntityType<GathererTileEntity> GATHERER = create(
			"gatherer", TileEntityType.Builder
					.create(GathererTileEntity::new, ModBlocks.GATHERER).build(null));

	public static <T extends TileEntity> TileEntityType<T> create(String key, TileEntityType<T> type) {
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<TileEntityType<?>> forgeRegistry) {
		forgeRegistry.register(RITUAL_PYLON);
		forgeRegistry.register(GATHERER);
	}
}
