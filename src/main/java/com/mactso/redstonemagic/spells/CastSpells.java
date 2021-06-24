package com.mactso.redstonemagic.spells;

import java.util.Collection;
import java.util.Random;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.item.RedstoneFocusItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
public class CastSpells {
	static final int THREE_SECONDS = 60;
	static final int FOUR_SECONDS = 80;
	static final int THIRTY_SECONDS = 600;
	static final int TICKS_PER_SECOND = 20;
	static final int FULL_INTENSITY = 80;
	static final int BOSS_MOB_LIMIT = 40;
	static final int NO_CHUNK_MANA_UPDATE = -1;
	static double posX = 0;
	static double posY = 0;
	static double posZ = 0;
	static final ItemStack MILK_STACK = new ItemStack (Items.MILK_BUCKET);
	static final ItemStack FEATHER_STACK = new ItemStack (Items.FEATHER);
	static final ItemStack PUFFERFISH_STACK = new ItemStack (Items.PUFFERFISH);
	static final ItemStack BLUE_ICE_STACK = new ItemStack (Items.BLUE_ICE);
	static final ItemStack GLISTERING_MELON_SLICE_STACK = new ItemStack (Items.GLISTERING_MELON_SLICE);
	static final ItemStack GOLDEN_CARROT_STACK = new ItemStack (Items.GOLDEN_CARROT);
	static final ItemStack FIRE_CHARGE_STACK = new ItemStack (Items.FIRE_CHARGE);
	static final ItemStack REDSTONE_STACK = new ItemStack (Items.REDSTONE);
	static final ItemStack MAGMA_CREAM_STACK = new ItemStack (Items.MAGMA_CREAM);
	static final ItemStack FERMENTED_SPIDER_EYE_STACK = new ItemStack (Items.FERMENTED_SPIDER_EYE);
	static final ItemStack SPIDER_EYE_STACK = new ItemStack (Items.SPIDER_EYE);
	static final ItemStack TURTLE_HELMET = new ItemStack (Items.TURTLE_HELMET);
	static final ItemStack GLOWSTONE_DUST_STACK = new ItemStack (Items.GLOWSTONE_DUST);
	static final ItemStack NETHER_STAR_STACK = new ItemStack (Items.NETHER_STAR);
	static final ItemStack DRAGON_EGG_STACK = new ItemStack (Items.DRAGON_EGG);
	static int total_calls = 0; 
	

	private static boolean castSpellAtTarget(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			RedstoneMagicSpellItem spell, BlockPos targetPos) {
		
		String spellTranslationKey = spell.getSpellTranslationKey();
		String spellTargetType = spell.getSpellTargetType();
		DamageSource myDamageSource = DamageSource.playerAttack(serverPlayer).bypassArmor()
				.setMagic();
		
		ServerWorld serverWorld = (ServerWorld) targetEntity.level;
		
	  	posX = targetEntity.getX();
	  	posY = targetEntity.getY();
	  	posZ = targetEntity.getZ();
	  	
	  	int baseWeaponDamage = 1;

		ItemStack handItem = serverPlayer.getMainHandItem();
		float damageModifierForCreature = 0.1f;
        if (targetEntity instanceof LivingEntity) {
            damageModifierForCreature = EnchantmentHelper.getDamageBonus(serverPlayer.getMainHandItem(), ((LivingEntity)targetEntity).getMobType());
         } else {
            damageModifierForCreature = EnchantmentHelper.getDamageBonus(serverPlayer.getMainHandItem(), CreatureAttribute.UNDEFINED);
         }
  	
	  	Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier attr : d)
        {
            baseWeaponDamage = (int) attr.getAmount()+1;
        }
		if (spellTime > 4) spellTime = 4;
		
        float weaponDamage = baseWeaponDamage + ((spellTime/2) * (spellTime/2));
		if (weaponDamage < 2.0f) {
			weaponDamage = 2.0f;
		}
		
//		MyConfig.sendChat(serverPlayer, "baseWeaponDamage:"+baseWeaponDamage+" weaponDamage: "+weaponDamage, Color.fromHex("0x3333FF"));

//		MyConfig.sendChat(serverPlayer, "Cast A Spell before Switch.  Spell is " + spell.getSpellTranslationKey(), Color.fromLegacyFormat(TextFormatting.RED));

//***
		if (spell.getSpellTranslationKey().equals("redstonemagic.nuke")) { // red bolt
			return doSpellNuke(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage, damageModifierForCreature);
		}
//***
		if (spellTranslationKey.equals(("redstonemagic.dot"))) { // sepsis
			return doSpellDoT(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage, damageModifierForCreature);
		}
//***
		if (spellTranslationKey.equals(("redstonemagic.sdot"))) { // crimson cloud
			return doSpellSnareDot(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage, damageModifierForCreature);
		}
//***
		if (spellTranslationKey.equals("redstonemagic.buff")) { // multi-buff
			return doSpellMultiBuff(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("redstonemagic.heal")) { // Crimson Heal
			return doSpellHeal(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage, damageModifierForCreature, targetPos);
		}
//***
		if (spellTranslationKey.equals("redstonemagic.resi")) { // Resistance
			return doSpellResistance(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("redstonemagic.rcrs")) { // remove curse
			return doSpellRemoveCurse(serverPlayer, targetEntity, spellTime, serverWorld);
		}
//***
		if (spellTranslationKey.equals("redstonemagic.tele")) { // Cardinal Call
			return doSpellTeleport(serverPlayer, targetEntity, spellTime, serverWorld);
		}		
		return false;
	}
	
	private static boolean doSpellDoT(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForTarget) {

		
		int totalDamage = (int) (targetEntity.getMaxHealth() * 0.7);
		int nukeDamage = spellTime/4;
		int dotDamage = totalDamage - nukeDamage;
		
		weaponDamage = weaponDamage + damageModifierForTarget;
		if (dotDamage > BOSS_MOB_LIMIT) dotDamage = BOSS_MOB_LIMIT;
		if (dotDamage > (weaponDamage * 2.0f)) dotDamage = (int) (weaponDamage*2.0f);
		if (dotDamage < weaponDamage) dotDamage = (int) (weaponDamage/0.4f);
		int duration = 1 + (dotDamage*spellTime)/4;
		int intensity = 0;
		if (dotDamage > 32) {
			intensity = 1;
			duration /= 2;
		}

		if (hasFalderal(serverPlayer, SPIDER_EYE_STACK)) {
			duration += 1;
		}
		
		if (canAttackTarget(serverPlayer, targetEntity)== false) {
			return false;
		}
		
//		boolean nukeHit = false;
		myDamageSource.bypassArmor();
		boolean nukeHit = targetEntity.hurt(myDamageSource, nukeDamage);
		
		EffectInstance ei = targetEntity.getEffect(Effects.POISON);
		if (ei != null) {
			if (ei.getDuration() > 19) {
				return false;
			}
			if (ei.getAmplifier() <= intensity) {
				targetEntity.removeEffect(Effects.POISON);
			}
		}

		targetEntity.hurt(myDamageSource, spellTime);
		targetEntity.addEffect(new EffectInstance(Effects.POISON,  duration * TICKS_PER_SECOND, intensity, true, true));
		LivingEntity lE = (LivingEntity) targetEntity;
		lE.setLastHurtByPlayer(serverPlayer); // set attacking player (I think)
		serverWorld.playSound(null, targetEntity.blockPosition(),SoundEvents.LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		serverWorld.playSound(null, targetEntity.blockPosition(),SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ITEM_SLIME);
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ANGRY_VILLAGER); 
		return true;
		
	}

	private static boolean canAttackTarget(ServerPlayerEntity serverPlayer, LivingEntity targetEntity) {
		boolean canAttackTargetEntity = true;
		if (targetEntity instanceof PlayerEntity) {
			if (serverPlayer.canHarmPlayer((PlayerEntity) targetEntity) == false) {
				canAttackTargetEntity = false;
			}
		}
		
		if (targetEntity instanceof TameableEntity) {
			TameableEntity t = (TameableEntity) targetEntity;
			if (t.isTame()) {
				if ( t.getOwner() instanceof PlayerEntity) {
					PlayerEntity p = (PlayerEntity) t.getOwner();
					if (serverPlayer.canHarmPlayer(p) == false) {
						canAttackTargetEntity = false;
					}
				}
			}
		}
		return canAttackTargetEntity;
	}

	private static boolean isValidPVETarget(LivingEntity targetEntity) {

		if (targetEntity instanceof TameableEntity) {
			TameableEntity t = (TameableEntity) targetEntity;
			if (t.isTame()) {
				return false;
			}
		}
		
		if (targetEntity instanceof PlayerEntity) {
			return false;
		}
		
		return true;
	}

	private static boolean doSpellHeal(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForTarget, BlockPos targetPos) {

		if (targetEntity.isInvertedHealAndHarm()) {
			float totalDamage = targetEntity.getMaxHealth() * (0.125f * spellTime);
			float nukeDamage = totalDamage;
			weaponDamage = weaponDamage + damageModifierForTarget;
			if (nukeDamage > BOSS_MOB_LIMIT) nukeDamage = BOSS_MOB_LIMIT;
			if (nukeDamage > weaponDamage * 2) nukeDamage = (int) (weaponDamage * 2) - 1 ;
			if (nukeDamage <= weaponDamage) nukeDamage = (int) (weaponDamage - 1);
			if (spellTime >= 4) nukeDamage += 1;
		
			if (targetEntity.hurt(myDamageSource, nukeDamage)) {
				serverWorld.playSound(null, serverPlayer.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.2f, 0.9f);
				serverWorld.playSound(null, targetEntity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.9f, 0.25f);
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.DAMAGE_INDICATOR);
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.CAMPFIRE_COSY_SMOKE);
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int) nukeDamage, ParticleTypes.DAMAGE_INDICATOR);
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE);
				return true;
			} else {
				return false;
			}
		}

		if ((isHealable(targetEntity))) {
			float healDamage = targetEntity.getMaxHealth() * .10f * spellTime;
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ASH);
			targetEntity.heal(healDamage);
			if(targetEntity instanceof WolfEntity) {
				WolfEntity wE = (WolfEntity) targetEntity;
				if (wE.getLastHurtByMob() instanceof PlayerEntity) {
					wE.setLastHurtByMob(null);
				}
			}
			serverWorld.playSound(null, serverPlayer.blockPosition(), SoundEvents.BEACON_ACTIVATE,
					SoundCategory.AMBIENT, 0.2f, 0.8f);
			serverWorld.playSound(null, targetEntity.blockPosition(), SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE,
					SoundCategory.AMBIENT, 0.7f, 0.86f);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.HEART);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE);
			return true;
		}
		else if (hasFalderal(serverPlayer, GLOWSTONE_DUST_STACK)) {
			if (targetPos != null) {
				serverWorld.setBlockAndUpdate(targetPos, ModBlocks.LIGHT_SPELL.defaultBlockState());
				serverSpawnMagicalParticles(targetPos, serverWorld, spellTime, RedstoneParticleData.REDSTONE);
				serverWorld.playSound(null, targetEntity.blockPosition(), ModSounds.REDSTONEMAGIC_LIGHT,
						SoundCategory.AMBIENT, 0.7f, 0.86f);
			}
			return true;
		}
		else {
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
		if (entity instanceof VillagerEntity ) {
			return true;
		}
		if (entity instanceof AnimalEntity) {
			return true;
		}
		return false;
	}
	
	
	
	
	private static boolean doSpellMultiBuff(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime, ServerWorld serverWorld) {

		BlockPos pos = targetEntity.blockPosition();
		int netherBoost = 0;
		if (hasFalderal(serverPlayer,NETHER_STAR_STACK)) {
			netherBoost = THIRTY_SECONDS;
		}

		Random r = serverPlayer.getLevel().getRandom();
		int randomBoost = (int) (320 * r.nextFloat());
		
		int durationBoosts = netherBoost + randomBoost;
		
		if (hasFalderal(serverPlayer, FEATHER_STACK)) {
			EffectInstance ei = targetEntity.getEffect(Effects.SLOW_FALLING);
			if (ei != null) {
				if ((ei.getDuration() < 20) ) {
					targetEntity.removeEffect(Effects.SLOW_FALLING);
					ei = null;
				}
			}
			if (ei == null) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.SPLASH);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.SOUL_FIRE_FLAME); 
				serverWorld.playSound(null, targetEntity.blockPosition(),
						SoundEvents.CHICKEN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
				if (targetEntity.getAirSupply() < targetEntity.getMaxAirSupply()) targetEntity.setAirSupply(targetEntity.getAirSupply()+1);
				int secondsDuration = (spellTime * THIRTY_SECONDS/2) + durationBoosts;
				int effectIntensity = 1;
				targetEntity.addEffect(new EffectInstance(Effects.SLOW_FALLING, secondsDuration/2, effectIntensity, true, true));
				return true;
			}
			
		}
		EffectInstance ei = targetEntity.getEffect(Effects.WATER_BREATHING);
		if (ei != null) {
			if ((ei.getDuration() < 40) ) {
				targetEntity.removeEffect(Effects.WATER_BREATHING);
				ei = null;
			}
		}
		if (ei == null) {
			if (isUnderwater(serverWorld, pos) ||
					(hasFalderal(serverPlayer, PUFFERFISH_STACK))){
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.BUBBLE);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.BUBBLE_COLUMN_UP); 
				serverWorld.playSound(null, targetEntity.blockPosition(),
						SoundEvents.DOLPHIN_PLAY, SoundCategory.AMBIENT, 0.9f, 0.25f);
				if (targetEntity.getAirSupply() < targetEntity.getMaxAirSupply()) targetEntity.setAirSupply(targetEntity.getAirSupply()+1);
				int falderalBoost = 0;
				if (hasFalderal(serverPlayer, new ItemStack (Items.PUFFERFISH))) {
					falderalBoost = THIRTY_SECONDS/2;
				}
				int secondsDuration = (spellTime * (THIRTY_SECONDS+falderalBoost)) + durationBoosts;
				int effectIntensity = 1;
				targetEntity.addEffect(new EffectInstance(Effects.WATER_BREATHING, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}
		
		World w = (World) serverWorld;
		if ((serverWorld.getMaxLocalRawBrightness(pos) <= 8 ) || (hasFalderal(serverPlayer, GOLDEN_CARROT_STACK))) {
			ei = targetEntity.getEffect(Effects.NIGHT_VISION);
			if (ei != null) {
				if ((ei.getDuration() < 20) ) {
					targetEntity.removeEffect(Effects.NIGHT_VISION);
					ei = null;
				}
			}	
			if (ei==null) {
				RegistryKey<World> rK = w.dimension();
				ResourceLocation rL1 = rK.location();
				ResourceLocation rl2 = rK.getRegistryName();
				boolean castNightVision = true;
				if ((rL1.getPath().equals("the_nether")) || (rL1.getPath().equals("the_end"))) {
					castNightVision = false;
				}
				if (hasFalderal(serverPlayer, GOLDEN_CARROT_STACK)) {
					castNightVision = true;
				}
				if (castNightVision == true) {
					drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.END_ROD);		
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.END_ROD); 
					serverWorld.playSound(null, targetEntity.blockPosition(),
							SoundEvents.ENDERMAN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
					int falderalBoost = 0;
					if (hasFalderal(serverPlayer, GOLDEN_CARROT_STACK)) {
						falderalBoost = THIRTY_SECONDS/2;
					}
					int secondsDuration = (spellTime * (THIRTY_SECONDS + falderalBoost)) + durationBoosts;
					int effectIntensity = 1;
					targetEntity.addEffect(new EffectInstance(Effects.NIGHT_VISION, secondsDuration, effectIntensity, true, true));
					return true;
				}
			}
		}


		ei = targetEntity.getEffect(Effects.FIRE_RESISTANCE);
		if (ei != null) {
			if ((ei.getDuration() < 40) ) {
				targetEntity.removeEffect(Effects.FIRE_RESISTANCE);
				ei = null;
			}
		}			
		if (ei == null) {
			if ((targetEntity.isOnFire()) || (hasFalderal(serverPlayer, MAGMA_CREAM_STACK))) {
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.END_ROD); 
				serverWorld.playSound(null, targetEntity.blockPosition(),
						SoundEvents.FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.9f, 0.25f);
				int falderalBoost = 0;
				if (hasFalderal(serverPlayer, MAGMA_CREAM_STACK)) {
					falderalBoost = THIRTY_SECONDS/2;
				}
				int secondsDuration = (spellTime * (THIRTY_SECONDS + falderalBoost)) + durationBoosts;				
				int effectIntensity = 1;
				targetEntity.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}

		if (hasFalderal(serverPlayer, FERMENTED_SPIDER_EYE_STACK)) {
			ei = targetEntity.getEffect(Effects.INVISIBILITY);
			if (ei != null) {
				if ((ei.getDuration() < 80) ) {
					targetEntity.removeEffect(Effects.INVISIBILITY);
					ei = null;
				}
			}
			if (ei == null) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.POOF);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.POOF); 
				serverWorld.playSound(null, targetEntity.blockPosition(),
						SoundEvents.BAT_HURT, SoundCategory.AMBIENT, 0.9f, 0.25f);
				int secondsDuration = ((spellTime * THIRTY_SECONDS) /2) + durationBoosts;	
				int effectIntensity = 1;
				targetEntity.addEffect(new EffectInstance(Effects.INVISIBILITY, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}

		return false;
	}

	private static boolean isUnderwater(ServerWorld serverWorld, BlockPos p) {
		return (serverWorld.getBlockState(p).getBlock() == Blocks.WATER) && (serverWorld.getBlockState(p.above()).getBlock() == Blocks.WATER);
	}

	private static boolean doSpellNuke(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForTarget) {

		if (canAttackTarget(serverPlayer, targetEntity)== false) {
			return false;
		}

		float totalDamage = (int) (targetEntity.getMaxHealth() * (0.1f * spellTime));
		float nukeDamage = totalDamage;
		weaponDamage = weaponDamage + damageModifierForTarget;
		if (nukeDamage > BOSS_MOB_LIMIT) nukeDamage = BOSS_MOB_LIMIT;
		if (nukeDamage > weaponDamage * 2) nukeDamage = (int) (weaponDamage * 2) - 1 ;
		if (nukeDamage <= weaponDamage) nukeDamage = (int) (weaponDamage - 1);
		if (spellTime >= 4) nukeDamage += 1;
		
		if (useReagent(serverPlayer, REDSTONE_STACK))
			nukeDamage = nukeDamage + 1;

		int debugz = 3;
		if (hasFalderal(serverPlayer, BLUE_ICE_STACK)) {
			if (targetEntity.fireImmune()) {
				nukeDamage += 2;
			}
			LivingEntity lE = (LivingEntity) targetEntity;
			lE.setLastHurtByPlayer(serverPlayer); // set attacking player (I think)
			targetEntity.hurt(myDamageSource, nukeDamage);
			serverWorld.playSound(null, serverPlayer.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
					SoundCategory.AMBIENT, 0.5f, 0.5f);
			serverWorld.playSound(null, targetEntity.blockPosition(), ModSounds.REDSTONEMAGIC_NUKE_ICY,
					SoundCategory.AMBIENT, 0.3f, 0.4f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.SOUL_FIRE_FLAME);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int) nukeDamage, RedstoneParticleData.REDSTONE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int) spellTime, ParticleTypes.CAMPFIRE_COSY_SMOKE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int) nukeDamage, ParticleTypes.DAMAGE_INDICATOR);
			return true;
		} else if (hasFalderal(serverPlayer, FIRE_CHARGE_STACK)) {
			if (!(targetEntity.fireImmune())) {
				LivingEntity lE = (LivingEntity) targetEntity;
				lE.setLastHurtByPlayer(serverPlayer); // set attacking player (I think)
				float targetHealth = targetEntity.getHealth();
				if (nukeDamage > 3) {
					nukeDamage = nukeDamage - 2;
				}
				targetEntity.setHealth(targetHealth - (float) nukeDamage);
				targetEntity.setSecondsOnFire(3);
				serverWorld.playSound(null, serverPlayer.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.5f, 0.5f);
				serverWorld.playSound(null, targetEntity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
						SoundCategory.AMBIENT, 0.2f, 0.4f);
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.LAVA);
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int) nukeDamage,
						RedstoneParticleData.REDSTONE);
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int) spellTime,
						ParticleTypes.CAMPFIRE_COSY_SMOKE);
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int) nukeDamage, ParticleTypes.DAMAGE_INDICATOR);
				return true;
			} else {
				targetEntity.setSecondsOnFire(2);
				LivingEntity lE = (LivingEntity) targetEntity;
				lE.setLastHurtByPlayer(serverPlayer); // set attacking player (I think)
				return false;
			}
		} else // magic damage
		if (targetEntity.hurt(myDamageSource, nukeDamage)) {
			serverWorld.playSound(null, serverPlayer.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
					SoundCategory.AMBIENT, 0.5f, 0.5f);
			serverWorld.playSound(null, targetEntity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
					SoundCategory.AMBIENT, 0.2f, 0.4f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, RedstoneParticleData.REDSTONE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int)nukeDamage, RedstoneParticleData.REDSTONE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int)nukeDamage, ParticleTypes.DAMAGE_INDICATOR);
			return true;
		}
		return false;
	}

	public static boolean hasFalderal(ServerPlayerEntity serverPlayer, ItemStack falderalStack) {
		int slot = findHotBarSlotWithItemType(serverPlayer, falderalStack);
		if ((slot > 0) && (slot < 9)) {
			return true;
		}
		return false;
	}

	public static boolean useReagent (ServerPlayerEntity serverPlayer, ItemStack reagentStack) {
		int slot = findHotBarSlotWithItemType (serverPlayer, reagentStack);
		if ((slot >0 ) && (slot< 9)) {
			serverPlayer.inventory.getItem(slot).shrink(2);
			return true;
		}
		return false;	
	}
	
	private static int findHotBarSlotWithItemType (ServerPlayerEntity serverPlayer, ItemStack stack) {
	    for(int i = 0; i < 9; ++i) {
	        if(serverPlayer.inventory.getItem(i).getItem() == stack.getItem()) {
	        	return i;
	        }
	    }		
		return -1;
	}
	
	private static boolean doSpellRemoveCurse(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			ServerWorld serverWorld) {
		serverWorld.playSound(null, serverPlayer.blockPosition(),
				SoundEvents.DOLPHIN_SWIM, SoundCategory.AMBIENT, 0.6f, 0.25f);
		if (!(serverPlayer.inventory.contains(MILK_STACK))) {
			MyConfig.sendChat(serverPlayer, "You have no milk in your inventory.", Color.fromLegacyFormat(TextFormatting.DARK_RED));
			serverWorld.playSound(null, serverPlayer.blockPosition(),
					ModSounds.SPELL_FAILS, SoundCategory.AMBIENT, 0.8f, 0.1f);	
			return false;
		}
		// small chance to replace milk bucket with empty bucket.
		
		EffectInstance ei = null;
		boolean curseRemoved = false;
		if (!(curseRemoved)) {
			curseRemoved = targetEntity.removeEffect(Effects.BLINDNESS);
		}
		if (!(curseRemoved)) {
			curseRemoved = targetEntity.removeEffect(Effects.WITHER);
		}
		if (!(curseRemoved)) {
			curseRemoved = targetEntity.removeEffect(Effects.POISON);
		}
		if (!(curseRemoved)) {
			curseRemoved = targetEntity.removeEffect(Effects.MOVEMENT_SLOWDOWN);
		}
		if (!(curseRemoved)) {
			curseRemoved = targetEntity.removeEffect(Effects.DIG_SLOWDOWN);
		}
		
		if (curseRemoved) {
			serverWorld.playSound(null, serverPlayer.blockPosition(),
					SoundEvents.NOTE_BLOCK_BELL, SoundCategory.AMBIENT, 0.6f, 0.65f);	
			serverWorld.playSound(null, serverPlayer.blockPosition(),
					SoundEvents.NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.6f, 0.75f);	
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.WITCH);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WITCH); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WHITE_ASH); 
		}

		return curseRemoved;
	}

	private static boolean doSpellResistance(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime, ServerWorld serverWorld) {
		EffectInstance ei = targetEntity.getEffect(Effects.DAMAGE_RESISTANCE);
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
			targetEntity.removeEffect(Effects.DAMAGE_RESISTANCE);
		}
		targetEntity.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, effectDuration, effectIntensity, true, true));
		if (effectIntensity == 0) {
			serverWorld.playSound(null, targetEntity.blockPosition(),
					SoundEvents.BELL_BLOCK, SoundCategory.AMBIENT, 0.9f, 0.25f);
			
		} else {
			serverWorld.playSound(null, targetEntity.blockPosition(),
					SoundEvents.ANVIL_LAND, SoundCategory.AMBIENT, 0.9f, 0.5f);
		}
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ENCHANT);
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.WHITE_ASH); 
		return true;
	}

	private static boolean doSpellSnareDot(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForTarget) {

		
		if (canAttackTarget(serverPlayer, targetEntity)== false) {
			return false;
		}
		
		
		int totalDamage = (int) (targetEntity.getMaxHealth() * 0.6);
		int nukeDamage = spellTime/4;
		int dotDamage = totalDamage - nukeDamage;
		
		weaponDamage = weaponDamage + damageModifierForTarget;
		if (dotDamage > BOSS_MOB_LIMIT) dotDamage = BOSS_MOB_LIMIT;
		if (dotDamage > (weaponDamage * 2.0f)) dotDamage = (int) (weaponDamage*2.0f);
		if (dotDamage < weaponDamage) dotDamage = (int) (weaponDamage - nukeDamage);
		int duration = 1 + (dotDamage*spellTime)/4;
		int intensity = 0;
		if (dotDamage > 32) {
			intensity = 1;
			duration = dotDamage / 2;
		}

		if (hasFalderal(serverPlayer, SPIDER_EYE_STACK)) {
			duration += 1;
		}
		if (hasFalderal(serverPlayer, TURTLE_HELMET)) {
			duration += 1;
		}


		myDamageSource.bypassArmor();
//		boolean nukeHit = false;
		boolean nukeHit = targetEntity.hurt(myDamageSource, nukeDamage);

		EffectInstance ei = targetEntity.getEffect(Effects.WITHER);
		if (ei != null) {
			if ((ei.getDuration() < 19) || (ei.getAmplifier() <= intensity)) {
				targetEntity.removeEffect(Effects.WITHER);
			}
		}
		targetEntity.addEffect(new EffectInstance(Effects.WITHER, (duration * 45), intensity, true, true));
		LivingEntity lE = (LivingEntity) targetEntity;
		lE.setLastHurtByPlayer(serverPlayer); // set attacking player (I think)
		
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_STAINED_GLASS.defaultBlockState()));
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ASH); 
		// mobs with over 140hp are not affected by snare
		if (targetEntity.getMaxHealth() < BOSS_MOB_LIMIT * 4) {
			ei = targetEntity.getEffect(Effects.MOVEMENT_SLOWDOWN);
			if (ei != null) {
				if ((ei.getDuration() < 19) || (ei.getAmplifier() <= 0)) {
					targetEntity.removeEffect(Effects.MOVEMENT_SLOWDOWN);
				}
			}
			targetEntity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration * 61, 0, true, true));
		}
		if (nukeHit) {
			serverWorld.playSound(null, targetEntity.blockPosition(),	SoundEvents.LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		}
		serverWorld.playSound(null, targetEntity.blockPosition(),	SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, new BlockParticleData(ParticleTypes.BLOCK, Blocks.CRYING_OBSIDIAN.defaultBlockState()));
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, new BlockParticleData(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState())); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_STAINED_GLASS.defaultBlockState())); 
		return true;
	}

	private static boolean doSpellTeleport(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			ServerWorld serverWorld) {

		if (serverPlayer.level.dimension() != World.OVERWORLD) {
			MyConfig.sendChat(serverPlayer,"You can only teleport in the Overworld.",Color.fromLegacyFormat((TextFormatting.YELLOW)));
			return false;
		}
		
		boolean teleportPetOrHorse = false;
		if (targetEntity instanceof TameableEntity) {
			TameableEntity p = (TameableEntity) targetEntity;
			if (p.isTame()) {
				if (p.getOwner() == serverPlayer) {
					teleportPetOrHorse = true;
				}
			}
		}
		if (targetEntity instanceof AbstractHorseEntity) {
			AbstractHorseEntity p = (AbstractHorseEntity) targetEntity;
			if (p.isTamed()) {
				if (p.getOwnerUUID().equals(serverPlayer.getUUID())) {
					teleportPetOrHorse = true;
				}
			}
		}
		
		float headPitch = serverPlayer.xRot;
		float headYaw = serverPlayer.yRot;
		// default - teleport to worldspawn.
		int wX = serverPlayer.level.getLevelData().getXSpawn();
		int wY = serverPlayer.level.getLevelData().getYSpawn();
		int wZ = serverPlayer.level.getLevelData().getZSpawn();

		BlockPos personalSpawnPos = serverPlayer.getRespawnPosition();
		float pitch = 0.4f;
		float soundVolume = 0.8f;
		if ((headPitch == -90.0) || (headPitch == 90)) {
			return false;
		}
		if (headPitch > 0) { // looking down- teleport to personal spawn instead.
			wX = personalSpawnPos.getX();
			wY = personalSpawnPos.getY()+ 1;
			wZ = personalSpawnPos.getZ();
			pitch = 0.5f;
			soundVolume = 0.6f;
		}

		ChunkPos chunkPos = new ChunkPos(new BlockPos (wX,wY,wZ));
		serverSpawnMagicalParticles(serverPlayer, serverWorld, 2, ParticleTypes.POOF);

		if (teleportPetOrHorse) {

			serverSpawnMagicalParticles(targetEntity, serverWorld, 2, ParticleTypes.POOF);

			SoundEvent petSound = SoundEvents.PORTAL_TRAVEL;
			if (targetEntity instanceof WolfEntity) {
				petSound = SoundEvents.WOLF_HOWL;
			} else if (targetEntity instanceof AbstractHorseEntity){
				petSound = SoundEvents.HORSE_ANGRY;
			} else if (targetEntity instanceof CatEntity){
				petSound = SoundEvents.CAT_AMBIENT;
			}
			serverPlayer.level.playSound(null, targetEntity.blockPosition(), petSound,
					SoundCategory.AMBIENT, soundVolume/2, pitch);
			targetEntity.setPos((double)wX, (double)wY, (double)wZ);

		} else {
			serverWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1 , serverPlayer.getId());
			serverPlayer.level.playSound(null, targetEntity.blockPosition(), SoundEvents.PORTAL_TRAVEL,
					SoundCategory.AMBIENT, soundVolume, pitch);
			serverSpawnMagicalParticles(serverPlayer, serverWorld, 2, ParticleTypes.POOF);
			serverPlayer.teleportTo(serverWorld, (double)wX, (double)wY, (double)wZ, headYaw, headPitch);
		}
		
		return true;
		
	}

	public static void drawSpellBeam (ServerPlayerEntity serverPlayer, ServerWorld serverWorld, Entity targetEntity, IParticleData spellParticleType) {
		Vector3d vector3d = serverPlayer.getEyePosition(1.0F);
		Vector3d vectorFocus = vector3d.subtract(0.0,0.2,0.0);
		Vector3d vector3d1 = serverPlayer.getViewVector(1.0F);
		vector3d.subtract(0.0,0.4,0.0);
		Vector3d target3d = targetEntity.getEyePosition(1.0F);
		double targetDistance = vector3d.distanceTo(target3d);
		boolean doSpellParticleType = true;
		for (double d0 = 0.0; d0 <= targetDistance; d0=d0+0.5D) {
			Vector3d beamPath3d2 = vectorFocus.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
			serverSpawnMagicalParticles(beamPath3d2, serverWorld, 1, RedstoneParticleData.REDSTONE); 
			doSpellParticleType = !doSpellParticleType;
			if (doSpellParticleType) {
				Vector3d swirlPath3d2 = beamPath3d2;
				swirlPath3d2.normalize();
				serverSpawnMagicalParticles(beamPath3d2, serverWorld, 1, spellParticleType); 
				doSpellParticleType = !doSpellParticleType;
			}
		}
	}

	// server dist
	public static void processSpellOnServer(int spellNumber, LivingEntity targetEntity, ServerPlayerEntity serverPlayer,
			int spellTime, int handIndex, int targetPosX, int targetPosY, int targetPosZ) {

		ItemStack correctRedstoneFocusStack = serverPlayer.getMainHandItem();
		if (handIndex == 2) {
			correctRedstoneFocusStack = serverPlayer.getOffhandItem();
		}
		CompoundNBT compoundnbt = correctRedstoneFocusStack.getOrCreateTag();
		long spellCastingStartTime = compoundnbt != null
				&& compoundnbt.contains("spellCastingStartTime", RedstoneFocusItem.NBT_NUMBER_FIELD)
						? compoundnbt.getLong("spellCastingStartTime")
						: 0;
						compoundnbt.putLong("spellCastingStartTime", RedstoneFocusItem.SPELL_NOT_CASTING);

		IMagicStorage playerManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
		if (playerManaStorage == null) {
			MyConfig.sendChat(serverPlayer, "Impossible Error: You do not have a mana pool.",
					Color.fromLegacyFormat((TextFormatting.YELLOW)));
			return;
		}

		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
		if (spellTime > 6) {
			spellTime = 6;
		}
		int spellCost = 1 + spell.getSpellBaseCost() * spellTime;
		
		if (MyConfig.getDebugLevel() > 0) {
			System.out.println(spellNumber +": SpellCost: " + spellCost + ".");
		}
		if (spellCost > playerManaStorage.getManaStored()) {
			serverPlayer.level.playSound(null, serverPlayer.blockPosition(), SoundEvents.DISPENSER_FAIL,
					SoundCategory.AMBIENT, 0.7f, 0.8f);
			serverPlayer.level.playSound(null, serverPlayer.blockPosition(), SoundEvents.DISPENSER_FAIL,
					SoundCategory.NEUTRAL, 0.6f, 0.2f);
			serverPlayer.level.playSound(null, serverPlayer.blockPosition(), ModSounds.SPELL_FAILS,
					SoundCategory.BLOCKS, 0.8f, 0.4f);
			serverSpawnMagicalParticles(serverPlayer, serverPlayer.getLevel(), 2, ParticleTypes.POOF);
			MyConfig.sendChat(serverPlayer, "You do not have enough mana.",
					Color.fromLegacyFormat((TextFormatting.RED)));
			return;
		}

		total_calls = total_calls + 1;
		if (spellTime > 4) spellTime = 4;
		
		BlockPos targetPos = null;
		if (targetPosY != -99999 ) {
			targetPos = new BlockPos (targetPosX, targetPosY, targetPosZ);
		}
		
		if (castSpellAtTarget(serverPlayer, targetEntity, spellTime, spell, targetPos)) {
			serverPlayer.getLevel().playSound(null, serverPlayer.blockPosition(),
					SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.AMBIENT, 0.4f, 0.9f);
			Chunk playerChunk = (Chunk) serverPlayer.level.getChunk(serverPlayer.blockPosition());
			// possible bug here.  getting from player not chunk
			IMagicStorage chunkManaStorage = playerChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			int chunkMana = chunkManaStorage.getManaStored();
			int chunkManaUsed = 0;
			int personalManaUsed = spellCost;
			if (spellCost > 1) {
				if (chunkManaStorage.useMana(1)) {
					personalManaUsed = spellCost-1;
				}
			}
			playerManaStorage.useMana(personalManaUsed);
			
			if (MyConfig.getDebugLevel() > 0) {
				System.out.println(serverPlayer.getName().toString() +"cast spell " + spell + " using mana: " + personalManaUsed);
			}
			Network.sendToClient(new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE),
					serverPlayer);
		} else {
			drawSpellBeam(serverPlayer, serverPlayer.getLevel(), targetEntity, ParticleTypes.POOF);
			serverPlayer.getLevel().playSound(null, serverPlayer.blockPosition(),
					ModSounds.SPELL_FAILS, SoundCategory.AMBIENT, 0.4f, 0.25f);
		}

	}

	public static void serverSpawnMagicalParticles(Entity entity, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

          double xOffset = 0.75D;
          double yOffset = 0.3D;
          double zOffset = 0.75D;
          particleCount *= 3;
          serverWorld.sendParticles(particleType, entity.getX(), entity.getY()+(double)entity.getEyeHeight(), entity.getZ(), particleCount, xOffset, yOffset, zOffset, -0.04D);
    }
	
	public static void serverSpawnMagicalParticles(BlockPos targetPos, ServerWorld serverWorld, int particleCount,
			IParticleData particleType) {
        double xOffset = 0.5D;
        double yOffset = 0.5D;
        double zOffset = 0.5D;
        particleCount *= 3;
        serverWorld.sendParticles(particleType, targetPos.getX(), targetPos.getY(), targetPos.getZ(), particleCount, xOffset, yOffset, zOffset, -0.14D);
		
	}

	
    public static void serverSpawnMagicalParticles(Vector3d bV3D, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

        double xOffset = 0.25D * (Math.sin(bV3D.x()));
        double yOffset = 0.15D * (Math.cos(bV3D.y())); 
        double zOffset = 0.25D * (Math.cos(bV3D.z()));
//        double xOffset = 0.25D ;
//        double yOffset = 0.15D ;
//        double zOffset = 0.25D ;
        particleCount *= 2;
        serverWorld.sendParticles(particleType, bV3D.x(), bV3D.y(), bV3D.z(), particleCount, xOffset, yOffset, zOffset, -0.04D);
  }


}
