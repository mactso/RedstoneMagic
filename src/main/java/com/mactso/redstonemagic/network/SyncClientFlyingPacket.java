package com.mactso.redstonemagic.network;
import java.util.function.Supplier;

import com.mactso.redstonemagic.Main;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncClientFlyingPacket {



		private boolean isFlying;
		private boolean isChunkFlying;
		private long chunkAge;
		
		public SyncClientFlyingPacket (boolean isFlying, boolean isChunkFlying, long chunkAge)
		{
			this.isFlying = isFlying;
			this.isChunkFlying= isChunkFlying;
			this.chunkAge= chunkAge;

		}

		public static void processSyncClientFlyingPacket(SyncClientFlyingPacket message, Supplier<NetworkEvent.Context> ctx)
		{

			ctx.get().enqueueWork( () -> 
				{	
					Main.proxy.setFlyingValues(message.isFlying, message.isChunkFlying, message.chunkAge);
				}
			);
			ctx.get().setPacketHandled(true);
		}

		public static SyncClientFlyingPacket readPacketData(PacketBuffer buf)
		{
			boolean isFlying = buf.readBoolean();
			boolean isChunkFlying = buf.readBoolean();
			long chunkAge = buf.readLong();

			return new SyncClientFlyingPacket(isFlying, isChunkFlying, chunkAge);
		}

		public static void writePacketData(SyncClientFlyingPacket msg, PacketBuffer buf)
		{
			msg.encode(buf);
		}
		
		public void encode(PacketBuffer buf)
		{

			buf.writeBoolean(isFlying);
			buf.writeBoolean(isChunkFlying);
			buf.writeLong(chunkAge);
		}

}
