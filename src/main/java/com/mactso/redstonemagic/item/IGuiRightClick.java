package com.mactso.redstonemagic.item;

import org.lwjgl.glfw.GLFW;

import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicArmorPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen.CreativeContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public interface IGuiRightClick  {
	public void menuRightClick(ItemStack stack);

    @Mod.EventBusSubscriber()
	public static class ClientEvents
	{
        @OnlyIn(Dist.CLIENT)
	    @SubscribeEvent
	    public static void onMouseScreenEvent(GuiScreenEvent.MouseClickedEvent.Pre event)
	    {
	    	if (event.isCanceled())
	    		return;
	    	if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT)
	    		return;
	    	Screen gui = event.getGui();
	    	if (gui == null || !(gui instanceof ContainerScreen<?>))
	    		return;
			ContainerScreen<?> cg = (ContainerScreen<?>) gui;
			Slot slot = cg.getSlotUnderMouse();
			if (slot != null && slot.hasItem())
			{
				ItemStack stack = slot.getItem();
				if (stack.getItem() instanceof IGuiRightClick)
				{
					Container cont = cg.getMenu();
					int index = -1;
					if (cont.containerId == 0 && cont instanceof CreativeContainer)
					{
						// need to remap to what the server side is using
						Minecraft mc = gui.getMinecraft();
						for (Slot slot2 : mc.player.inventoryMenu.slots)
						{
							if (slot2.isSameInventory(slot) && slot2.getSlotIndex() == slot.getSlotIndex())
							{
								index = slot2.index;
								break;
							}
						}
					}
					else
						index = slot.index;
					if (cont instanceof RecipeBookContainer<?>)
					{
						// skip if in the crafting section
						if (index < ((RecipeBookContainer<?>)cont).getSize())
							return;
					}
					if (index >= 0)
					{
						Network.sendToServer(new RedstoneMagicArmorPacket(1, cont.containerId, index));
						if (event.isCancelable())
							event.setCanceled(true);
					}
				}
			}
	    	return;
	    }
    }

}
