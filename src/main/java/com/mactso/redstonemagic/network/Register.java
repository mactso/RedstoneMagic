package com.mactso.redstonemagic.network;

public class Register
{
	public static void initPackets()
	{
	    Network.registerMessage(RedstoneMagicPacket.class,
	    		RedstoneMagicPacket::writePacketData,
	    		RedstoneMagicPacket::readPacketData,
	    		RedstoneMagicPacket::processRedstoneMagicPacket);
	}
}
