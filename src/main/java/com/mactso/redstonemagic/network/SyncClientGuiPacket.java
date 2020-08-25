package com.mactso.redstonemagic.network;

import java.util.function.Supplier;
import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

// carries # of new prepared spell (-1 if no spell prepared)
// carries # of spell being cast (-1 if no spell cast/ spell cancelled)

public class SyncClientGuiPacket {
	
	private int preparedSpellNumber;
	private int castingSpellNumber;
	
	public SyncClientGuiPacket (int preparedSpellNumber, int castingSpellNumber)
	{
		this.preparedSpellNumber = preparedSpellNumber;
		this.castingSpellNumber = castingSpellNumber;

	}
	
	public static void processSyncClientGuiPacket(SyncClientGuiPacket message, Supplier<NetworkEvent.Context> ctx)
	{

		ctx.get().enqueueWork( () -> 
			{
				if (message.preparedSpellNumber >= 0) RedstoneMagicGuiEvent.setPreparedSpellNumber(message.preparedSpellNumber);
				if (message.castingSpellNumber >= 0) RedstoneMagicGuiEvent.setCastPreparedSpellNumber(message.castingSpellNumber);
			}
		);
		ctx.get().setPacketHandled(true);
	}
	
	public static SyncClientGuiPacket readPacketData(PacketBuffer buf)
	{
		int preparedSpelLNumber = buf.readVarInt();
		int castingSpellNumber = buf.readVarInt();
		return new SyncClientGuiPacket(preparedSpelLNumber, castingSpellNumber);
	}
	
	public static void writePacketData(SyncClientGuiPacket msg, PacketBuffer buf)
	{
		msg.encode(buf);
	}
	
	public void encode(PacketBuffer buf)
	{
		buf.writeVarInt(preparedSpellNumber);
		buf.writeVarInt(castingSpellNumber);
	}
}
