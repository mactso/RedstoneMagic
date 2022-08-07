package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities {

	public static final BlockEntityType<RitualPylonTileEntity> RITUAL_PYLON = create(
			"ritual_pylon", BlockEntityType.Builder
					.of(RitualPylonTileEntity::new, ModBlocks.RITUAL_PYLON).build(null));

	public static final BlockEntityType<GathererTileEntity> GATHERER = create(
			"gatherer", BlockEntityType.Builder
					.of(GathererTileEntity::new, ModBlocks.GATHERER).build(null));

	public static <T extends BlockEntity> BlockEntityType<T> create(String key, BlockEntityType<T> type) {
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<BlockEntityType<?>> forgeRegistry) {
		forgeRegistry.register(RITUAL_PYLON);
		forgeRegistry.register(GATHERER);
	}
}
