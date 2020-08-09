package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.spelltargets.Mobs;

//import com.lupicus.bk.Mobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RedstoneMagicPacket 
{
	private int cmd;
	private int id;
	
	public RedstoneMagicPacket(int cmd, int id)
	{
		this.cmd = cmd;
		this.id = id;
	}
	
	public void encode(PacketBuffer buf)
	{
		buf.writeByte(cmd);
		buf.writeVarInt(id);
	}

	public static RedstoneMagicPacket readPacketData(PacketBuffer buf)
	{
		int cmd = buf.readByte();
		int id = buf.readVarInt();
		return new RedstoneMagicPacket(cmd, id);
	}

	public static void writePacketData(RedstoneMagicPacket msg, PacketBuffer buf)
	{
		msg.encode(buf);
	}

	public static void processRedstoneMagicPacket(RedstoneMagicPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ServerPlayerEntity serverPlayer = ctx.get().getSender();
		Entity entity = serverPlayer.world.getEntityByID(message.id);
		if (entity instanceof LivingEntity)
		{
			ctx.get().enqueueWork( () -> 
			{
				Mobs.processCastSpells(message.cmd, (LivingEntity) entity, serverPlayer);
				}
			);
		}
		ctx.get().setPacketHandled(true);
	}
}



