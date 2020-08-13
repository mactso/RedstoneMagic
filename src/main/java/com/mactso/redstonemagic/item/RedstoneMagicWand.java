package com.mactso.redstonemagic.item;

import java.util.List;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.spelltargets.Mobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.Color;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraft.util.math.RayTraceResult.Type;

public class RedstoneMagicWand extends ShieldItem {
	
	public static int NBTNumberField = 99;
	BowItem b;
	ShieldItem s;
	
	public RedstoneMagicWand(Properties builder) {
		super(builder);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		// TODO Auto-generated method stub
         CompoundNBT compoundnbt = stack.getOrCreateTag();
	     int spellNumberKey =  compoundnbt != null && compoundnbt.contains("spellNumber", NBTNumberField) ? compoundnbt.getInt("spellNumber") : 0;
	     SpellManager.RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumberKey));
		tooltip.add(new StringTextComponent("Spell Number : " + spell.getSpellComment()));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

		
		
	    int duration = stack.getUseDuration();
	    int costFactor = (((stack.getUseDuration() - timeLeft) + 10) / 20 ); 
	    float soundVolume = 0.4f + 0.1f * costFactor;
    	if (soundVolume > 0.9f) {
    		soundVolume = 0.9f;
    	}
    	
	    if (!(entityLiving instanceof ServerPlayerEntity)) {
		    PlayerEntity playerEntity = (PlayerEntity) entityLiving;



		    CompoundNBT compoundnbt = stack.getOrCreateTag();
		    int spellState = compoundnbt != null && compoundnbt.contains("spellState", 99) ? compoundnbt.getInt("spellState") : 0;
		    if (spellState == 1) { // just prepared a spell.  TODO move chat message here.
		    	 compoundnbt.putInt("spellState", 0); // not casting a spell.
		    	 MyConfig.dbgPrintln("Just prepared a spell.  do nothing.");
		    	 return;
		     }
		    if (spellState == 0) {
		    	System.out.print("Not casting a spell.  do nothing.");
		    	return;  // not casting a spell.  not preparing a spell.
		    }
		    if (costFactor == 0) {
				MyConfig.sendChat(playerEntity, "Your spell fizzled.  Cast slower.", Color.func_240744_a_(TextFormatting.RED));
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		    	
		    }
		    
		    int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
        	RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
        	MyConfig.sendChat (playerEntity,"You cast"+ spell.getSpellComment() + " costFactor" + costFactor  + " ("+ duration +"/" + timeLeft +").",Color.func_240744_a_(TextFormatting.DARK_RED));	
	    	String spellTargetType = spell.getSpellTargetType();
	    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.6f, 0.5f);
        	Entity targetEntity = Mobs.longTarget(playerEntity);
        	
        	// spell "T"arget, "B"oth self or target, "S"elf only.
        	if (spellTargetType.equals(("T"))) {
        		if (targetEntity == null) {
        			MyConfig.sendChat(playerEntity, "Your spell missed.", Color.func_240744_a_(TextFormatting.RED));
			    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.4f, 0.3f);
        		} else {
                	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), costFactor ));
            	}
        	} 
        	if (spellTargetType.equals("B")) {
        		if (targetEntity == null) {
        			targetEntity = playerEntity;
        		}
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.4f, 0.65f);
            	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), costFactor ));
        		
        	}
        	int debut = 3;
        	if (spellTargetType.equals("S")) {
        		if (targetEntity == null) {
        			targetEntity = playerEntity;
        		}
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.5f, 0.8f);
            	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), costFactor ));
        	}
    	}
	    
	    if (entityLiving instanceof ServerPlayerEntity) {
		    ServerPlayerEntity playerIn = (ServerPlayerEntity) entityLiving;
		    ServerWorld serverWorld = playerIn.getServerWorld();
	    	serverWorld.playMovingSound(null, playerIn, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, soundVolume, 0.65f);
	    }
		     // get the spell value NBT .
			 // cast the appropriate spell.
	
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		
		
		if ( playerIn instanceof ServerPlayerEntity ) {
			ItemStack itemstack = playerIn.getHeldItem(handIn);
			CompoundNBT compoundnbt = itemstack.getOrCreateTag();
		    int spellNumber = compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
		    int spellState = compoundnbt != null && compoundnbt.contains("spellState", 99) ? compoundnbt.getInt("spellState") : 0;
		    
		    if (playerIn.isSneaking()) { // change to a new spell.
			    spellNumber = (spellNumber+1)%8;			
			    compoundnbt.putInt("spellNumber", spellNumber);
			    spellState = 1;
			    compoundnbt.putInt("spellState", spellState); 
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
				MyConfig.sendChat (playerIn,"You switch to "+ spell.getSpellComment()  + ".",Color.func_240744_a_(TextFormatting.DARK_RED));	
			} else {  // start casting current spell
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
			    spellState = 2;
				compoundnbt.putInt("spellState", spellState); // currently casting a spell.
		    	MyConfig.sendChat (playerIn,"You begin casting "+ spell.getSpellComment()  + ".",Color.func_240744_a_(TextFormatting.DARK_RED));	
			}	
			
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}	
	
    public int getUseDuration(ItemStack stack) {
		return 72000;
    }
	
}
