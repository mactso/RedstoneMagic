package com.mactso.redstonemagic.events;

import java.util.Collection;

import com.mactso.redstonemagic.config.MyConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GolemDropsEvent {

		public static long tickTimer = 0;
		
		@SubscribeEvent
	    public void handleGolemDropsEvent(LivingDropsEvent event) { 
			
			Entity eventEntity = event.getEntityLiving();
			if (event.getEntity() == null) {
				return;
			}

			
			if (!(eventEntity.world instanceof ServerWorld)) {
				return;
			}
			
			if (eventEntity instanceof IronGolemEntity) {
				Collection<ItemEntity> eventItems = event.getDrops();
				eventItems.removeIf((itemEntity) -> {return itemEntity.getItem().getItem() == Items.IRON_INGOT;});
				
				long worldTime = eventEntity.world.getGameTime();
				if (tickTimer > worldTime) {
					if (MyConfig.debugLevel > 0) {
						System.out.println("Poor Golems: A Golem Died at: "
								+ (int) eventEntity.getPosX() + ", "
								+ (int) eventEntity.getPosY() + ", "
								+ (int) eventEntity.getPosZ() + ", "
								+"and dropped no iron.");
					}	
					return;
				}

				tickTimer = worldTime + (long) (MyConfig.secondsBetweenIronDrops * 20);

				int rollRange = MyConfig.MaxIronDropAmount - MyConfig.MinIronDropAmount;
				if (rollRange < 0) {
					rollRange = 0;
				}
				int randomLootRoll = (int) (Math.ceil(eventEntity.world.rand.nextDouble() * rollRange ) + MyConfig.MinIronDropAmount);
		        ItemStack itemStackToDrop = new ItemStack(Items.IRON_INGOT, (int) randomLootRoll );
				ItemEntity myItemEntity = new ItemEntity (eventEntity.world, eventEntity.getPosX(),eventEntity.getPosY(),eventEntity.getPosZ(),itemStackToDrop);
				eventItems.add(myItemEntity);

				if (MyConfig.debugLevel > 0) {
					System.out.println("Poor Golems: A Golem Died at: "
							+ (int) eventEntity.getPosX() + ", "
							+ (int) eventEntity.getPosY() + ", "
							+ (int) eventEntity.getPosZ() + ", "
							+"and dropped "+ randomLootRoll +" iron.");
				}	

				int debugline = 3;
			}
		}
	 
}
