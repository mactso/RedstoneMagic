package com.mactso.redstonemagic.spelltargets;

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
import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.entity.projectile.ProjectileHelper;
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
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.LazyOptional;
import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;

public class Mobs {
//	private static final Logger LOGGER = LogManager.getLogger();
////	public static boolean move_state = true;
////	public static boolean look_state = true;
//	public static HashSet<LivingEntity> mobs = new HashSet<>();
//	public static final AttributeModifier FREEZE_ATTR = new AttributeModifier(UUID.fromString("8d2c8d25-7a8c-4fbc-be91-8dba276ebbe0"), "freeze", -1.0, Operation.MULTIPLY_TOTAL);
	static final int FOUR_SECONDS = 80;
	static final int THIRTY_SECONDS = 600;
	static final int QUICK_INTENSITY = 20;
	static final int FULL_INTENSITY = 80;

	// server dist
	public static void processCastSpells(int spellNumber, LivingEntity entity, ServerPlayerEntity serverPlayer,
			int costFactor) {
		String spellKey = Integer.toString(spellNumber);
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(spellKey);
		if (castASpell(spell, entity, serverPlayer, costFactor)) {
			System.out.println(
					"Serverside Spell :" + spell.getSpellComment() + " at " + entity.getName().toString() + ".");
		} else {
			System.out.println("Serverside Spell :" + spell.getSpellComment() + ".  failed.");
		}

	}

	public static boolean castASpell(RedstoneMagicSpellItem spell, LivingEntity targetEntity,
			ServerPlayerEntity serverPlayer, int costFactor) {

		// @TODO this section will be replaced with intensity based on #ticks holding
		// spell ready (max 80).

		int spellIntensity = QUICK_INTENSITY;

		int spellCost = spell.getSpellBaseCost() * costFactor;

		String spellTargetType = spell.getSpellTargetType();

		String spellTranslationKey = spell.getSpellTranslationKey();
		if (spellTargetType.equals(("T"))) {
			serverPlayer.world.playSound(serverPlayer, serverPlayer.getPosition(),
					SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 0.4f, 0.3f);
		}
		if (spellTargetType.equals("B")) {
			serverPlayer.world.playSound(serverPlayer, serverPlayer.getPosition(),
					SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.AMBIENT, 0.4f, 0.3f);

		}
		if (spellTargetType.equals("S")) {
			serverPlayer.world.playSound(serverPlayer, serverPlayer.getPosition(),
					SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.5f, 0.8f);
		}

		LazyOptional<IMagicStorage> optPlayer = serverPlayer.getCapability(CapabilityMagic.MAGIC);
		IMagicStorage cap = optPlayer.orElseGet(null);
		if (optPlayer.isPresent()) {

			int currentMana = cap.getManaStored(); // checks for max capacity internally based on object type.
			if (spellCost > currentMana) {
				serverPlayer.world.playSound(serverPlayer, serverPlayer.getPosition(), SoundEvents.BLOCK_BASALT_FALL,
						SoundCategory.AMBIENT, 0.5f, 0.8f);
				MyConfig.sendChat(serverPlayer, "You don't have enough mana to finish the spell.",
						Color.func_240744_a_(TextFormatting.RED));
			}
		} else {
			return false;
		}

		if (castSpellAtTarget(spell, targetEntity, spellCost, serverPlayer)) {
			cap.useMana(spellCost);
			MyConfig.sendChat(serverPlayer, "You have " + cap.getManaStored() + "mana left.",
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
			ServerPlayerEntity serverPlayerEntity) {
		String spellTranslationKey = spell.getSpellTranslationKey();
		String spellTargetType = spell.getSpellTargetType();
		DamageSource myDamageSource = DamageSource.causePlayerDamage(serverPlayerEntity).setDamageBypassesArmor()
				.setMagicDamage();

		if (spell.getSpellTranslationKey().equals("RM.DMG")) {
			int damage = (int) targetEntity.getHealth() / 10;
			if (damage < 2) {
				damage = 2 * spellCost;
			}
			return (targetEntity.attackEntityFrom(myDamageSource, damage));
		}

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
			return true;
		}

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
			targetEntity
					.addPotionEffect(new EffectInstance(Effects.POISON, secondsDuration, effectIntensity, true, true));
			ei = targetEntity.getActivePotionEffect(Effects.SLOWNESS);
			if (ei != null) {
				if ((ei.getDuration() < 11) || (ei.getAmplifier() <= effectIntensity)) {
					targetEntity.removeActivePotionEffect(Effects.SLOWNESS);
				}
			}
			targetEntity.addPotionEffect(
					new EffectInstance(Effects.SLOWNESS, secondsDuration, effectIntensity, true, true));
			return true;
		}

		if (spellTranslationKey.equals("RM.HEAL")) {
			int damage = (int) targetEntity.getHealth() / 10;
			if (damage < 2) {
				damage = 2 * spellCost;
			}
			if (targetEntity.isEntityUndead()) {
				return (targetEntity.attackEntityFrom(myDamageSource, damage));
			}
			if (targetEntity.isAlive()) {
				targetEntity.heal((float) damage);
				return true;
			}
			return false;
		}

		if (spellTranslationKey.equals("RM.RESI")) {
			EffectInstance ei = targetEntity.getActivePotionEffect(Effects.RESISTANCE);
			if (ei != null) {
				if (ei.getDuration() > 10) {
					return false;
				}
				if (ei.getAmplifier() <= 1) {
					targetEntity.removeActivePotionEffect(Effects.RESISTANCE);
				}
			}
			targetEntity
					.addPotionEffect(new EffectInstance(Effects.RESISTANCE, FOUR_SECONDS * spellCost, 1, true, true));
			return true;
		}

		if (spellTranslationKey.equals("RM.RCRS")) {
			// @TODO if milk in inventory then
			if (MyConfig.debugLevel > 0) {
				System.out.println("Remove Curse: remove one negative effect");
			}
			return true;

		}

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
			ServerWorld serverWorld = (ServerWorld) serverTargetPlayer.world;
			serverWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1 , serverTargetPlayer.getEntityId());
			targetEntity.world.playSound(null, targetEntity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL,
					SoundCategory.AMBIENT, soundVolume, pitch);
			targetEntity.setLocationAndAngles((double)wX, (double)wY, (double)wZ, headYaw, headPitch); 
			return true;
		}		
		return false;
	}
}
