package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.Main;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.spells.CastSpells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
				if (message.playerMana >= 0) Main.setCurrentPlayerRedstoneMana(message.playerMana);
				if (message.chunkMana >= 0) Main.setCurrentChunkRedstoneMana(message.chunkMana);
			}
		);
		ctx.get().setPacketHandled(true);
	}

	public static SyncClientManaPacket readPacketData(PacketBuffer buf)
	{
		int playerMana = buf.readVarInt();
		int chunkMana = buf.readVarInt();
		return new SyncClientManaPacket(playerMana, chunkMana);
	}

	public static void writePacketData(SyncClientManaPacket msg, PacketBuffer buf)
	{
		msg.encode(buf);
	}
	
	public void encode(PacketBuffer buf)
	{

		buf.writeVarInt(playerMana);
		buf.writeVarInt(chunkMana);

	}
}
