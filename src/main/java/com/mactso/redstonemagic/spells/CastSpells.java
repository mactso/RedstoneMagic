package com.mactso.redstonemagic.spells;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.screen.EditGamerulesScreen.GamerulesList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
public class CastSpells {
	static final int THREE_SECONDS = 60;
	static final int FOUR_SECONDS = 80;
	static final int THIRTY_SECONDS = 600;
	static final int QUICK_INTENSITY = 20;
	static final int FULL_INTENSITY = 80;
	static final int BOSS_MOB_LIMIT = 40;
	static final int NO_CHUNK_MANA_UPDATE = -1;
	static double posX = 0;
	static double posY = 0;
	static double posZ = 0;
	static ItemStack MILK_STACK = new ItemStack (Items.MILK_BUCKET);
	static int total_calls = 0; 
	
	private static boolean castSpellAtTarget(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			RedstoneMagicSpellItem spell) {
		
		String spellTranslationKey = spell.getSpellTranslationKey();
		String spellTargetType = spell.getSpellTargetType();
		DamageSource myDamageSource = DamageSource.causePlayerDamage(serverPlayer).setDamageBypassesArmor()
				.setMagicDamage();
		ServerWorld serverWorld = (ServerWorld) targetEntity.world;
	  	posX = targetEntity.getPosX();
	  	posY = targetEntity.getPosY();
	  	posZ = targetEntity.getPosZ();
	  	int baseWeaponDamage = 0;
		ItemStack handItem = serverPlayer.getHeldItemMainhand();
		Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE);
		while((d.iterator().hasNext()) && (baseWeaponDamage == 0)) {
			baseWeaponDamage = (int) d.iterator().next().getAmount();
	    }
		int weaponDamage = (int) ((float)(baseWeaponDamage / 4) * spellTime);
		
//***
		if (spell.getSpellTranslationKey().equals("RM.NUKE")) {
			return doSpellNuke(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage);
		}
//***
		if (spellTranslationKey.equals(("RM.DOT"))) {
			return doSpellDoT(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage);
		}
//***
		if (spellTranslationKey.equals(("RM.SDOT"))) {
			return doSpellSnareDot(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage);
		}
//***
		if (spellTranslationKey.equals("RM.BUFF")) {
			return doSpellMultiBuff(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("RM.HEAL")) {
			return doSpellHeal(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage);
		}
//***
		if (spellTranslationKey.equals("RM.RESI")) {
			return doSpellResistance(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("RM.RCRS")) {
			return doSpellRemoveCurse(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("RM.TELE")) {
			return doSpellTeleport(serverPlayer, targetEntity, spellTime, serverWorld);
		}		
		return false;
	}
	
	private static boolean doSpellDoT(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, int weaponDamage) {

		int effectIntensity = (int) targetEntity.getHealth() / 12;
		if (effectIntensity < 1) effectIntensity = 1;
		if (effectIntensity > BOSS_MOB_LIMIT / 5 ) effectIntensity = BOSS_MOB_LIMIT /5; 
		if (weaponDamage / 4 > effectIntensity) effectIntensity = weaponDamage/4;			
		int secondsDuration = FOUR_SECONDS * spellTime;
		
		boolean damaged = false;
		damaged = targetEntity.attackEntityFrom(myDamageSource, spellTime);
		
		if ((targetEntity instanceof PlayerEntity) && (!(damaged))) {
			return false;  // PVP hack til I find server settings.  Basically if a player and not damaged by nuke, then don't apply DoT to them.
		}		

		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.POISON);
		if (ei != null) {
			if (ei.getDuration() > 6) {
				return false;
			}
			if (ei.getAmplifier() <= effectIntensity) {
				targetEntity.removeActivePotionEffect(Effects.POISON);
			}
		}

		targetEntity.attackEntityFrom(myDamageSource, spellTime);
		targetEntity.addPotionEffect(new EffectInstance(Effects.POISON, secondsDuration, effectIntensity, true, true));
		serverWorld.playSound(null, targetEntity.getPosition(),SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		serverWorld.playSound(null, targetEntity.getPosition(),SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ITEM_SLIME);
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ANGRY_VILLAGER); 
		return true;
		
	}

	private static boolean doSpellHeal(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, int weaponDamage) {

		int damage = (int) targetEntity.getHealth() / 10;
		if (damage < 2)	damage = 2;
		damage = 2 * spellTime;
		if (targetEntity.isEntityUndead()) {

			if (damage > BOSS_MOB_LIMIT)
				damage = BOSS_MOB_LIMIT;
			if (weaponDamage > damage)
				damage = weaponDamage - 1;

			if (targetEntity.attackEntityFrom(myDamageSource, damage)) {
				serverWorld.playSound(null, serverPlayer.getPosition(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.2f, 0.9f);
				serverWorld.playSound(null, targetEntity.getPosition(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.9f, 0.25f);
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.HEART);
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.CAMPFIRE_COSY_SMOKE);
				serverSpawnMagicalParticles(targetEntity, serverWorld, damage, ParticleTypes.DAMAGE_INDICATOR);
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE_DUST);
				return true;
			} else {
				return false;
			}
		}
		if ((isHealable(targetEntity))) {
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ASH);
			targetEntity.heal((float) damage);
			serverWorld.playSound(null, serverPlayer.getPosition(), SoundEvents.BLOCK_BEACON_ACTIVATE,
					SoundCategory.AMBIENT, 0.2f, 0.8f);
			serverWorld.playSound(null, targetEntity.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
					SoundCategory.AMBIENT, 0.9f, 0.86f);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.HEART);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE_DUST);
			return true;
		} else {
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.POOF);
			return false;

		}

	}
	
	
	private static boolean isHealable (LivingEntity entity) {
		if (entity.getHealth() >= entity.getMaxHealth()) {
			return false;
		}
		if (entity instanceof PlayerEntity) {
			return true;
		}
		if (entity instanceof AnimalEntity) {
			return true;
		}
		return false;
	}
	
	
	
	private static boolean doSpellMultiBuff(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime, ServerWorld serverWorld) {

		BlockPos p = targetEntity.getPosition();

		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.WATER_BREATHING);
		if (ei != null) {
			if ((ei.getDuration() < 10) ) {
				targetEntity.removeActivePotionEffect(Effects.WATER_BREATHING);
				ei = null;
			}
		}
		if (ei == null) {
			if ((serverWorld.getBlockState(p).getBlock() == Blocks.WATER) &&
					(serverWorld.getBlockState(p.up()).getBlock() == Blocks.WATER)) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.BUBBLE);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.BUBBLE_COLUMN_UP); 
				serverWorld.playSound(null, targetEntity.getPosition(),
						SoundEvents.ENTITY_DOLPHIN_PLAY, SoundCategory.AMBIENT, 0.9f, 0.25f);
				if (targetEntity.getAir() < targetEntity.getMaxAir()) targetEntity.setAir(targetEntity.getAir()+1);
				int secondsDuration = (spellTime * THIRTY_SECONDS);
				int effectIntensity = 1;
				targetEntity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, secondsDuration/2, effectIntensity, true, true));
				return true;
			}
		}
		
		ei = targetEntity.getActivePotionEffect(Effects.NIGHT_VISION);
		if (ei != null) {
			if ((ei.getDuration() < 10) ) {
				targetEntity.removeActivePotionEffect(Effects.NIGHT_VISION);
				ei = null;
			}
		}			
		if (ei == null) {
			if (serverWorld.getLight(p) <= 8 ) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.END_ROD);		
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.END_ROD); 
				serverWorld.playSound(null, targetEntity.getPosition(),
						SoundEvents.ENTITY_ENDERMAN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
				int secondsDuration = (spellTime * THIRTY_SECONDS);
				int effectIntensity = 1;
				targetEntity.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}
		
		ei = targetEntity.getActivePotionEffect(Effects.FIRE_RESISTANCE);
		if (ei != null) {
			if ((ei.getDuration() < 10) ) {
				targetEntity.removeActivePotionEffect(Effects.FIRE_RESISTANCE);
				ei = null;
			}
		}			
		if (ei == null) {
			if (targetEntity.isBurning()) {
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.END_ROD); 
				serverWorld.playSound(null, targetEntity.getPosition(),
						SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.9f, 0.25f);
				
				int secondsDuration = (spellTime * THIRTY_SECONDS);
				int effectIntensity = 1;
				targetEntity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}

		return false;
	}

	private static boolean doSpellNuke(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellCost,
			DamageSource myDamageSource, ServerWorld serverWorld, int weaponDamage) {

		int damage = (int) targetEntity.getHealth() / 10;
		if (damage == 0) damage = 2;
		damage = damage * spellCost;
		if (damage > BOSS_MOB_LIMIT) damage = BOSS_MOB_LIMIT;
		if (weaponDamage > damage)damage = weaponDamage - 1;


		if (targetEntity.attackEntityFrom(myDamageSource, damage)) {
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.5f, 0.5f);
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.2f, 0.4f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.CAMPFIRE_COSY_SMOKE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, damage, RedstoneParticleData.REDSTONE_DUST); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, damage, ParticleTypes.DAMAGE_INDICATOR); 
			return true;
		}
		return false;
	}

	private static boolean doSpellRemoveCurse(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			ServerWorld serverWorld) {
		serverWorld.playSound(null, serverPlayer.getPosition(),
				SoundEvents.ENTITY_DOLPHIN_SWIM, SoundCategory.AMBIENT, 0.6f, 0.25f);
		if (!(serverPlayer.inventory.hasItemStack(MILK_STACK))) {
			MyConfig.sendChat(serverPlayer, "You have no milk in your inventory.", Color.func_240744_a_(TextFormatting.DARK_RED));
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.8f, 0.1f);	
			return false;
		}
		// small chance to replace milk bucket with empty bucket.
		

		EffectInstance ei = null;
		boolean curseRemoved = false;
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.BLINDNESS);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.BLINDNESS);
				curseRemoved = true;
			}
		}
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.WITHER);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.WITHER);
				curseRemoved = true;
			}
		}
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.POISON);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.POISON);
				curseRemoved = true;
			}
		}
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				curseRemoved = true;
			}
			
		}
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.MINING_FATIGUE);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.MINING_FATIGUE);
				curseRemoved = true;
			}
		}
		
		if (curseRemoved) {
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.AMBIENT, 0.6f, 0.65f);	
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.6f, 0.75f);	
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.WITCH);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WITCH); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WHITE_ASH); 
		}
		MyConfig.dbgPrintln(1, "Remove Curse: remove one negative effect");

		return curseRemoved;
	}

	private static boolean doSpellResistance(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime, ServerWorld serverWorld) {
		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.RESISTANCE);
		int effectDuration = FOUR_SECONDS + spellTime * FOUR_SECONDS;
		int effectIntensity = 0;
		if (ei != null) {
			int durationLeft = ei.getDuration();
			int currentAmplifier = ei.getAmplifier();
			if (durationLeft >= 6) {
				if (currentAmplifier > 0) {
					return false;
				}
				effectIntensity = 1;
				effectDuration = THIRTY_SECONDS + FOUR_SECONDS;
			} 
			targetEntity.removeActivePotionEffect(Effects.RESISTANCE);
		}
		targetEntity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, effectDuration, effectIntensity, true, true));
		if (effectIntensity == 0) {
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_DOLPHIN_SWIM, SoundCategory.AMBIENT, 0.9f, 0.25f);
			
		} else {
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.AMBIENT, 0.9f, 0.5f);
		}
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ENCHANT);
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WHITE_ASH); 
		return true;
	}

	private static boolean doSpellSnareDot(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, int weaponDamage) {

		int effectIntensity = (int) targetEntity.getHealth() / 20;
		if (effectIntensity > BOSS_MOB_LIMIT / 5 ) effectIntensity = BOSS_MOB_LIMIT /5; 
		if (weaponDamage / 6 > effectIntensity) effectIntensity = weaponDamage/6;			
		int secondsDuration = FOUR_SECONDS * spellTime;
		
		boolean damage = false;
		damage = targetEntity.attackEntityFrom(myDamageSource, spellTime);
		
		if ((targetEntity instanceof PlayerEntity) && (!(damage))) {
			return false;  // PVP hack til I find server settings.  Basically if a player and not damaged by nuke, then don't apply DoT to them.
		}
		
		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.WITHER);
		if (ei != null) {
			if ((ei.getDuration() < 6) || (ei.getAmplifier() <= effectIntensity)) {
				targetEntity.removeActivePotionEffect(Effects.WITHER);
			}
		}
		targetEntity.addPotionEffect(new EffectInstance(Effects.WITHER, secondsDuration, effectIntensity, true, true));
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ASH);
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ASH); 

		// mobs with over 140hp are not affected by snare
		if (targetEntity.getHealth() < BOSS_MOB_LIMIT * 4) {
			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				if ((ei.getDuration() < 6) || (ei.getAmplifier() <= effectIntensity)) {
					targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				}
			}
			targetEntity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, secondsDuration, effectIntensity, true, true));
		}
		serverWorld.playSound(null, targetEntity.getPosition(),	SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		serverWorld.playSound(null, targetEntity.getPosition(),	SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.DAMAGE_INDICATOR);
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ANGRY_VILLAGER); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.SMOKE); 
		return true;
	}

	private static boolean doSpellTeleport(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			ServerWorld serverWorld) {
		if (spellTime < 4) {
			MyConfig.sendChat(serverPlayer,"Cast Longer to Teleport.",Color.func_240744_a_(TextFormatting.YELLOW));
			return false;
		}
		if (targetEntity.world.func_234923_W_() != World.field_234918_g_) {
			MyConfig.sendChat(serverPlayer,"You can only teleport in the Overworld.",Color.func_240744_a_(TextFormatting.YELLOW));
			return false;
		}
		// TODO Check for configurable reagent.
		float headPitch = targetEntity.rotationPitch;
		float headYaw = targetEntity.rotationYaw;
		// default - teleport to worldspawn.
		int wX = targetEntity.world.getWorldInfo().getSpawnX();
		int wY = targetEntity.world.getWorldInfo().getSpawnY();
		int wZ = targetEntity.world.getWorldInfo().getSpawnZ();

		ServerPlayerEntity serverTargetPlayer = (ServerPlayerEntity) targetEntity;
		BlockPos personalSpawnPos = serverTargetPlayer.func_241140_K_();
		float pitch = 0.4f;
		float soundVolume = 0.8f;
		if (headPitch > 0) { // looking down- teleport to personal spawn instead.
			wX = personalSpawnPos.getX();
			wY = personalSpawnPos.getY()+ 1;
			wZ = personalSpawnPos.getZ();
			pitch = 0.5f;
			soundVolume = 0.6f;
		}

		ChunkPos chunkPos = new ChunkPos(new BlockPos (wX,wY,wZ));

		serverWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1 , serverTargetPlayer.getEntityId());
		targetEntity.world.playSound(null, targetEntity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL,
				SoundCategory.AMBIENT, soundVolume, pitch);
		serverTargetPlayer.teleport(serverWorld, (double)wX, (double)wY, (double)wZ, headYaw, headPitch);
		return true;
	}

	public static void drawSpellBeam (ServerPlayerEntity serverPlayer, ServerWorld serverWorld, Entity targetEntity, IParticleData spellParticleType) {
		Vector3d vector3d = serverPlayer.getEyePosition(1.0F);
		Vector3d vectorFocus = vector3d.subtract(0.0,0.4,0.0);
		Vector3d vector3d1 = serverPlayer.getLook(1.0F);
		vector3d.subtract(0.0,0.4,0.0);
		Vector3d target3d = targetEntity.getEyePosition(1.0F);
		double targetDistance = vector3d.distanceTo(target3d);
		boolean doSpellParticleType = true;
		for (double d0 = 1.0; d0 < targetDistance; d0=d0+0.5D) {
			Vector3d beamPath3d2 = vectorFocus.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
			serverSpawnMagicalParticles(beamPath3d2, serverWorld, 1, RedstoneParticleData.REDSTONE_DUST); 
			doSpellParticleType = !doSpellParticleType;
			if (doSpellParticleType) {
				Vector3d swirlPath3d2 = beamPath3d2;
				swirlPath3d2.normalize();
				serverSpawnMagicalParticles(beamPath3d2, serverWorld, 1, spellParticleType); 
				doSpellParticleType = !doSpellParticleType;
			}
		}
	}

	public static LivingEntity lookForDistantTarget(PlayerEntity clientPlayer) {
		double d0 = 30;
		double d1 = d0 * d0;
		Vector3d vector3d = clientPlayer.getEyePosition(1.0F);
		Vector3d vector3d1 = clientPlayer.getLook(1.0F);
		Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

		AxisAlignedBB axisalignedbb = clientPlayer.getBoundingBox().expand(vector3d1.scale(d0)).grow(1.0D, 1.0D, 1.0D);
		EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(clientPlayer, vector3d, vector3d2,
				axisalignedbb, (p_215312_0_) -> {
					return !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith();
				}, d1);
		if (entityraytraceresult != null) {
			Entity entity1 = entityraytraceresult.getEntity();
			Vector3d vector3d3 = entityraytraceresult.getHitVec();
			if (entity1 instanceof LivingEntity) {
				return (LivingEntity) entity1;
			}
		}
		return null;
	}

	// server dist
	public static void processSpellOnServer(int spellNumber, LivingEntity targetEntity, ServerPlayerEntity serverPlayer,
			int spellTime) {

		IMagicStorage playerManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (playerManaStorage == null) {
			MyConfig.sendChat(serverPlayer, "Impossible Error: You do not have a mana pool.",
					Color.func_240744_a_(TextFormatting.YELLOW));
			return;
		}

		String spellKey = Integer.toString(spellNumber);
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(spellKey);
		int spellCost = spell.getSpellBaseCost() * spellTime;
		if (spellCost > playerManaStorage.getManaStored()) {
			serverPlayer.world.playSound(serverPlayer, serverPlayer.getPosition(), SoundEvents.BLOCK_BASALT_FALL,
					SoundCategory.AMBIENT, 0.5f, 0.8f);
			MyConfig.sendChat(serverPlayer, "Not Enough Mana!", Color.func_240744_a_(TextFormatting.YELLOW));
			return;
		}

		total_calls = total_calls + 1;
		if (castSpellAtTarget(serverPlayer, targetEntity, spellTime, spell) ) {
			serverPlayer.getServerWorld().playSound(null, serverPlayer.getPosition(),SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.AMBIENT, 0.4f, 0.9f);
			playerManaStorage.useMana(spellCost);
//			// TODO get chunk mana here and use 1 mana from chunk
			Network.sendToClient(new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE), serverPlayer);			
			MyConfig.sendChat(serverPlayer, "You have " + playerManaStorage.getManaStored() + "mana left.", Color.func_240744_a_(TextFormatting.RED));
		} else {
			MyConfig.sendChat(serverPlayer, "You have " + playerManaStorage.getManaStored() + "mana left.", Color.func_240744_a_(TextFormatting.RED));
			serverPlayer.getServerWorld().playSound(null, serverPlayer.getPosition(),SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.AMBIENT, 0.4f, 0.25f);
			MyConfig.dbgPrintln(1, "Serverside Spell :" + spell.getSpellComment() + ".  failed. calls:" + total_calls  +"." );
		}
		
	}

	public static void serverSpawnMagicalParticles(Entity entity, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

          double xOffset = 0.75D;
          double yOffset = 0.3D;
          double zOffset = 0.75D;
          particleCount *= 3;
          serverWorld.spawnParticle(particleType, posX, posY+(double)entity.getEyeHeight(), posZ, particleCount, xOffset, yOffset, zOffset, -0.04D);                
  		  int debug = 2;
    }
	
    public static void serverSpawnMagicalParticles(Vector3d bV3D, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

        double xOffset = 0.25D * (Math.sin(bV3D.getX()));
        double yOffset = 0.15D * (Math.cos(bV3D.getY())); 
        double zOffset = 0.25D * (Math.cos(bV3D.getZ()));
//        double xOffset = 0.25D ;
//        double yOffset = 0.15D ;
//        double zOffset = 0.25D ;
        particleCount *= 2;
        serverWorld.spawnParticle(particleType, bV3D.getX(), bV3D.getY(), bV3D.getZ(), particleCount, xOffset, yOffset, zOffset, -0.04D);                
		  int debug = 2;
  }

    public static LivingEntity target(PlayerEntity player) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.objectMouseOver.getType() == Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
			if (entity instanceof LivingEntity)
				return (LivingEntity) entity;
		}
		return null;
	}
}
