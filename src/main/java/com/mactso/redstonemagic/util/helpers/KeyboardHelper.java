package com.mactso.redstonemagic.util.helpers;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KeyboardHelper {
	private static long Window = Minecraft.getInstance().getWindow().getWindow();
	@OnlyIn (Dist.CLIENT)
	public static boolean isHoldingShift() {
		return InputConstants.isKeyDown(Window,GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Window,GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
	
	@OnlyIn (Dist.CLIENT)
	public static boolean isHoldingCtrl() {
		return InputConstants.isKeyDown(Window,GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(Window,GLFW.GLFW_KEY_RIGHT_CONTROL);
	}
}
