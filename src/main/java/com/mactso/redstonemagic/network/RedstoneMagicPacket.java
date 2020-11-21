package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.spells.CastSpells;

//import com.lupicus.bk.Mobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class RedstoneMagicPacket 
{
	private int cmd;
	private int id;
	private int timeLeft;
	private int handIndex;
	private int targetPosX;
	private int targetPosY;
	private int targetPosZ;
	
	
	public RedstoneMagicPacket(int cmd, int id, int timeLeft,int handIndex, int targetPosX, int targetPosY, int targetPosZ)
	{
		this.cmd = cmd;
		this.id = id;
		this.timeLeft = timeLeft;
		this.handIndex = handIndex;
		this.targetPosX = targetPosX;
		this.targetPosY = targetPosY;
		this.targetPosZ = targetPosZ;
		}
	
	public void encode(PacketBuffer buf)
	{
		buf.writeByte(cmd);
		buf.writeVarInt(id);
		buf.writeVarInt(timeLeft);
		buf.writeVarInt(handIndex);
		buf.writeVarInt(targetPosX);
		buf.writeVarInt(targetPosY);
		buf.writeVarInt(targetPosZ);

	}

	public static RedstoneMagicPacket readPacketData(PacketBuffer buf)
	{
		int cmd = buf.readByte();
		int id = buf.readVarInt();
		int timeLeft = buf.readVarInt();
		int slotIndex = buf.readVarInt();
		int targetPosX = buf.readVarInt();
		int targetPosY = buf.readVarInt();
		int targetPosZ = buf.readVarInt();
		return new RedstoneMagicPacket(cmd, id, timeLeft, slotIndex, targetPosX, targetPosY, targetPosZ);
	}

	public static void writePacketData(RedstoneMagicPacket msg, PacketBuffer buf)
	{
		msg.encode(buf);
	}

	public static void processRedstoneMagicPacket(RedstoneMagicPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ServerPlayerEntity serverPlayer = ctx.get().getSender();
		Entity targetEntity = serverPlayer.world.getEntityByID(message.id);

		ctx.get().enqueueWork( () -> 
			{
				CastSpells.processSpellOnServer(message.cmd, (LivingEntity) targetEntity, 
						serverPlayer, 
						message.timeLeft, 
						message.handIndex,
						message.targetPosX,
						message.targetPosY,
						message.targetPosZ);
			}
		);
		ctx.get().setPacketHandled(true);
	}
}



