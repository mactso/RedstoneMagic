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
	    Network.registerMessage(SyncClientFlyingPacket.class,
	    		SyncClientFlyingPacket::writePacketData,
	    		SyncClientFlyingPacket::readPacketData,
	    		SyncClientFlyingPacket::processSyncClientFlyingPacket);
	    Network.registerMessage(SyncClientGuiPacket.class,
	    		SyncClientGuiPacket::writePacketData,
	    		SyncClientGuiPacket::readPacketData,
	    		SyncClientGuiPacket::processSyncClientGuiPacket);
	    Network.registerMessage(RedstoneMagicArmorPacket.class,
	    		RedstoneMagicArmorPacket::writePacketData,
	    		RedstoneMagicArmorPacket::readPacketData,
	    		RedstoneMagicArmorPacket::processPacket);
	}
}
