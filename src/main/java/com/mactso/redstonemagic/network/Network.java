package com.mactso.redstonemagic.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mactso.redstonemagic.Main;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Network
{
	private static final String PROTOCOL_VERSION = "1.0";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
	    new ResourceLocation(Main.MODID, "main"),
	    () -> PROTOCOL_VERSION,
	    PROTOCOL_VERSION::equals,
	    PROTOCOL_VERSION::equals
	);
	private static int id = 0;

	public static <MSG> void registerMessage(Class<MSG> msg,
			BiConsumer<MSG, PacketBuffer> encoder,
			Function<PacketBuffer, MSG> decoder,
			BiConsumer<MSG, Supplier<Context>> handler)
	{
		INSTANCE.registerMessage(id++, msg, encoder, decoder, handler);
	}

	@OnlyIn(Dist.CLIENT)
	public static <MSG> void sendToServer(MSG msg)
	{
		INSTANCE.sendToServer(msg);
	}

	public static <MSG> void sendToClient(MSG msg, ServerPlayerEntity player)
	{
		INSTANCE.sendTo(msg, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static <MSG> void sendToTarget(PacketDistributor.PacketTarget target, MSG msg)
	{
		INSTANCE.send(target, msg);
	}

}
