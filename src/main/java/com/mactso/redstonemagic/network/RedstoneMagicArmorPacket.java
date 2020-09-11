package com.mactso.redstonemagic.network;

import java.util.function.Supplier;

import com.mactso.redstonemagic.item.IGuiRightClick;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
	
	public void encode(PacketBuffer buf)
	{
		buf.writeByte(cmd);
		buf.writeByte(windowId);
		buf.writeShort(index);
	}

	public static RedstoneMagicArmorPacket readPacketData(PacketBuffer buf)
	{
		int cmd = buf.readByte();
		int windowId = buf.readByte();
		int index = buf.readShort();
		return new RedstoneMagicArmorPacket(cmd, windowId, index);
	}

	public static void writePacketData(RedstoneMagicArmorPacket msg, PacketBuffer buf)
	{
		msg.encode(buf);
	}

	public static void processPacket(RedstoneMagicArmorPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ServerPlayerEntity player = ctx.get().getSender();
		if (message.cmd == 1)
		{
			ctx.get().enqueueWork(() -> {
				Container cont = player.openContainer;
				if (message.windowId == cont.windowId && message.index >= 0)
				{
					Slot slot = cont.getSlot(message.index);
					if (slot.getHasStack())
					{
						ItemStack stack = slot.getStack();
						if (stack.getItem() instanceof IGuiRightClick)
						{
							((IGuiRightClick)stack.getItem()).menuRightClick(stack);
							slot.onSlotChanged();
						}
					}
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}
}

