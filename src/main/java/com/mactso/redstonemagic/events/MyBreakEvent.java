package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
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
		ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) event.getPlayer();

		if (!(serverPlayerEntity instanceof ServerPlayerEntity)) {
			return;
		}

		int bonusLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,
				event.getPlayer().getMainHandItem());

		BlockPos pos = event.getPos();
		IWorld world = event.getWorld();
		IChunk ichunk = world.getChunk(pos);
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

		if (ichunk instanceof Chunk) {
			int debug = 6;
			Chunk chunk = (Chunk) ichunk;
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
