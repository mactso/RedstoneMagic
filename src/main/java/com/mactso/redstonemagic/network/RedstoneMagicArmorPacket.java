package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.item.IGuiRightClick;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

public class RedstoneMagicArmorPacket
{
	private int cmd;
	private int windowId;
	private int index;
	
	public RedstoneMagicArmorPacket(int cmd, int windowId, int index)
	{
		this.cmd = cmd;
		this.windowId = windowId;
		this.index = index;
	}
	
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeByte(cmd);
		buf.writeByte(windowId);
		buf.writeShort(index);
	}

	public static RedstoneMagicArmorPacket readPacketData(FriendlyByteBuf buf)
	{
		int cmd = buf.readByte();
		int windowId = buf.readByte();
		int index = buf.readShort();
		return new RedstoneMagicArmorPacket(cmd, windowId, index);
	}

	public static void writePacketData(RedstoneMagicArmorPacket msg, FriendlyByteBuf buf)
	{
		msg.encode(buf);
	}

	public static void processPacket(RedstoneMagicArmorPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ServerPlayer player = ctx.get().getSender();
		if (message.cmd == 1)
		{
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu cont = player.containerMenu;
				if (message.windowId == cont.containerId && message.index >= 0)
				{
					Slot slot = cont.getSlot(message.index);
					if (slot.hasItem())
					{
						ItemStack stack = slot.getItem();
						if (stack.getItem() instanceof IGuiRightClick)
						{
							((IGuiRightClick)stack.getItem()).menuRightClick(stack);
							slot.setChanged();
						}
					}
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}
}

