package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MyBreakEvent {

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		BlockState state = event.getState();
		Block block = state.getBlock();
		boolean manaChanged = false;
		int playerMana = -1;
		int chunkMana = -1;

		if (block != Blocks.REDSTONE_ORE) {
			return;
		}
		if (event.getExpToDrop() == 0) {
			return;
		}
		if (event.getPlayer() == null) {
			return;
		}
		ServerPlayer serverPlayerEntity = (ServerPlayer) event.getPlayer();

		if (!(serverPlayerEntity instanceof ServerPlayer)) {
			return;
		}

		int bonusLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,
				event.getPlayer().getMainHandItem());

		BlockPos pos = event.getPos();
		LevelAccessor world = event.getLevel();
		ChunkAccess ichunk = world.getChunk(pos);
		double randint = world.getRandom().nextDouble();
		randint *= 3.0;
		
		int redstoneMagicIncrease = 3 + (int) randint + (bonusLevel*2);
		redstoneMagicIncrease = 30;
		IMagicStorage cap = serverPlayerEntity.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (cap != null) {
			cap.addMana(redstoneMagicIncrease); // checks for max capacity internally based on object type.
			playerMana = cap.getManaStored();
			manaChanged = true;
		}

		if (ichunk instanceof LevelChunk) {
			LevelChunk chunk = (LevelChunk) ichunk;
			cap = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (cap != null) {
				if (cap.getManaStored() + redstoneMagicIncrease <= MyConfig.getMaxChunkRedstoneMagic()) {
					cap.addMana(redstoneMagicIncrease);
					chunkMana = cap.getManaStored();
					manaChanged = true;
				}
				MyConfig.dbgPrintln(1, "Increase Redstone Chunk Magic by " + redstoneMagicIncrease + " to "
								+ cap.getManaStored() + ".");
			}
		}
		Network.sendToClient(new SyncClientManaPacket(playerMana, chunkMana), serverPlayerEntity);
	}
}
