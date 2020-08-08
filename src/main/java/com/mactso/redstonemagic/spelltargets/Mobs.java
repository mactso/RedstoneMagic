package com.mactso.redstonemagic.spelltargets;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Mobs
{
//	private static final Logger LOGGER = LogManager.getLogger();
////	public static boolean move_state = true;
////	public static boolean look_state = true;
//	public static HashSet<LivingEntity> mobs = new HashSet<>();
//	public static final AttributeModifier FREEZE_ATTR = new AttributeModifier(UUID.fromString("8d2c8d25-7a8c-4fbc-be91-8dba276ebbe0"), "freeze", -1.0, Operation.MULTIPLY_TOTAL);

	//server dist
	public static void process(int spellNumber, LivingEntity entity, ServerPlayerEntity serverPlayer)
	{
		System.out.println("Serverside Spell :" + spellNumber + " at " + entity.getName().toString() + ".");
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
