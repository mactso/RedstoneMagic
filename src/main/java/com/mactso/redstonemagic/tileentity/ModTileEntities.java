package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModTileEntities {

	public static final BlockEntityType<RitualPylonTileEntity> RITUAL_PYLON = 
			BlockEntityType.Builder
					.of(RitualPylonTileEntity::new, ModBlocks.RITUAL_PYLON).build(null);

	public static final BlockEntityType<GathererTileEntity> GATHERER = 
			 BlockEntityType.Builder
					.of(GathererTileEntity::new, ModBlocks.GATHERER).build(null);
		
	public static void register(IForgeRegistry<BlockEntityType<?>> forgeRegistry) {
		forgeRegistry.register("ritual_pylon", RITUAL_PYLON);
		forgeRegistry.register("gatherer", GATHERER);
	}
}
