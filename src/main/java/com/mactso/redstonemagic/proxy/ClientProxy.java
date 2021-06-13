package com.mactso.redstonemagic.proxy;

import com.mactso.redstonemagic.item.RedstoneFocusItem;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class ClientProxy implements IProxy{

	@Override
	public void setFlyingValues(boolean isFlying, boolean isChunkFlying, long chunkAge) {
		
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		
		RedstoneFocusItem.setIsFlying(player, isFlying, chunkAge);
		RedstoneFocusItem.setIsChunkFlying(player, isChunkFlying);
		RedstoneFocusItem.setChunkAge(player, chunkAge);

		
	}

}
