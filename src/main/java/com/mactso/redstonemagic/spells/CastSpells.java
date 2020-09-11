package com.mactso.redstonemagic.spells;

import java.util.Collection;
import java.util.UUID;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.item.RedstoneFocusItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;

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
import net.minecraft.entity.passive.ParrotEntity;
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
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
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
	static final ItemStack GOLDEN_CARROT_STACK = new ItemStack (Items.GOLDEN_CARROT);
	static final ItemStack FIRE_CHARGE_STACK = new ItemStack (Items.FIRE_CHARGE);
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
		float damageModifierForCreature = 0.1f;
        if (targetEntity instanceof LivingEntity) {
            damageModifierForCreature = EnchantmentHelper.getModifierForCreature(serverPlayer.getHeldItemMainhand(), ((LivingEntity)targetEntity).getCreatureAttribute());
         } else {
            damageModifierForCreature = EnchantmentHelper.getModifierForCreature(serverPlayer.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
         }
  	
	  	Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier attr : d)
        {
            baseWeaponDamage = (int) attr.getAmount();
        }
		float weaponDamage = baseWeaponDamage;
		if (spellTime > 4) spellTime = 4;
//		MyConfig.sendChat(serverPlayer, "Cast A Spell before Switch.  Spell is " + spell.getSpellTranslationKey(), Color.func_240744_a_(TextFormatting.RED));

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
			return doSpellHeal(serverPlayer, targetEntity, spellTime, myDamageSource, serverWorld, weaponDamage, damageModifierForCreature);
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
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForCreature) {

		int secondsDuration = 4 * spellTime; // spellTime = # half seconds
		int nukeDamage = spellTime - 2; // 1,1,1,2
		if (nukeDamage < 1) nukeDamage = 1;
		int dotSecondsDuration = secondsDuration+1;
		int secondarySecondsDuration = secondsDuration;
		int secondaryEffectIntensity = 0;
		int totalDamage = (int) (targetEntity.getMaxHealth() * 0.7);
		int currentHealth = (int) targetEntity.getHealth();
		if (totalDamage > BOSS_MOB_LIMIT) totalDamage = BOSS_MOB_LIMIT;
		weaponDamage = weaponDamage + damageModifierForCreature;
		if (weaponDamage > totalDamage) totalDamage = (int) weaponDamage-1;
		if (totalDamage > weaponDamage * 2) totalDamage = (int) (weaponDamage * 2) - 1;
		int dotDamageToDeal = totalDamage - 1; // nuke damage bonus for full 2 seconds.
		if (totalDamage<4) totalDamage = 4;
		totalDamage = ((totalDamage+1) * spellTime)/4;
		int dotEffectIntensity = (totalDamage * 2 / secondsDuration) - 1;
		if (dotEffectIntensity < 0) dotEffectIntensity = 0;
		int nukeRemainderBonus = totalDamage*2 - ((dotEffectIntensity+1) * secondsDuration);
		nukeRemainderBonus = nukeRemainderBonus / 2;
		if (nukeRemainderBonus < 0) nukeRemainderBonus = 0;
		if (totalDamage <= secondsDuration * 2) {
			dotSecondsDuration = totalDamage+1;
			dotEffectIntensity = 0;
		}
		
		boolean damaged = false;
		damaged = targetEntity.attackEntityFrom(myDamageSource, spellTime);
		
		if ((targetEntity instanceof PlayerEntity) && (!(damaged))) {
			return false;  // PVP hack til I find server settings.  Basically if a player and not damaged by nuke, then don't apply DoT to them.
		}

		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.POISON);
		if (ei != null) {
			if (ei.getDuration() > 19) {
				return false;
			}
			if (ei.getAmplifier() <= dotEffectIntensity) {
				targetEntity.removeActivePotionEffect(Effects.POISON);
			}
		}

		targetEntity.attackEntityFrom(myDamageSource, spellTime);
		targetEntity.addPotionEffect(new EffectInstance(Effects.POISON, secondsDuration * TICKS_PER_SECOND, dotEffectIntensity, true, true));
		LivingEntity lE = (LivingEntity) targetEntity;
		lE.func_230246_e_(serverPlayer); // set attacking player (I think)
		serverWorld.playSound(null, targetEntity.getPosition(),SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		serverWorld.playSound(null, targetEntity.getPosition(),SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ITEM_SLIME);
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ANGRY_VILLAGER); 
		return true;
		
	}

	private static boolean doSpellHeal(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForCreature) {

		int damage = (int) targetEntity.getHealth() / 10;
		if (damage < 2)	damage = 2;
		damage = damage * spellTime;
		if (targetEntity.isEntityUndead()) {

			if (damage > BOSS_MOB_LIMIT)
				damage = BOSS_MOB_LIMIT;
			weaponDamage = weaponDamage + damageModifierForCreature;
			if (weaponDamage > damage)
				damage = (int) ( weaponDamage - 1.0f);

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
		// @TODO add code to remove revenge if animal healed close to max health.
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
		if (entity instanceof VillagerEntity ) {
			return true;
		}
		if (entity instanceof AnimalEntity) {
			return true;
		}
		return false;
	}
	
	
	
	
	private static boolean doSpellMultiBuff(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime, ServerWorld serverWorld) {

		BlockPos pos = targetEntity.getPosition();

		if (hasFalderal(serverPlayer, FEATHER_STACK)) {
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.SLOW_FALLING);
			if (ei != null) {
				if ((ei.getDuration() < 10) ) {
					targetEntity.removeActivePotionEffect(Effects.SLOW_FALLING);
					ei = null;
				}
			}
			if (ei == null) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.SPLASH);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.SOUL_FIRE_FLAME); 
				serverWorld.playSound(null, targetEntity.getPosition(),
						SoundEvents.ENTITY_CHICKEN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
				if (targetEntity.getAir() < targetEntity.getMaxAir()) targetEntity.setAir(targetEntity.getAir()+1);
				int secondsDuration = (spellTime * THIRTY_SECONDS/2);
				int effectIntensity = 1;
				targetEntity.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, secondsDuration/2, effectIntensity, true, true));
				return true;
			}
			
		}
		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.WATER_BREATHING);
		if (ei != null) {
			if ((ei.getDuration() < 10) ) {
				targetEntity.removeActivePotionEffect(Effects.WATER_BREATHING);
				ei = null;
			}
		}
		if (ei == null) {
			if (isUnderwater(serverWorld, pos) ||
					(hasFalderal(serverPlayer, PUFFERFISH_STACK))){
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.BUBBLE);				
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.BUBBLE_COLUMN_UP); 
				serverWorld.playSound(null, targetEntity.getPosition(),
						SoundEvents.ENTITY_DOLPHIN_PLAY, SoundCategory.AMBIENT, 0.9f, 0.25f);
				if (targetEntity.getAir() < targetEntity.getMaxAir()) targetEntity.setAir(targetEntity.getAir()+1);
				int falderalBoost = 0;
				if (hasFalderal(serverPlayer, new ItemStack (Items.PUFFERFISH))) {
					falderalBoost = 8;
				}
				int secondsDuration = (spellTime * (THIRTY_SECONDS+falderalBoost));
				int effectIntensity = 1;
				targetEntity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, secondsDuration, effectIntensity, true, true));
				return true;
			}
		}
		
		World w = (World) serverWorld;
		RegistryKey rK = w.func_234922_V_();
		ResourceLocation rL1 = rK.func_240901_a_();
		ResourceLocation rl2 = rK.getRegistryName();
		if ((rL1.getPath().equals("the_nether")) || (rL1.getPath().equals("the_end"))) {
		  // don't give night vision in the nether or the end.
		  // @TODO add a configurable list.
		} else { // .equals("overworld")
			ei = targetEntity.getActivePotionEffect(Effects.NIGHT_VISION);
			if (ei != null) {
				if ((ei.getDuration() < 10) ) {
					targetEntity.removeActivePotionEffect(Effects.NIGHT_VISION);
					ei = null;
				}
			}			
			if (ei == null) {
				if (serverWorld.getLight(pos) <= 8 ) {
					drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.END_ROD);		
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.END_ROD); 
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.ENTITY_ENDERMAN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
					int falderalBoost = 0;
					if (hasFalderal(serverPlayer, GOLDEN_CARROT_STACK)) {
						falderalBoost = 8;
					}
					int secondsDuration = (spellTime * (THIRTY_SECONDS+falderalBoost));
					int effectIntensity = 1;
					targetEntity.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, secondsDuration, effectIntensity, true, true));
					return true;
				}
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

	private static boolean isUnderwater(ServerWorld serverWorld, BlockPos p) {
		return (serverWorld.getBlockState(p).getBlock() == Blocks.WATER) && (serverWorld.getBlockState(p.up()).getBlock() == Blocks.WATER);
	}

	private static boolean doSpellNuke(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForCreature) {

		// can't nuke pets that are not made at you.  But they can block casting at a target.
		if ((targetEntity instanceof TameableEntity)) {
			TameableEntity t = (TameableEntity) targetEntity;
			if (t.isTamed()) {
				if ((t.getRevengeTarget() != (LivingEntity) serverPlayer)) {
					return false;
				}
			}
		}
		
		float damage = targetEntity.getMaxHealth() * 0.4f;
		damage = damage * spellTime / 4;
		if (damage > BOSS_MOB_LIMIT) damage = BOSS_MOB_LIMIT;
		if (damage > weaponDamage * 2) damage = weaponDamage * 2-1;
		if (weaponDamage > damage) damage = weaponDamage - 1;
		if (spellTime == 4) damage = weaponDamage + 1;
		damage = damage + damageModifierForCreature;
		
		if (hasFalderal(serverPlayer, FIRE_CHARGE_STACK)) {
			if (!(targetEntity.isImmuneToFire())) {
				LivingEntity lE = (LivingEntity) targetEntity;
				lE.func_230246_e_(serverPlayer); // set attacking player (I think)
				float targetHealth = targetEntity.getHealth();
				if (damage > 3.0f) {
					damage = damage - 2.0f;
				}
				targetEntity.setHealth(targetHealth - (float) damage);
				targetEntity.setFire(3);
				serverWorld.playSound(null, serverPlayer.getPosition(),SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.5f, 0.5f);
				serverWorld.playSound(null, targetEntity.getPosition(),SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.2f, 0.4f);
				drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.SOUL_FIRE_FLAME);
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int)damage, RedstoneParticleData.REDSTONE_DUST); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int)spellTime, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, (int)damage, ParticleTypes.DAMAGE_INDICATOR); 
				return true;
			} else {
				targetEntity.setFire(2);
				LivingEntity lE = (LivingEntity) targetEntity;
				lE.func_230246_e_(serverPlayer); // set attacking player (I think)
				return false;
			}
		} else  // magic damage
		if (targetEntity.attackEntityFrom(myDamageSource, damage)) {
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.5f, 0.5f);
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.2f, 0.4f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity, RedstoneParticleData.REDSTONE_DUST);
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int)damage, RedstoneParticleData.REDSTONE_DUST); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, RedstoneParticleData.REDSTONE_DUST); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, (int)damage, ParticleTypes.DAMAGE_INDICATOR); 
			return true;
		}
		return false;
	}

	public static boolean hasFalderal (ServerPlayerEntity serverPlayer, ItemStack falderalStack) {
		int slot = serverPlayer.inventory.findSlotMatchingUnusedItem(falderalStack);
		if ((slot >0 ) && (slot< 8)) {
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
				targetEntity.addPotionEffect(new EffectInstance(Effects.WITHER, 1, 0, true, true));
				curseRemoved = true;
			}
		}
		if (!(curseRemoved)) {
			ei = targetEntity.getActivePotionEffect(Effects.POISON);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.POISON);
				targetEntity.addPotionEffect(new EffectInstance(Effects.POISON, 1, 0, true, true));
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
					SoundEvents.BLOCK_BELL_USE, SoundCategory.AMBIENT, 0.9f, 0.25f);
			
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
			DamageSource myDamageSource, ServerWorld serverWorld, float weaponDamage, float damageModifierForCreature) {

		int secondsDuration = 4 * spellTime; // spellTime = # half seconds
		int nukeDamage = spellTime - 2; // 1,1,1,2
		if (nukeDamage < 1) nukeDamage = 1;
		int dotSecondsDuration = secondsDuration+1;
		int snareSecondsDuration = secondsDuration;
		int snareEffectIntensity = 0;
		int totalDamage = (int) (targetEntity.getMaxHealth() * 0.6);
		int currentHealth = (int) targetEntity.getHealth();
		if (totalDamage > BOSS_MOB_LIMIT) totalDamage = BOSS_MOB_LIMIT;
		weaponDamage = weaponDamage + damageModifierForCreature;
		if (weaponDamage > totalDamage) totalDamage = (int) (weaponDamage-1.0f);
		if (totalDamage > weaponDamage * 2) totalDamage = (int) ((weaponDamage * 2) - 1.0f);
		int dotDamageToDeal = totalDamage - 1; // nuke damage bonus for full 2 seconds.
		if (totalDamage<4) totalDamage = 4;
		totalDamage = ((totalDamage+1) * spellTime)/4;
		int dotEffectIntensity = (totalDamage * 2 / secondsDuration) - 1;
		if (dotEffectIntensity < 0) dotEffectIntensity = 0;
		int nukeRemainderBonus = totalDamage*2 - ((dotEffectIntensity+1) * secondsDuration);
		nukeRemainderBonus = nukeRemainderBonus / 2;
		if (nukeRemainderBonus < 0) nukeRemainderBonus = 0;
		if (totalDamage <= secondsDuration * 2) {
			dotSecondsDuration = totalDamage+1;
			dotEffectIntensity = 0;
		}
		
		boolean damage = false;
		damage = targetEntity.attackEntityFrom(myDamageSource, nukeDamage + nukeRemainderBonus);
		
		if ((targetEntity instanceof PlayerEntity) && (!(damage))) {
			return false;  // PVP hack til I find server settings.  Basically if a player and not damaged by nuke, then don't apply DoT to them.
		}
		
		EffectInstance ei = targetEntity.getActivePotionEffect(Effects.WITHER);
		if (ei != null) {
			if ((ei.getDuration() < 19) || (ei.getAmplifier() <= dotEffectIntensity)) {
				targetEntity.removeActivePotionEffect(Effects.WITHER);
			}
		}
		targetEntity.addPotionEffect(new EffectInstance(Effects.WITHER, dotSecondsDuration * TICKS_PER_SECOND, dotEffectIntensity, true, true));
		LivingEntity lE = (LivingEntity) targetEntity;
		lE.func_230246_e_(serverPlayer); // set attacking player (I think)
		
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_STAINED_GLASS.getDefaultState()));
//		drawSpellBeam(serverPlayer, serverWorld, targetEntity, ParticleTypes.ASH);
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, ParticleTypes.ASH); 
//		((ServerWorld)this.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getPosX(), this.getPosYHeight(0.6666666666666666D), this.getPosZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05D);
		// mobs with over 140hp are not affected by snare
		if (targetEntity.getHealth() < BOSS_MOB_LIMIT * 4) {
			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				if ((ei.getDuration() < 19) || (ei.getAmplifier() <= snareEffectIntensity)) {
					targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				}
			}
			targetEntity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, snareSecondsDuration * TICKS_PER_SECOND, 0, true, true));
		}
		serverWorld.playSound(null, targetEntity.getPosition(),	SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
		serverWorld.playSound(null, targetEntity.getPosition(),	SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
		drawSpellBeam(serverPlayer, serverWorld, targetEntity, new BlockParticleData(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()));
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.WITCH); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, new BlockParticleData(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState())); 
		serverSpawnMagicalParticles(targetEntity, serverWorld, spellTime, new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_STAINED_GLASS.getDefaultState())); 
		return true;
	}

	private static boolean doSpellTeleport(ServerPlayerEntity serverPlayer, LivingEntity targetEntity, int spellTime,
			ServerWorld serverWorld) {

		if (serverPlayer.world.func_234923_W_() != World.field_234918_g_) {
			MyConfig.sendChat(serverPlayer,"You can only teleport in the Overworld.",Color.func_240744_a_(TextFormatting.YELLOW));
			return false;
		}
		
		boolean teleportPetOrHorse = false;
		if (targetEntity instanceof TameableEntity) {
			TameableEntity p = (TameableEntity) targetEntity;
			if (p.isTamed()) {
				if (p.getOwner() == serverPlayer) {
					teleportPetOrHorse = true;
				}
			}
		}
		if (targetEntity instanceof AbstractHorseEntity) {
			AbstractHorseEntity p = (AbstractHorseEntity) targetEntity;
			if (p.isTame()) {
				if (p.getOwnerUniqueId().equals(serverPlayer.getUniqueID())) {
					teleportPetOrHorse = true;
				}
			}
		}
		
		// TODO Check for configurable reagent.
		float headPitch = serverPlayer.rotationPitch;
		float headYaw = serverPlayer.rotationYaw;
		// default - teleport to worldspawn.
		int wX = serverPlayer.world.getWorldInfo().getSpawnX();
		int wY = serverPlayer.world.getWorldInfo().getSpawnY();
		int wZ = serverPlayer.world.getWorldInfo().getSpawnZ();

		BlockPos personalSpawnPos = serverPlayer.func_241140_K_();
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

			SoundEvent petSound = SoundEvents.BLOCK_PORTAL_TRAVEL;
			if (targetEntity instanceof WolfEntity) {
				petSound = SoundEvents.ENTITY_WOLF_HOWL;
			} else if (targetEntity instanceof AbstractHorseEntity){
				petSound = SoundEvents.ENTITY_HORSE_ANGRY;
			} else if (targetEntity instanceof CatEntity){
				petSound = SoundEvents.ENTITY_CAT_AMBIENT;
			}
			serverPlayer.world.playSound(null, targetEntity.getPosition(), petSound,
					SoundCategory.AMBIENT, soundVolume/2, pitch);
			targetEntity.setPosition((double)wX, (double)wY, (double)wZ);

		} else {
			serverWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1 , serverPlayer.getEntityId());
			serverPlayer.world.playSound(null, targetEntity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL,
					SoundCategory.AMBIENT, soundVolume, pitch);
			serverSpawnMagicalParticles(serverPlayer, serverWorld, 2, ParticleTypes.POOF);
			serverPlayer.teleport(serverWorld, (double)wX, (double)wY, (double)wZ, headYaw, headPitch);
		}
		
		return true;
		
	}

	public static void drawSpellBeam (ServerPlayerEntity serverPlayer, ServerWorld serverWorld, Entity targetEntity, IParticleData spellParticleType) {
		Vector3d vector3d = serverPlayer.getEyePosition(1.0F);
		Vector3d vectorFocus = vector3d.subtract(0.0,0.2,0.0);
		Vector3d vector3d1 = serverPlayer.getLook(1.0F);
		vector3d.subtract(0.0,0.4,0.0);
		Vector3d target3d = targetEntity.getEyePosition(1.0F);
		double targetDistance = vector3d.distanceTo(target3d);
		boolean doSpellParticleType = true;
		for (double d0 = 0.0; d0 <= targetDistance; d0=d0+0.5D) {
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

	// server dist
	public static void processSpellOnServer(int spellNumber, LivingEntity targetEntity, ServerPlayerEntity serverPlayer,
			int spellTime, int handIndex) {

		ItemStack correctRedstoneFocusStack = serverPlayer.getHeldItemMainhand();
		if (handIndex == 2) {
			correctRedstoneFocusStack = serverPlayer.getHeldItemOffhand();
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
					Color.func_240744_a_(TextFormatting.YELLOW));
			return;
		}
		int debug = 1;
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
		if (spellTime > 6) {
			spellTime = 6;
		}
		int spellCost = 1 + spell.getSpellBaseCost() * spellTime;
		
		if (MyConfig.getDebugLevel() > 0) {
			System.out.println(spellNumber +": SpellCost: " + spellCost + ".");
		}
		if (spellCost > playerManaStorage.getManaStored()) {
			serverPlayer.world.playSound(null, serverPlayer.getPosition(), SoundEvents.BLOCK_DISPENSER_FAIL,
					SoundCategory.AMBIENT, 0.7f, 0.8f);
			serverPlayer.world.playSound(null, serverPlayer.getPosition(), SoundEvents.BLOCK_DISPENSER_FAIL,
					SoundCategory.NEUTRAL, 0.6f, 0.2f);
			serverPlayer.world.playSound(null, serverPlayer.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO,
					SoundCategory.BLOCKS, 0.8f, 0.4f);
			serverSpawnMagicalParticles(serverPlayer, serverPlayer.getServerWorld(), 2, ParticleTypes.POOF);
			MyConfig.sendChat(serverPlayer, "You do not have enough mana.",
					Color.func_240744_a_(TextFormatting.RED));
			return;
		}

		total_calls = total_calls + 1;
		if (spellTime > 4) spellTime = 4;

		if (castSpellAtTarget(serverPlayer, targetEntity, spellTime, spell)) {
			serverPlayer.getServerWorld().playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.AMBIENT, 0.4f, 0.9f);
			IChunk playerChunk = serverPlayer.world.getChunk(serverPlayer.getPosition());
			IMagicStorage chunkManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
			int chunkMana = chunkManaStorage.getManaStored();
			int chunkManaUsed = 0;
			int personalManaUsed = spellCost;
			if (spellCost > 1) {
				if (chunkManaStorage.useMana(1)) {
					personalManaUsed = spellCost-1;
				}
			}
			playerManaStorage.useMana(personalManaUsed);

			Network.sendToClient(new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE),
					serverPlayer);
		} else {
			drawSpellBeam(serverPlayer, serverPlayer.getServerWorld(), targetEntity, ParticleTypes.POOF);
			serverPlayer.getServerWorld().playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.AMBIENT, 0.4f, 0.25f);
		}

	}

	public static void serverSpawnMagicalParticles(Entity entity, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

          double xOffset = 0.75D;
          double yOffset = 0.3D;
          double zOffset = 0.75D;
          particleCount *= 3;
          serverWorld.spawnParticle(particleType, entity.getPosX(), entity.getPosY()+(double)entity.getEyeHeight(), entity.getPosZ(), particleCount, xOffset, yOffset, zOffset, -0.04D);                
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


}
