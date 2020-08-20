package com.mactso.redstonemagic.item;

import java.util.List;
import java.util.function.Consumer;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.spells.CastSpells;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
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
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
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
import net.minecraft.particles.BasicParticleType;
public class RedstoneFocusItem extends ShieldItem {
	
	public static int NBTNumberField = 99;
	private final int damageReduceAmount;

//	private static final IParticleData HARM_PARTICLE_DATA = new BlockParticleData(ParticleTypes.BLOCK, Blocks.BRICKS.getDefaultState()),
//	HEAL_PARTICLE_DATA = new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_CONCRETE.getDefaultState());

	@Override
	public boolean isShield(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (amount > 2) {
			amount = 2;
		}
		return super.damageItem(stack, amount, entity, onBroken);
	}
	
	@Override
	public boolean isDamageable() {
		return true;
	}
	
	public RedstoneFocusItem(Properties builder) {
		super(builder);
		damageReduceAmount = 1;
		// TODO Auto-generated constructor stub
	}

//	private void getDamageReduceAmount() {
//
//	}
	
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
//	    int costFactor = (((stack.getUseDuration() - timeLeft) + 10) / 20 ); 
	    long netSpellCastingTime = 0;
	    if (MyConfig.getCastTime() > 0) {
			netSpellCastingTime = (worldIn.getGameTime() - MyConfig.getCastTime())/4;
			if (netSpellCastingTime > 4) netSpellCastingTime = 4;
		}
	    float soundVolume = 0.4f + 0.1f * netSpellCastingTime;
    	if (soundVolume > 0.9f) {
    		soundVolume = 0.9f;
    	}

    	if (entityLiving instanceof ServerPlayerEntity) {
    		MyConfig.dbgPrintln("onPlayerStoppedUsing: servercall");
    		return;
    	} else {
		    PlayerEntity playerEntity = (PlayerEntity) entityLiving;

		    CompoundNBT compoundnbt = stack.getOrCreateTag();
		    int spellState = compoundnbt != null && compoundnbt.contains("spellState", 99) ? compoundnbt.getInt("spellState") : 0;
		    if (spellState == 1) { // just prepared a spell.  TODO move chat message here.
		    	 compoundnbt.putInt("spellState", 0); // not casting a spell.
		    	 MyConfig.dbgPrintln("Just prepared a spell.  do nothing.");
		    	 return;
		     }
		    if (spellState == 0) {
		    	MyConfig.dbgPrintln("Not casting a spell.  do nothing.");
		    	return;  // not casting a spell.  not preparing a spell.
		    }
		    if (netSpellCastingTime == 0) {
				MyConfig.sendChat(playerEntity, "Your spell fizzled.  Cast slower.", Color.func_240744_a_(TextFormatting.RED));
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		    	return;
		    }
		    
		    int spellNumber =  compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
        	RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
        	MyConfig.sendChat (playerEntity,"You cast "+ spell.getSpellComment() + " Casting Time:" + netSpellCastingTime +"."  ,Color.func_240744_a_(TextFormatting.DARK_RED));	
	    	String spellTargetType = spell.getSpellTargetType();
	    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.6f, 0.5f);
        	Entity targetEntity = CastSpells.longTarget(playerEntity);
        	
        	// spell "T"arget, "B"oth self or target, "S"elf only.
        	if (spellTargetType.equals(("T"))) {
        		if (targetEntity == null) {
        			MyConfig.sendChat(playerEntity, "Your spell missed.", Color.func_240744_a_(TextFormatting.RED));
			    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.4f, 0.3f);
        		} else {
                	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), (int) netSpellCastingTime));
        		}
        	} 
        	if (spellTargetType.equals("B")) {
        		if (targetEntity == null) {
        			targetEntity = playerEntity;
        		}
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.AMBIENT, 0.4f, 0.65f);
            	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), (int) netSpellCastingTime ));
        		
        	}
        	if (spellTargetType.equals("S")) {
        		if (targetEntity == null) {
        			targetEntity = playerEntity;
        		}
		    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.5f, 0.8f);
            	Network.sendToServer(new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), (int) netSpellCastingTime ));
        	}
 
        	MyConfig.setCastTime((long)0);
	    } 
   		


   
//	    if (entityLiving instanceof ServerPlayerEntity) {
//		    ServerPlayerEntity playerIn = (ServerPlayerEntity) entityLiving;
//		    ServerWorld serverWorld = playerIn.getServerWorld();
//	    	serverWorld.playMovingSound(null, playerIn, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.AMBIENT, soundVolume, 0.65f);
//	    }
	
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		
		if ( playerIn instanceof ServerPlayerEntity ) {
			MyConfig.dbgPrintln("onItemRightClick: servercall");
			ItemStack itemstack = playerIn.getHeldItem(handIn);
			CompoundNBT compoundnbt = itemstack.getOrCreateTag();
		    int spellNumber = compoundnbt != null && compoundnbt.contains("spellNumber", 99) ? compoundnbt.getInt("spellNumber") : 0;
		    int spellState = compoundnbt != null && compoundnbt.contains("spellState", 99) ? compoundnbt.getInt("spellState") : 0;
		    
		    if (playerIn.isSneaking()) { // change to a new spell.
				float headPitch = playerIn.rotationPitch;
		    	if (headPitch <= -0.1) { // looking up - go backwards thru list.
			    	spellNumber = (spellNumber+7)%8;			
		    	} else {  				 // looking straight - go forwards thru list
		    		spellNumber = (spellNumber+1)%8;
		    	}
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
		    	MyConfig.setCastTime(playerIn.world.getGameTime());
			}	
			
		} else {
			MyConfig.dbgPrintln("player clientside call" + MyConfig.getCastTime());
		}

		
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}	

	public int getItemEnchantability() {
	       return 1;
	}	
	
    public int getUseDuration(ItemStack stack) {
		return 72000;
    }
	
    public void spawnDamageParticles(Entity entity, int particleCount, IParticleData particleType) {
    	  if (!(entity.world.isRemote())) {
              for(int i = 0; i < particleCount; ++i) {
            	  double posX = entity.getPosX();
            	  double posY = entity.getPosY();
            	  double posZ = entity.getPosZ();
                  double motionX = entity.world.rand.nextGaussian() * 0.02D;
                  double motionY = entity.world.rand.nextGaussian() * 0.02D;
                  double motionZ = entity.world.rand.nextGaussian() * 0.02D;
                  double PosXWidth = entity.getPosXWidth(1.0D);
                  double PosYWidth = PosXWidth;
                  double PosZWidth = entity.getPosZWidth(1.0D);
                      entity.world.addParticle(
                            particleType, 
                            posX + 0.5D + motionX, 
                            posY + 0.5D + motionY, 
                            posZ + 0.5D + motionZ, 
                            motionX, motionY, motionZ);
              }
         }
    		  
    }
}
