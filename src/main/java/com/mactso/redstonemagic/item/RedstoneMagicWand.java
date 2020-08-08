package com.mactso.redstonemagic.item;

import java.util.List;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.spelltargets.Mobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.util.math.RayTraceResult.Type;

public class RedstoneMagicWand extends ShieldItem {

	public RedstoneMagicWand(Properties builder) {
		super(builder);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		// TODO Auto-generated method stub
         CompoundNBT compoundnbt = stack.getOrCreateTag();
	     int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;

		tooltip.add(new StringTextComponent("Spell Number :" + spellNumber));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

	     CompoundNBT compoundnbt = stack.getOrCreateTag();
	     int duration = stack.getUseDuration();
	     int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
	     int debug = 1;
		 // get the spell value NBT .
		 // cast the appropriate spell.
	     PlayerEntity playerIn = (PlayerEntity) entityLiving;
	     
	     if (playerIn != null) {
		     if (playerIn.world.isRemote()) {
		    	 Entity target = Mobs.target(playerIn);
		    	 MyConfig.sendChat (playerIn,"You released spell # " + spellNumber + " ("+ duration+"/" + timeLeft +").",TextFormatting.DARK_RED);	
		    	 if (target != null) {

		    		 TranslationTextComponent t = (TranslationTextComponent) target.getDisplayName();

			    	 MyConfig.sendChat (playerIn," at the "+ t.getString() + ".",TextFormatting.DARK_RED);
					 Network.sendToServer(new RedstoneMagicPacket(spellNumber, target.getEntityId()));			    	 
		    	 } else {
			    	 MyConfig.sendChat (playerIn,"and missed.... Whoosh.",TextFormatting.DARK_RED);			    		 
		    	 }
			 }
	     }

	
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		// TODO Auto-generated method stub
		boolean shiftKey = playerIn.isSneaking();
		// check for both sides (server and client);
		if (playerIn.world.isRemote()) {
			
		}
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		// if item isn't a wand skip this.

		if (playerIn.isSneaking()) {
		     // get the spell value NBT (++)%8.
		     CompoundNBT compoundnbt = itemstack.getOrCreateTag();
		     int spellNumber = compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
		     spellNumber = (spellNumber+1)%8;
			 compoundnbt.putInt("spellNumber", spellNumber);

			 MyConfig.sendChat (playerIn,"You prepare spell # "+ spellNumber +".",TextFormatting.DARK_RED);
		} else {
		     CompoundNBT compoundnbt = itemstack.getOrCreateTag();
		     int duration = itemstack.getUseDuration();
		     int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
		     int debug = 1;
		     
			 // get the spell value NBT .
			 // cast the appropriate spell.
		     MyConfig.sendChat (playerIn,"Your holding spell # "+ spellNumber +" ready to cast.",TextFormatting.DARK_RED);

		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
		
	}
	
	   public int getUseDuration(ItemStack stack) {
		     CompoundNBT compoundnbt = stack.getOrCreateTag();
		     int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
		     return spellNumber * 10;
	   }
	
}
