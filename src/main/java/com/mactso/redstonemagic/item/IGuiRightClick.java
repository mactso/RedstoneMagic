package com.mactso.redstonemagic.item;

import org.lwjgl.glfw.GLFW;

import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicArmorPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent.MouseButtonPressed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public interface IGuiRightClick  {
	public void menuRightClick(ItemStack stack);

    @Mod.EventBusSubscriber()
	public static class ClientEvents
	{
        @OnlyIn(Dist.CLIENT)
	    @SubscribeEvent
	    public static void onMouseScreenEvent(MouseButtonPressed.Pre event)
	    {
	    	if (event.isCanceled())
	    		return;
	    	if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT)
	    		return;
	    	Screen gui = event.getScreen();
	    	if (gui == null || !(gui instanceof AbstractContainerScreen<?>))
	    		return;
			AbstractContainerScreen<?> cg = (AbstractContainerScreen<?>) gui;
			Slot slot = cg.getSlotUnderMouse();
			if (slot != null && slot.hasItem())
			{
				ItemStack stack = slot.getItem();
				if (stack.getItem() instanceof IGuiRightClick)
				{
					AbstractContainerMenu cont = cg.getMenu();
					int index = -1;
					if (cont.containerId == 0 && cont instanceof ItemPickerMenu)
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
					if (cont instanceof RecipeBookMenu<?>)
					{
						// skip if in the crafting section
						if (index < ((RecipeBookMenu<?>)cont).getSize())
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
