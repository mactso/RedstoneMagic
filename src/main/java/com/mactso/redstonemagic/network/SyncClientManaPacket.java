package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncClientManaPacket {

	private int playerMana;
	private int chunkMana;
	
	public SyncClientManaPacket (int playerMana, int chunkMana)
	{
		this.playerMana = playerMana;
		this.chunkMana = chunkMana;

	}

	public static void processSyncClientManaPacket(SyncClientManaPacket message, Supplier<NetworkEvent.Context> ctx)
	{

		ctx.get().enqueueWork( () -> 
			{
				if (message.playerMana >= 0) RedstoneMagicGuiEvent.setCurrentPlayerRedstoneMana(message.playerMana);
				if (message.chunkMana >= 0) RedstoneMagicGuiEvent.setCurrentChunkRedstoneMana(message.chunkMana);
			}
		);
		ctx.get().setPacketHandled(true);
	}

	public static SyncClientManaPacket readPacketData(FriendlyByteBuf buf)
	{
		int playerMana = buf.readVarInt();
		int chunkMana = buf.readVarInt();
		return new SyncClientManaPacket(playerMana, chunkMana);
	}

	public static void writePacketData(SyncClientManaPacket msg, FriendlyByteBuf buf)
	{
		msg.encode(buf);
	}
	
	public void encode(FriendlyByteBuf buf)
	{

		buf.writeVarInt(playerMana);
		buf.writeVarInt(chunkMana);

	}
}
