package com.mactso.redstonemagic.spells;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
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

public class CastSpells {
	static final int FOUR_SECONDS = 80;
	static final int THIRTY_SECONDS = 600;
	static final int QUICK_INTENSITY = 20;
	static final int FULL_INTENSITY = 80;
	static double posX = 0;
	static double posY = 0;
	static double posZ = 0;
	static ItemStack MILK_STACK = new ItemStack (Items.MILK_BUCKET);
	static int total_calls = 0; 
	// server dist
	public static void processSpellForServer(int spellNumber, LivingEntity entity, ServerPlayerEntity serverPlayer,
			int costFactor) {
		String spellKey = Integer.toString(spellNumber);
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(spellKey);
		total_calls = total_calls + 1;
		if (castASpell(spell, entity, serverPlayer, costFactor)) {
			MyConfig.dbgPrintln("Serverside Spell :" + spell.getSpellComment() + " at " + entity.getName().toString() + " calls:" + total_calls + ".");
		} else {
			serverPlayer.getServerWorld().playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.AMBIENT, 0.9f, 0.25f);
			MyConfig.dbgPrintln("Serverside Spell :" + spell.getSpellComment() + ".  failed. calls:" + total_calls  +"." );
		}

	}

	public static boolean castASpell(RedstoneMagicSpellItem spell, LivingEntity targetEntity,
			ServerPlayerEntity serverPlayerEntity, int costFactor) {

		// @TODO this section will be replaced with intensity based on #ticks holding
		// spell ready (max 80).

		int spellIntensity = QUICK_INTENSITY;

		int spellCost = spell.getSpellBaseCost() * costFactor;

		String spellTargetType = spell.getSpellTargetType();

		String spellTranslationKey = spell.getSpellTranslationKey();


		if (spellTargetType.equals("S")) {
			serverPlayerEntity.getServerWorld().playSound(null, serverPlayerEntity.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.5f, 0.8f);
		}

		LazyOptional<IMagicStorage> optPlayer = serverPlayerEntity.getCapability(CapabilityMagic.MAGIC);
		IMagicStorage playerManaStorage = optPlayer.orElseGet(null);
		if (optPlayer.isPresent()) {

			int currentMana = playerManaStorage.getManaStored(); // checks for max capacity internally based on object type.
			if (spellCost > currentMana) {
				serverPlayerEntity.world.playSound(serverPlayerEntity, serverPlayerEntity.getPosition(), SoundEvents.BLOCK_BASALT_FALL,
						SoundCategory.AMBIENT, 0.5f, 0.8f);
				MyConfig.sendChat(serverPlayerEntity, "You don't have enough mana to finish the spell.",
						Color.func_240744_a_(TextFormatting.RED));
				return false;
			}
		} else {
			return false;
		}

		if (castSpellAtTarget(spell, targetEntity, spellCost, serverPlayerEntity)) {
			playerManaStorage.useMana(spellCost);
			int chunkMana = -1; // TODO get chunk mana here and use 1 mana from chunk
			int playerMana = playerManaStorage.getManaStored();
			Network.sendToClient(new SyncClientManaPacket(playerMana, chunkMana), serverPlayerEntity);			
			MyConfig.sendChat(serverPlayerEntity, "You have " + playerManaStorage.getManaStored() + "mana left.",
					Color.func_240744_a_(TextFormatting.RED));
			return true;
		} else {
			MyConfig.sendChat(serverPlayerEntity, "You have " + playerManaStorage.getManaStored() + "mana left.",
					Color.func_240744_a_(TextFormatting.RED));
		}

		return false;
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

	public static void drawSpellBeam (ServerPlayerEntity serverPlayer, ServerWorld serverWorld, Entity targetEntity) {
		Vector3d vector3d = serverPlayer.getEyePosition(1.0F);
		Vector3d vectorFocus = vector3d.subtract(0.0,0.4,0.0);
		Vector3d vector3d1 = serverPlayer.getLook(1.0F);
		vector3d.subtract(0.0,0.4,0.0);
		Vector3d target3d = targetEntity.getEyePosition(1.0F);
		double targetDistance = vector3d.distanceTo(target3d);
		for (double d0 = 1.0; d0 < targetDistance; d0=d0+0.5D) {
			Vector3d beamPath3d2 = vectorFocus.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
			serverSpawnMagicalParticles(beamPath3d2, serverWorld, 1, RedstoneParticleData.REDSTONE_DUST); 
		}
	}	
	
	public static LivingEntity longTarget(PlayerEntity clientPlayer) {
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

	private static boolean castSpellAtTarget(RedstoneMagicSpellItem spell, LivingEntity targetEntity, int spellCost,
			ServerPlayerEntity serverPlayer) {
		
		String spellTranslationKey = spell.getSpellTranslationKey();
		String spellTargetType = spell.getSpellTargetType();
		DamageSource myDamageSource = DamageSource.causePlayerDamage(serverPlayer).setDamageBypassesArmor()
				.setMagicDamage();
		ServerWorld serverWorld = (ServerWorld) targetEntity.world;
	  	posX = targetEntity.getPosX();
	  	posY = targetEntity.getPosY();
	  	posZ = targetEntity.getPosZ();
//***
		if (spell.getSpellTranslationKey().equals("RM.NUKE")) {
			int damage = (int) targetEntity.getHealth() / 10;
			if (damage == 0) {
				damage = 2;
			}
			damage = damage * spellCost;
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.2f, 0.9f);
			boolean damaged = targetEntity.attackEntityFrom(myDamageSource, damage);
			if (damaged) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity);
				serverSpawnMagicalParticles(targetEntity, serverWorld, damage, RedstoneParticleData.REDSTONE_DUST); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
				serverSpawnMagicalParticles(targetEntity, serverWorld, damage, ParticleTypes.DAMAGE_INDICATOR); 
				return true;
			}
			return false;
		}
//***
		if (spellTranslationKey.equals(("RM.DOT"))) {
			int effectIntensity = (int) targetEntity.getHealth() / 20;
			if (effectIntensity < 1) {
				effectIntensity = 1;
			}
			int secondsDuration = FOUR_SECONDS * spellCost;
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.POISON);
			if (ei != null) {
				if (ei.getDuration() > 10) {
					return false;
				}
				if (ei.getAmplifier() <= effectIntensity) {
					targetEntity.removeActivePotionEffect(Effects.POISON);
				}
			}
			targetEntity.attackEntityFrom(myDamageSource, 1);
			targetEntity
					.addPotionEffect(new EffectInstance(Effects.POISON, secondsDuration, effectIntensity, true, true));
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity);
			serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, 3, ParticleTypes.WITCH); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ANGRY_VILLAGER); 
			return true;
		}
//***
		if (spellTranslationKey.equals(("RM.SDOT"))) {
			int effectIntensity = (int) targetEntity.getHealth() / 20;
			if (effectIntensity < 1) {
				effectIntensity = 1;
			}
			int secondsDuration = FOUR_SECONDS * spellCost;
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.POISON);
			if (ei != null) {
				if ((ei.getDuration() < 11) || (ei.getAmplifier() <= effectIntensity)) {
					targetEntity.removeActivePotionEffect(Effects.POISON);
				}
			}
			targetEntity.attackEntityFrom(myDamageSource, 1);
			targetEntity.addPotionEffect(new EffectInstance(Effects.POISON, secondsDuration, effectIntensity, true, true));
			drawSpellBeam(serverPlayer, serverWorld, targetEntity);
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ANGRY_VILLAGER); 

			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				if ((ei.getDuration() < 11) || (ei.getAmplifier() <= effectIntensity)) {
					targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				}
			}
			targetEntity.addPotionEffect(
					new EffectInstance(Effects.SLOWNESS, secondsDuration, effectIntensity, true, true));
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.6f, 0.8f);
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.AMBIENT, 0.7f, 0.3f);
			drawSpellBeam(serverPlayer, serverWorld, targetEntity);
			serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, 6, ParticleTypes.WITCH); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ANGRY_VILLAGER); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.SMOKE); 
			return true;
		}
//***
		if (spellTranslationKey.equals("RM.BUFF")) {
			BlockPos p = targetEntity.getPosition();
			boolean hasNotCastBuff = true;
			
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.WATER_BREATHING);
			if (ei != null) {
				if ((ei.getDuration() < 9) ) {
					targetEntity.removeActivePotionEffect(Effects.WATER_BREATHING);
					ei = null;
				}
			}
			if ((ei == null) && (hasNotCastBuff)) {
				if ((serverWorld.getBlockState(p).getBlock() == Blocks.WATER) &&
						(serverWorld.getBlockState(p.up()).getBlock() == Blocks.WATER)) {
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.BUBBLE_COLUMN_UP); 
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.ENTITY_DOLPHIN_PLAY, SoundCategory.AMBIENT, 0.9f, 0.25f);
					int secondsDuration = (spellCost * THIRTY_SECONDS)/2;
					int effectIntensity = 1;
					targetEntity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, secondsDuration/2, effectIntensity, true, true));
					hasNotCastBuff = false;
				}
			}
			
			ei = targetEntity.getActivePotionEffect(Effects.NIGHT_VISION);
			if (ei != null) {
				if ((ei.getDuration() < 9) ) {
					targetEntity.removeActivePotionEffect(Effects.NIGHT_VISION);
					ei = null;
				}
			}			
			if ((ei == null) && (hasNotCastBuff)) {
				if (serverWorld.getLight(p) <= 7 ) {
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.END_ROD); 
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.ENTITY_ENDERMAN_AMBIENT, SoundCategory.AMBIENT, 0.9f, 0.25f);
					int secondsDuration = (spellCost * THIRTY_SECONDS)/3;
					int effectIntensity = 1;
					targetEntity.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, secondsDuration, effectIntensity, true, true));
					hasNotCastBuff = false;
				}
			}
			ei = targetEntity.getActivePotionEffect(Effects.FIRE_RESISTANCE);
			if (ei != null) {
				if ((ei.getDuration() < 9) ) {
					targetEntity.removeActivePotionEffect(Effects.FIRE_RESISTANCE);
					ei = null;
				}
			}			
			if ((ei == null) && (hasNotCastBuff)) {
				if (targetEntity.isBurning()) {
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.END_ROD); 
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.9f, 0.25f);
					
					int secondsDuration = (spellCost * THIRTY_SECONDS)/3;
					int effectIntensity = 1;
					targetEntity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, secondsDuration, effectIntensity, true, true));
					hasNotCastBuff = false;
				}
			}
			if (hasNotCastBuff) {
				return false;
			} else {
				return true;
			}
		}
//***
		if (spellTranslationKey.equals("RM.HEAL")) {
			int damage = (int) targetEntity.getHealth() / 10;
			if (damage < 2) {
				damage = 2 * spellCost;
			}
			if (targetEntity.isEntityUndead()) {
				boolean damaged = targetEntity.attackEntityFrom(myDamageSource, damage);
				if (damaged) {
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.9f, 0.25f);
					serverWorld.playSound(null, serverPlayer.getPosition(),
							SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.2f, 0.9f);
					drawSpellBeam(serverPlayer, serverWorld, targetEntity);
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.CAMPFIRE_COSY_SMOKE); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, damage, ParticleTypes.DAMAGE_INDICATOR); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, RedstoneParticleData.REDSTONE_DUST); 
					return true;
				} else {
					return false;
				}
			}
			if (targetEntity.isAlive()) {
				drawSpellBeam(serverPlayer, serverWorld, targetEntity);
				if (targetEntity.getHealth() < targetEntity.getMaxHealth()) {
					targetEntity.heal((float) damage);
					serverWorld.playSound(null, serverPlayer.getPosition(),
							SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.AMBIENT, 0.2f, 0.8f);
					serverWorld.playSound(null, targetEntity.getPosition(),
							SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.9f, 0.25f);
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.HEART); 
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, RedstoneParticleData.REDSTONE_DUST); 
					return true;
				} else {
					serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.POOF); 
				return false;
				}
				
			}
		}
//***
		if (spellTranslationKey.equals("RM.RESI")) {
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.RESISTANCE);
			int effectDuration = spellCost * FOUR_SECONDS;
			int effectIntensity = 0;
			if (ei != null) {
				int durationLeft = ei.getDuration();
				int currentAmplifier = ei.getAmplifier();
				if (durationLeft >= 6) {
					if (currentAmplifier > 0) {
						return false;
					}
					effectIntensity = 1;
				} 
				targetEntity.removeActivePotionEffect(Effects.RESISTANCE);
			}
			targetEntity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, effectDuration, effectIntensity, true, true));
			serverWorld.playSound(null, targetEntity.getPosition(),
					SoundEvents.ENTITY_DOLPHIN_SWIM, SoundCategory.AMBIENT, 0.9f, 0.25f);
			targetEntity
					.addPotionEffect(new EffectInstance(Effects.RESISTANCE, FOUR_SECONDS * spellCost, 0, true, true));
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.WHITE_ASH); 

			return true;
		}
//***
		if (spellTranslationKey.equals("RM.RCRS")) {
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.ENTITY_DOLPHIN_SWIM, SoundCategory.AMBIENT, 0.6f, 0.25f);
			if (!(serverPlayer.inventory.hasItemStack(MILK_STACK))) {
				MyConfig.sendChat(serverPlayer, "You have no milk in your inventory.", Color.func_240744_a_(TextFormatting.DARK_RED));
				serverWorld.playSound(null, serverPlayer.getPosition(),
						SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.8f, 0.1f);	
				return false;
			}
			// small chance to replace milk bucket with empty bucket.
			
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.6f, 0.65f);	
			serverWorld.playSound(null, serverPlayer.getPosition(),
					SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 0.6f, 0.75f);	
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.ENCHANT); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.WITCH); 
			serverSpawnMagicalParticles(targetEntity, serverWorld, spellCost, ParticleTypes.WHITE_ASH); 

			EffectInstance ei = null;
			ei = targetEntity.getActivePotionEffect(Effects.BLINDNESS);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.BLINDNESS);
				return true;
			}
			ei = targetEntity.getActivePotionEffect(Effects.WITHER);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.WITHER);
				return true;
			}
			ei = targetEntity.getActivePotionEffect(Effects.POISON);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.POISON);
				return true;
			}
			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				return true;
			}
			ei = targetEntity.getActivePotionEffect(Effects.MINING_FATIGUE);
			if (ei != null) {
				targetEntity.removeActivePotionEffect(Effects.MINING_FATIGUE);
				return true;
			}
			MyConfig.dbgPrintln("Remove Curse: remove one negative effect");

			return true;

		}
//***
		if (spellTranslationKey.equals("RM.TELE")) {
			if (spellCost < 2) {
				MyConfig.dbgPrintln("Too little casting time for teleport.");
				return false;
			}
			if (targetEntity.world.func_234923_W_() != World.field_234918_g_) {
				MyConfig.dbgPrintln("For now can only teleport in Overworld.. add end");
				return false;
			}


			
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
//			targetEntity.setLocationAndAngles((double)wX, (double)wY, (double)wZ, headYaw, headPitch); 
			return true;
		}		
		return false;
	}
	
    public static void serverSpawnMagicalParticles(Entity entity, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

          double xOffset = 0.75D;
          double yOffset = 0.3D;
          double zOffset = 0.75D;
          particleCount *= 2;
          serverWorld.spawnParticle(particleType, posX, posY+(double)entity.getEyeHeight(), posZ, particleCount, xOffset, yOffset, zOffset, -0.04D);                
  		  int debug = 2;
    }

    public static void serverSpawnMagicalParticles(Vector3d bV3D, ServerWorld serverWorld, int particleCount, IParticleData particleType) {

        double xOffset = 0.25D;
        double yOffset = 0.15D;
        double zOffset = 0.25D;
        particleCount *= 2;
        serverWorld.spawnParticle(particleType, bV3D.getX(), bV3D.getY(), bV3D.getZ(), particleCount, xOffset, yOffset, zOffset, -0.04D);                
		  int debug = 2;
  }
}
