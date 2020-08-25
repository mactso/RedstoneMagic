package com.mactso.redstonemagic.item;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.network.SyncClientGuiPacket;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.spells.CastSpells;
import com.mactso.redstonemagic.util.helpers.KeyboardHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneFocusItem extends ShieldItem {

	public final static int NBT_NUMBER_FIELD = 99;
	public final static long SPELL_NOT_CASTING = -1;

	@OnlyIn(value=Dist.CLIENT)
	public static LivingEntity doLookForDistantTarget(PlayerEntity clientPlayer) {
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
 
	public static LivingEntity target(PlayerEntity player) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.objectMouseOver.getType() == Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
			if (entity instanceof LivingEntity)
				return (LivingEntity) entity;
		}
		return null;
	}

	public RedstoneFocusItem(Properties builder) {
		super(builder);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		// TODO Auto-generated method stub
		CompoundNBT compoundnbt = stack.getOrCreateTag();
		int spellNumberKey = compoundnbt != null && compoundnbt.contains("spellKeyNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("spellKeyNumber")
				: 0;
		SpellManager.RedstoneMagicSpellItem spell = SpellManager
				.getRedstoneMagicSpellItem(Integer.toString(spellNumberKey));
		tooltip.add(new StringTextComponent("Spell Name : " + spell.getSpellComment()));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}


	private boolean canUseRedstoneFocusItem(PlayerEntity playerIn) {
		int baseWeaponDamage = 0;
		boolean canUseRedstoneFocus = false;
		ItemStack handItem = playerIn.getHeldItemMainhand();
		if (handItem.getItem() instanceof RedstoneFocusItem) {
			canUseRedstoneFocus = true;
		}
		if (handItem != null) {
			Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND)
					.get(Attributes.ATTACK_DAMAGE);
			while ((d.iterator().hasNext()) && (baseWeaponDamage == 0)) {
				baseWeaponDamage = (int) d.iterator().next().getAmount();
				if (baseWeaponDamage > 1) {
					canUseRedstoneFocus = true;
				}
			}
		} else {
			canUseRedstoneFocus = true;
		}
		return canUseRedstoneFocus;
	}


//	CompoundNBT compoundnbt = stack.getOrCreateTag();
//	int spellState = compoundnbt != null && compoundnbt.contains("spellState", NBT_NUMBER_FIELD)
//			? compoundnbt.getInt("spellState")
//			: 0;
//	compoundnbt.putInt("spellState", STATE_NEUTRAL);

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (amount > 9) {
			amount = 3;
		} else if (amount > 2) {
			amount = 2;
		}
		return super.damageItem(stack, amount, entity, onBroken);
	}
	
    private void doCastPreparedSpell( ServerPlayerEntity serverPlayer, ItemStack itemStack) {

		CompoundNBT compoundnbt = itemStack.getOrCreateTag();
		long spellCastingStartTime = compoundnbt != null && compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
				? compoundnbt.getLong("spellCastingStartTime")
				: 0;
		spellCastingStartTime = serverPlayer.world.getGameTime();
		compoundnbt.putLong("spellCastingStartTime", spellCastingStartTime);
		int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("preparedSpellNumber")
				: 0;
		Network.sendToClient(new SyncClientGuiPacket(-1, preparedSpellNumber), serverPlayer);

	}
    
	private void doChangePreparedSpell(ServerPlayerEntity serverPlayer, ItemStack itemStack ) {

		CompoundNBT compoundnbt = itemStack.getOrCreateTag();
		int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("preparedSpellNumber")
				: 0;

		float headPitch = serverPlayer.rotationPitch;
		if (headPitch <= -0.1) preparedSpellNumber = (preparedSpellNumber + 7) % 8;
		else preparedSpellNumber = (preparedSpellNumber + 1) % 8;

		compoundnbt.putInt("preparedSpellNumber", preparedSpellNumber);
		long spellCastingStartTime = compoundnbt != null && compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
				? compoundnbt.getLong("spellCastingStartTime")
				: 0;
		compoundnbt.putLong("spellCastingStartTime", SPELL_NOT_CASTING );

		Network.sendToClient(new SyncClientGuiPacket(preparedSpellNumber, -1), serverPlayer);
	}

	private void doPlayCastingTickSounds(ServerPlayerEntity serverPlayer, ItemStack stack, long spellCastingStartTime, int preparedSpellNumber ) {

		ServerWorld serverWorld = (ServerWorld) serverPlayer.getServerWorld();
		long castingDuration = serverWorld.getGameTime() - spellCastingStartTime;
		float soundModifier = 0.4f + (0.01f * castingDuration);
		if (soundModifier < 0.8f) {
			if ( castingDuration %3 == 0) {
				System.out.println(" doPlayCastingTickSounds.  preparedSpell=" + preparedSpellNumber);
				if (preparedSpellNumber == 0) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.ENTITY_CAT_HISS, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 1) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.ENTITY_FOX_AMBIENT, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 2) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 3) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 4) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 5) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 6) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 7) {
					doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.0f + soundModifier);
				} 
			}
		} else {
			if ( castingDuration %20 == 0) {
				doPlayTickSpellSound(serverPlayer, serverWorld, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 0.3f, 0.14f);
				System.out.println(" max spell power.");
			}
		}
			
	}

	private void doPlayTickSpellSound(ServerPlayerEntity serverPlayer, ServerWorld serverWorld, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
		serverWorld.playSound(null, serverPlayer.getPosition(), soundEvent, soundCategory, volume, pitch);
	}

	
	public int getItemEnchantability() {
		return 1;
	}


	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	
	@Override
	public boolean hasEffect(ItemStack stack) {
		CompoundNBT compoundnbt = stack.getOrCreateTag();
		long spellCastingStartTime = compoundnbt != null && compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
				? compoundnbt.getLong("spellCastingStartTime")
				: 0;
		if (spellCastingStartTime != SPELL_NOT_CASTING) {
			return true;
		} else {
			return false;
		}
	}

	
	@Override
	public boolean isDamageable() {
		return true;
	}


	@Override
	public boolean isShield(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

		if (playerIn instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerIn;
			ItemStack itemStack = serverPlayer.getHeldItem(handIn);
			if (canUseRedstoneFocusItem(serverPlayer)) {
				if (serverPlayer.isSneaking()) {  // change spell
					doChangePreparedSpell(serverPlayer, itemStack);				
				} else {
					doCastPreparedSpell(serverPlayer, itemStack);
				}
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

		if (entityLiving == null) {
			MyConfig.dbgPrintln(1, "onPlayerStoppedUsing: entityNull");
			return;
		}

		if (entityLiving instanceof ServerPlayerEntity) {
			// if I reset casting time here, it happens at arbitrary time.
		} else { // client side.

			//client side NBT will be overwritten.
			RedstoneMagicGuiEvent.spellBeingCast = "";
			RedstoneMagicGuiEvent.timerCastingSpell = 0;
			PlayerEntity clientPlayer = (PlayerEntity) entityLiving;
			if ((canUseRedstoneFocusItem(clientPlayer)) && 
			   (!(clientPlayer.isSneaking()))) {
				int handIndex = 0;
				ItemStack mainStack = clientPlayer.getHeldItemMainhand();
				ItemStack offStack = clientPlayer.getHeldItemOffhand();
				if (stack == mainStack) {
					handIndex = 1;
				}
				if (stack == offStack) {
					handIndex = 2;
				}
				CompoundNBT compoundnbt = stack.getOrCreateTag();
				long spellCastingStartTime = compoundnbt != null
						&& compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
								? compoundnbt.getLong("spellCastingStartTime")
								: 0;
				SoundEvent soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO; // "failure"
				// casting a spell
				long netSpellCastingTime = (((stack.getUseDuration() - timeLeft) + 5) / 10);

				int preparedSpellNumber = compoundnbt.getInt("preparedSpellNumber");
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));

				int minimumCastingTime = 1;
				if (spell.getSpellTranslationKey().equals("RM.TELE")) {
					minimumCastingTime = 4;
				}
				if (netSpellCastingTime < minimumCastingTime) {
					// spell fizzle too fast
					MyConfig.sendChat(clientPlayer, "Your spell fizzled.  Cast it longer.",
							Color.func_240744_a_(TextFormatting.RED));
					clientPlayer.world.playSound(clientPlayer, clientPlayer.getPosition(), soundEvent, SoundCategory.AMBIENT, 0.7f,
							0.3f);
				} else {

					float volume = 0.4f + (0.1f * netSpellCastingTime);
					float pitch = 0.3f;

					Entity targetEntity = null;
					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_OTHER)) {
						targetEntity = doLookForDistantTarget(clientPlayer);
						if (targetEntity != null)
							soundEvent = SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH;
					}

					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_BOTH)) {
						targetEntity = doLookForDistantTarget(clientPlayer);
						if (targetEntity == null)
							targetEntity = clientPlayer;
						soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
					}

					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_SELF)) {
						targetEntity = clientPlayer;
						soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
					}

					if (targetEntity != null) {
						clientPlayer.world.playSound(null, targetEntity.getPosition(), soundEvent, SoundCategory.PLAYERS,
								volume, pitch);
						Network.sendToServer(new RedstoneMagicPacket(preparedSpellNumber,targetEntity.getEntityId(),
								(int) netSpellCastingTime, handIndex) );
					} else {
						clientPlayer.world.playSound(null, clientPlayer.getPosition(), soundEvent, SoundCategory.PLAYERS,
								volume, pitch);
					}
				}
				
			}
			
		}

		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);

	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			CompoundNBT compoundnbt = stack.getOrCreateTag();
			long spellCastingStartTime = compoundnbt != null && compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
					? compoundnbt.getLong("spellCastingStartTime")
					: 0;
			int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
					? compoundnbt.getInt("preparedSpellNumber")
					: 0;
			if (spellCastingStartTime != SPELL_NOT_CASTING) {
				doPlayCastingTickSounds(serverPlayer, stack, spellCastingStartTime,preparedSpellNumber);
				
			}
		}
		super.onUsingTick(stack, player, count);
	}

}
