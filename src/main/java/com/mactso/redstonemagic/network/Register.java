package com.mactso.redstonemagic.network;

public class Register
{
	public static void initPackets()
	{
	    Network.registerMessage(RedstoneMagicPacket.class,
	    		RedstoneMagicPacket::writePacketData,
	    		RedstoneMagicPacket::readPacketData,
	    		RedstoneMagicPacket::processRedstoneMagicPacket);
	    Network.registerMessage(SyncClientManaPacket.class,
	    		SyncClientManaPacket::writePacketData,
	    		SyncClientManaPacket::readPacketData,
	    		SyncClientManaPacket::processSyncClientManaPacket);
	    Network.registerMessage(SyncClientGuiPacket.class,
	    		SyncClientGuiPacket::writePacketData,
	    		SyncClientGuiPacket::readPacketData,
	    		SyncClientGuiPacket::processSyncClientGuiPacket);
	}
}
