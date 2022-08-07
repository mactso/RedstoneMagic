package com.mactso.redstonemagic.proxy;

import com.mactso.redstonemagic.item.RedstoneFocusItem;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy implements IProxy{

	@Override
	public void setFlyingValues(boolean isFlying, boolean isChunkFlying, long chunkAge) {
		
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		
		RedstoneFocusItem.setIsFlying(player, isFlying, chunkAge);
		RedstoneFocusItem.setIsChunkFlying(player, isChunkFlying);
		RedstoneFocusItem.setChunkAge(player, chunkAge);

		
	}

}
