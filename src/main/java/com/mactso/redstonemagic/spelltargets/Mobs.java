package com.mactso.redstonemagic.spelltargets;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

public class Mobs
{
//	private static final Logger LOGGER = LogManager.getLogger();
////	public static boolean move_state = true;
////	public static boolean look_state = true;
//	public static HashSet<LivingEntity> mobs = new HashSet<>();
//	public static final AttributeModifier FREEZE_ATTR = new AttributeModifier(UUID.fromString("8d2c8d25-7a8c-4fbc-be91-8dba276ebbe0"), "freeze", -1.0, Operation.MULTIPLY_TOTAL);

	//server dist
	public static void processCastSpells(int spellNumber, LivingEntity entity, ServerPlayerEntity serverPlayer)
	{
		String spellKey = Integer.toString(spellNumber);
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(spellKey);
		if (castSpell (spell, entity, serverPlayer)){
			System.out.println("Serverside Spell :" + spell.getSpellComment() + " at " + entity.getName().toString() + ".");
		} else {
			System.out.println("Serverside Spell :" + spell.getSpellComment() + ".  Not enough Mana.");
		}
		
	}

	public static boolean castSpell (RedstoneMagicSpellItem spell, LivingEntity entity, ServerPlayerEntity serverPlayer) {
    	final int FOUR_SECONDS = 80;	
    	final int TWENTY_SECONDS = 400;
    	final int QUICK_INTENSITY = 1;
    	final int FULL_INTENSITY = 4;
    	
    	// @TODO this section will be replaced with intensity based on #ticks holding spell ready (max 80).
    	int spellCost = QUICK_INTENSITY;
    	int spellIntensity = QUICK_INTENSITY;
		
		String costCode = spell.getSpellCostCode();
		String spellKey = spell.getSpellTranslationKey();
		if (spellKey.equals("RM.DOT")) {
			int debugSame = 1;
		}
		if (costCode.equals("C1")) {
			spellCost = 1;  // up to 4 based on quick click vs hold time
		}
		if (costCode.equals( "C2")) {
			spellCost = 2;  // up to 8 based on quick click vs hold time
			
		}
		if (costCode.equals( "C3")) {
			spellCost = 3; // up to 12 based on quick click vs hold time
			
		}
		if (costCode.equals( "C4")) {
			spellCost = 4; // up to 16 based on quick click vs hold time
		}
		if (costCode.equals( "CDIS")) {
			
			int pX = (int) serverPlayer.getPosX();
			int pY = (int) serverPlayer.getPosY();
			int pZ = (int) serverPlayer.getPosZ();
			BlockPos playerPos = new BlockPos (pX,pY,pZ);
			if (spellKey.equals("RM.TWSP")) {
				int wX = serverPlayer.world.getWorldInfo().getSpawnX();
				int wY = serverPlayer.world.getWorldInfo().getSpawnY();
				int wZ = serverPlayer.world.getWorldInfo().getSpawnZ();
				BlockPos worldspawnPos = new BlockPos (wX,wY,wZ);
				spellCost = (worldspawnPos.manhattanDistance(playerPos) / 1000) + 1;
			} else if  (spellKey.equals("RM.THOM")) {
				Optional<BlockPos> optBedPos = serverPlayer.getBedPosition();
    			if (optBedPos.isPresent()) {
    				BlockPos bedPos = optBedPos.get();
    				spellCost = (bedPos.manhattanDistance(playerPos) / 1000) + 1;
    			}
			}
		}

		LazyOptional<IMagicStorage> optMagicStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC);
		if ((optMagicStorage.isPresent()) && (spellCost > 0))
		{
			IMagicStorage cap = optMagicStorage.orElseGet(null);
			if (cap.useMana(spellCost)) {
				// spell was cast. can be paid for now... so cast spell

				if (spellKey.equals("RM.DMG") ) {
					int damage = (int) entity.getHealth()/10;
					if (damage < 2) {
						damage = 2;
					}
					if (entity.attackEntityFrom(DamageSource.MAGIC, damage)) {
						return true;
					}
					return false;
				} else
				if (spellKey.equals("RM.HEAL") ) {
					int damage = (int) entity.getHealth()/10;
					if (damage < 2) {
						damage = 2;
					}
					if (entity.isEntityUndead()) {
						entity.attackEntityFrom(DamageSource.MAGIC, damage);
						return true;
					} else // not undead and alive (so not a chest, broken block, etc.)
					if (entity.isAlive()){
						entity.heal((float)damage);
						return true;
					}
					return false;
				} else
				if (spellKey.equals(("RM.DOT")))  {
					int debugxxzzy = 5;
					int effectIntensity = (int) entity.getHealth()/20;
					if (effectIntensity < 1) {
						effectIntensity = 1;
					}
					if (entity.isAlive()) {  // a creature (even zombies) - not a broken block.
			 			EffectInstance ei = entity.getActivePotionEffect(Effects.POISON);
			    		if (ei != null) {
			    			if (ei.getDuration() > 10) {
			    				return false;
			    			}
			    			if (ei.getAmplifier() <= effectIntensity) {
			    				entity.removeActivePotionEffect(Effects.POISON );
			    			}
			    		}
						entity.addPotionEffect(new EffectInstance(Effects.POISON, FOUR_SECONDS, effectIntensity, true, true  ));
						return true;
					}
				} else
				if (spellKey.equals("RM.TWSP") ) {
					int wX = serverPlayer.world.getWorldInfo().getSpawnX();
					int wY = serverPlayer.world.getWorldInfo().getSpawnY();
					int wZ = serverPlayer.world.getWorldInfo().getSpawnZ();
					if (entity.attemptTeleport(wX, wY, wZ, true)) {
						// successful teleport to worldspawn. Pay the cost
						return true;
					} else {
						// failure didn't teleport.  Don't pay the cost.
					}
					return false;
				} else
				if (spellKey.equals("RM.THOM") ) {
					Optional<BlockPos> optBedPos = serverPlayer.getBedPosition();
	    			if (optBedPos.isPresent()) {
	    				BlockPos bedPos = optBedPos.get();
						if (entity.attemptTeleport(bedPos.getX(), bedPos.getY(), bedPos.getZ(), true)) {
							// successful teleport to worldspawn. Pay the cost
							return true;
						} else {
							// failure didn't teleport.  Don't pay the cost.
						}
	    			}
	    			return false;
				}
				if (spellKey.equals( "RM.RESI") ) {

		 			EffectInstance ei = entity.getActivePotionEffect(Effects.RESISTANCE);
		    		if (ei != null) {
		    			if (ei.getDuration() > 10) {
		    				return false;
		    			}
		    			if (ei.getAmplifier() <= spellIntensity) {
		    				entity.removeActivePotionEffect(Effects.POISON );
		    			}
		    		}
					entity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, TWENTY_SECONDS, spellIntensity, true, true  ));
					return true;
				} else
				if (spellKey.equals("RM.RCRS") ) {
					// @TODO if milk in inventory then
					if (MyConfig.debugLevel > 0) {
						System.out.println ("Remove Curse: remove one negative effect");
					}
					return true;
				} else
				if (spellKey.equals("RM.SDOT") ) {
					if (entity.isAlive()) {  // a creature (even zombies) - not a broken block.
			 			EffectInstance ei = entity.getActivePotionEffect(Effects.POISON);
			    		if (ei != null) {
			    			if (ei.getDuration() > 10) {
			    				return false;
			    			}
			    			if (ei.getAmplifier() <= spellIntensity) {
			    				entity.removeActivePotionEffect(Effects.POISON );
			    			}
							entity.addPotionEffect(new EffectInstance(Effects.POISON, TWENTY_SECONDS, spellIntensity, true, true  ));
			    		}
				 		ei = entity.getActivePotionEffect(Effects.SLOWNESS);
				    	if (ei != null) {
				    			if (ei.getDuration() > 10) {
				    				return false;
				    			}
				    			if (ei.getAmplifier() <= spellIntensity) {
				    				entity.removeActivePotionEffect(Effects.SLOWNESS);
				    			}
								entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, TWENTY_SECONDS - 4, spellIntensity, true, true  ));
				    		}
							return true;
						}
				}
			}			
		}
		return false;
	}
	
	public static LivingEntity target(PlayerEntity player)
	{

		Minecraft mc = Minecraft.getInstance();
		if (mc.objectMouseOver.getType() == Type.ENTITY)
		{
			Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
			if (entity instanceof LivingEntity)
				return (LivingEntity) entity;
		}
		return null;
	}
	
//	public static void patch_mob(LivingEntity entity)
//	{
//
//	}



//	public static void toggleClosest(PlayerEntity player)
//	{
//		LivingEntity closest = closest(player);
//		if (closest != null)
//		{
//			Network.sendToServer(new RedstoneMagicPacket(1, closest.getEntityId()));
//		}
//	}

//	public static void toggleTarget(PlayerEntity player)
//	{
//		LivingEntity target = target(player);
//		if (target != null)
//		{
//			Network.sendToServer(new RedstoneMagicPacket(1, target.getEntityId()));
//
//		}
//	}


//	public static LivingEntity closest(PlayerEntity player)
//	{
//		World world = player.world;
//		AxisAlignedBB bbox = new AxisAlignedBB(-8.0, -8.0, -8.0, 8.0, 8.0, 8.0);
//		List<LivingEntity> list = world.getEntitiesWithinAABB(LivingEntity.class, bbox);
//		double d0 = Double.MAX_VALUE;
//		LivingEntity closest = null;
//		for (LivingEntity entity : list)
//		{
//			double d1 = player.getDistanceSq(entity);
//			if (d1 < d0)
//			{
//				d0 = d1;
//				closest = entity;
//			}
//		}
//		return closest;
//	}

}
