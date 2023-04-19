package com.mactso.redstonemagic.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3f;

import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import com.mactso.redstonemagic.config.ModExclusionListDataManager;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.network.SyncClientFlyingPacket;
import com.mactso.redstonemagic.network.SyncClientGuiPacket;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;
import com.mactso.redstonemagic.spells.CastSpells;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneFocusItem extends ShieldItem {

//  ANYTHING except constants here should probably be in the item CompoundNBT.
//  variables are global.
	public static final int NBT_NUMBER_FIELD = 99;
	public static final long SPELL_NOT_CASTING = -1;
	static final int TICKS_PER_SECOND = 20;
	public static final int NO_CHUNK_MANA_UPDATE = -1;

	static final int MANA_REGENERATION_DURATION = 300;

	static final ItemStack GHAST_TEAR_STACK = new ItemStack(Items.GHAST_TEAR);
	static ItemStack FLYING_REAGENT_STACK;
	
	static final int START_FLYING = 1;
	static final int KEEP_FLYING = 2;
	static final int STOP_FLYING = 3;

	static final List<Block> NO_FLY_LIST = new ArrayList<Block>(
			Arrays.asList(Blocks.WATER, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK));
	static final List<Block> FREE_FLY_LIST = new ArrayList<Block>(
			Arrays.asList(Blocks.REDSTONE_ORE, Blocks.REDSTONE_BLOCK));

	@OnlyIn(value = Dist.CLIENT)
	public static LivingEntity doLookForDistantTarget(Player clientPlayer) {
		
		double d0 = 36.0;
		double d1 = d0 * d0;
		Vec3 vector3d = clientPlayer.getEyePosition(1.0F);
		Vec3 vector3d1 = clientPlayer.getViewVector(1.0F).scale(d0);
		Vec3 vector3d2 = vector3d.add(vector3d1);

		AABB axisalignedbb = clientPlayer.getBoundingBox().expandTowards(vector3d1).inflate(1.0D);
		EntityHitResult entityRayTraceResult = ProjectileUtil.getEntityHitResult(clientPlayer, vector3d,
				vector3d2, axisalignedbb, (p_215312_0_) -> {
					return !p_215312_0_.isSpectator() && p_215312_0_.isPickable();
				}, d1);
		if (entityRayTraceResult != null) {
			Entity entity1 = entityRayTraceResult.getEntity();
			if (entity1 instanceof LivingEntity) { // if can see
				if ( clientPlayer.level
						.clip(new ClipContext(vector3d, entityRayTraceResult.getLocation(),
								ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, clientPlayer))
						.getType() == HitResult.Type.MISS) {
					return (LivingEntity) entity1;
				}
			}
		}
		return null;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
			return Rarity.RARE;
	}
	@OnlyIn(value = Dist.CLIENT)
	public static BlockPos doLookForDistantBlock(Player clientPlayer, int distance) {

		double d0 = distance;
		double d1 = d0 * d0;
		Vec3 vector3d = clientPlayer.getEyePosition(1.0F);
		Vec3 vector3d1 = clientPlayer.getViewVector(1.0F);
		Vec3 vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

		Level world = clientPlayer.getCommandSenderWorld();

		ClipContext r1 = new ClipContext(vector3d, vector3d2, ClipContext.Block.COLLIDER, Fluid.NONE, clientPlayer);
		Vec3 hitPosition = world.clip(r1).getLocation();

		Vec3 eyePos = clientPlayer.getEyePosition(0);
		Vec3 lookVector = clientPlayer.getLookAngle().scale(30.0D);

		ClipContext r2 = new ClipContext(eyePos, lookVector, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, clientPlayer);
		Vec3 hitPosition2 = world.clip(r2).getLocation();

		BlockPos targetPos = null;

		if (hitPosition2 != null) {
			Vec3 vL = clientPlayer.getViewVector(1.0F);
			targetPos = BlockPos.containing(hitPosition.x() - vL.x(), hitPosition.y() - vL.y(), hitPosition.z() - vL.z());
			Block b = world.getBlockState(targetPos).getBlock();
			if (!(b instanceof AirBlock)) {
				targetPos = null;
			}
		}
		Direction d = clientPlayer.getDirection();
		d = d.getOpposite();
		return targetPos;

	}

	public static LivingEntity target(Player player) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hitResult.getType() == Type.ENTITY) {
			Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
			if (entity instanceof LivingEntity)
				return (LivingEntity) entity;
		}
		return null;
	}

	public RedstoneFocusItem(Properties builder) {
		super(builder);
	}


	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		int preparedSpellNumber = 0;
		CompoundTag tag = stack.getTag();
        if (tag != null) {
        	if (tag.contains("ownerName")) {
                 tooltip.add(Component.literal("Owner: "+ tag.getString("ownerName")).withStyle(ChatFormatting.GOLD));
        	}
        	if (tag.contains("preparedSpellNumber")) {
        		preparedSpellNumber = tag.getInt("preparedSpellNumber");
         	}
        }
   		SpellManager.RedstoneMagicSpellItem spellItem = SpellManager
				.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));
		tooltip.add(Component.literal("Spell: " + spellItem.getSpellComment()).withStyle(ChatFormatting.RED));

        // @TODO
        // TranslationTextComponent("ca.hover.claim_block").withStyle(TextFormatting.DARK_GREEN));
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	public static void setIsFlying(Player player, boolean isFlying, long chunkAge) {

		CompoundTag compoundnbt = player.getPersistentData();
		if (compoundnbt.getBoolean("isFlying") != isFlying) {
			compoundnbt.putBoolean("isFlying", isFlying);
			if (player instanceof ServerPlayer) {
				setChunkAge(player, chunkAge);
				Network.sendToClient(new SyncClientFlyingPacket(isFlying, getIsChunkFlying(player), chunkAge),
						(ServerPlayer) player);
			}
		}
		if (!isFlying) {
			player.setForcedPose(null);
		}

	}

	public static boolean getIsFlying(Player player) {
		CompoundTag compoundnbt = player.getPersistentData();
		return compoundnbt.getBoolean("isFlying");
	}

	public static void setIsChunkFlying(Player player, boolean bool) {
		CompoundTag compoundnbt = player.getPersistentData();
		compoundnbt.putBoolean("isChunkFlying", bool);
	}

	public static boolean getIsChunkFlying(Player player) {
		CompoundTag compoundnbt = player.getPersistentData();
		return compoundnbt.getBoolean("isChunkFlying");
	}

	// exists because client side chunk "inhabited time" field is not populated.
	public static void setChunkAge(Player player, long chunkAge) {
		CompoundTag compoundnbt = player.getPersistentData();
		compoundnbt.putLong("chunkAge", chunkAge);
	}

	// exists because client side chunk "inhabited time" field is not populated.
	public static long getChunkAge(Player player) {
		CompoundTag compoundnbt = player.getPersistentData();
		long chunkAge = compoundnbt.getLong("chunkAge");
		if (MyConfig.getDebugLevel() > 0) {
			MyConfig.sendChat(player, "ChunkAge:" + chunkAge, ChatFormatting.AQUA);
		}
		return chunkAge;
	}

	private boolean canUseRedstoneFocusItem(Player playerIn) {

		int baseWeaponDamage = 0;
		boolean canUseRedstoneFocus = false;

		ItemStack handItem = playerIn.getMainHandItem();
		ItemStack offHandItem = playerIn.getOffhandItem();

		if (!(handItem.getItem() instanceof RedstoneFocusItem)
				&& !(offHandItem.getItem() instanceof RedstoneFocusItem)) {
			return false;
		}

//		String i = handItem.getDescriptionId().toString();
		if (handItem.getUseDuration() == 0) {
			canUseRedstoneFocus = true;
		}

		if ((handItem.getItem() == Items.LADDER) || (handItem.getItem() == Items.BONE_MEAL) || (handItem.getItem() instanceof ShovelItem) || (handItem.getItem() instanceof HoeItem)) {
			canUseRedstoneFocus = false;
			return canUseRedstoneFocus;
		}
		
		// replace this with the item being clicked!  (waystones) but hand device should work.
		// replace this with a list later but hard coded for now.
		
		String modName = handItem.getItem().builtInRegistryHolder().key().location().getNamespace();
		if (playerIn.level.getGameTime()%20 == 0) {
			MyConfig.dbgPrintln(1, "modname of item in hand:"+ modName);
		}
		
		if (ModExclusionListDataManager.getModExclusionListItem(modName) != null) {
			canUseRedstoneFocus = false;
			return canUseRedstoneFocus;
		}

		if (handItem.getItem() instanceof RedstoneFocusItem) {
			canUseRedstoneFocus = true;
			return canUseRedstoneFocus;
		}
		Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlot.MAINHAND)
				.get(Attributes.ATTACK_DAMAGE);
		for (AttributeModifier attr : d) {
			baseWeaponDamage = (int) attr.getAmount();
			if (baseWeaponDamage >= 1) {
				canUseRedstoneFocus = true;
				break;
			}
		}
		return canUseRedstoneFocus;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		CompoundTag tag = stack.getTag();
		if (amount > 9) {
			amount = 3;
			tag.putLong("stopDamageSound", 2);
		} else if (amount > 2) {
			amount = 2;
			tag.putLong("stopDamageSound", 1);
		}
		return super.damageItem(stack, amount, entity, onBroken);
	}

	private void doCastPreparedSpell(ServerPlayer serverPlayer, ItemStack itemStack) {

		CompoundTag compoundnbt = itemStack.getOrCreateTag();
		long spellCastingStartTime = compoundnbt != null
				&& compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
						? compoundnbt.getLong("spellCastingStartTime")
						: 0;
		spellCastingStartTime = serverPlayer.level.getGameTime();
		compoundnbt.putLong("spellCastingStartTime", spellCastingStartTime);
		int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("preparedSpellNumber")
				: 0;
		Network.sendToClient(new SyncClientGuiPacket(-1, preparedSpellNumber), serverPlayer);

	}

	private void doChangePreparedSpell(ServerPlayer serverPlayer, ItemStack itemStack) {

		CompoundTag compoundnbt = itemStack.getOrCreateTag();
		int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("preparedSpellNumber")
				: 0;

		float headPitch = serverPlayer.getXRot();
		if (headPitch <= -0.1)
			preparedSpellNumber = (preparedSpellNumber + 7) % 8;
		else
			preparedSpellNumber = (preparedSpellNumber + 1) % 8;

		compoundnbt.putInt("preparedSpellNumber", preparedSpellNumber);
		long spellCastingStartTime = compoundnbt != null
				&& compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
						? compoundnbt.getLong("spellCastingStartTime")
						: 0;
		compoundnbt.putLong("spellCastingStartTime", SPELL_NOT_CASTING);

		Network.sendToClient(new SyncClientGuiPacket(preparedSpellNumber, -1), serverPlayer);
	}

	private void doPlayCastingTickSounds(ServerPlayer serverPlayer, ItemStack stack, long spellCastingStartTime,
			int preparedSpellNumber) {

		ServerLevel serverWorld = (ServerLevel) serverPlayer.getLevel();
		long castingDuration = serverWorld.getGameTime() - spellCastingStartTime;
		float soundModifier = 0.4f + (0.01f * castingDuration);
		if (soundModifier < 0.8f) {
			if (castingDuration % 5 == 0) {
				if (preparedSpellNumber == 0) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_NUKE, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 1) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_HEAL, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 2) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_DOT, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 3) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_SDOT, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 4) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_RESI, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 5) {
					if (castingDuration == 3) {
						float headPitch = serverPlayer.getXRot();
						if (headPitch < 0) {
							doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_TELE_START,
									SoundSource.AMBIENT, 0.3f + soundModifier, 0.3f + soundModifier);
						}
					}
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_TELE, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 6) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_BUFF, SoundSource.WEATHER,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 7) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_RCRS, SoundSource.WEATHER,
							0.3f + soundModifier, 0.0f + soundModifier);
				}
			}
		} else {
			if (castingDuration % 20 == 0) {
				doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.SPELL_RESONATES, SoundSource.BLOCKS, 0.3f,
						0.14f);
			}
		}

	}

	private void doPlayTickSpellSound(ServerPlayer serverPlayer, ServerLevel serverWorld, SoundEvent soundEvent,
			SoundSource soundCategory, float volume, float pitch) {
		serverWorld.playSound(null, serverPlayer.blockPosition(), soundEvent, soundCategory, volume, pitch);
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		CompoundTag compoundnbt = stack.getOrCreateTag();
		long spellCastingStartTime = compoundnbt != null
				&& compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
						? compoundnbt.getLong("spellCastingStartTime")
						: 0;
		if (spellCastingStartTime != SPELL_NOT_CASTING) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

		if (playerIn instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer) playerIn;
			ItemStack stack = serverPlayer.getItemInHand(handIn);
			CompoundTag compoundnbt = stack.getOrCreateTag();
			int preparedSpellNumber = compoundnbt != null
					&& compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
							? compoundnbt.getInt("preparedSpellNumber")
							: 0;

			if (canUseRedstoneFocusItem(serverPlayer)) {
				if (serverPlayer.isShiftKeyDown()) { // change spell
					doChangePreparedSpell(serverPlayer, stack);
				} else if ((playerCanFly(serverPlayer) && (preparedSpellNumber == 0))) {
					if (doPayFlyingCost(serverPlayer, START_FLYING)) {
						ServerLevel sWorld = serverPlayer.getLevel();
						BlockPos pos = serverPlayer.blockPosition();
						setIsFlying(serverPlayer, true, sWorld.getChunkAt(pos).getInhabitedTime());
						sWorld.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5D + (double) pos.getX(),
								0.5D + (double) pos.getY(), 0.5D + (double) pos.getZ(), 13, 0, -1.15D, 0, 0.06D);
					}
				} else {
					doCastPreparedSpell(serverPlayer, stack);
				}

			}
		}

		return super.use(worldIn, playerIn, handIn);
	}

	private boolean playerCanFly(ServerPlayer serverPlayer) {
		if (FLYING_REAGENT_STACK == null) {
			FLYING_REAGENT_STACK = new ItemStack(ModItems.FLYING_REAGENT);
		}

		ServerLevel sWorld = serverPlayer.getLevel();
		BlockPos pos = serverPlayer.blockPosition();
		if (MyConfig.getMaxFlightSpeed() == 0) {
			return false;
		}
		if (!CastSpells.hasFalderal(serverPlayer, GHAST_TEAR_STACK)) {
			if (!CastSpells.hasReagent(serverPlayer, FLYING_REAGENT_STACK)) {
				return false;
			}
		}
		if ((sWorld.getBlockState(pos).getBlock() == Blocks.WATER)
				|| (NO_FLY_LIST.contains(sWorld.getBlockState(pos.below()).getBlock()))) {
			sWorld.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.SPELL_FAILS, SoundSource.BLOCKS,
					0.5F, 1.5F);
			return false;
		}
		return true;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {

		if (entityLiving == null) {
			MyConfig.dbgPrintln(1, "onPlayerStoppedUsing: entityNull");
			return;
		}

		boolean wasFlying = false;
		if (entityLiving instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer) entityLiving;
			if (getIsFlying(serverPlayer)) {
				doStopFlying(serverPlayer);
				wasFlying = true;
			}
			// if I reset casting time here, it happens at arbitrary time.
		} else { // client side.

			// client side NBT will be overwritten.
			RedstoneMagicGuiEvent.spellBeingCast = "";
			RedstoneMagicGuiEvent.timerCastingSpell = 0;
			CompoundTag compoundnbt = stack.getOrCreateTag();
			compoundnbt.putLong("spellCastingStartTime", SPELL_NOT_CASTING);

			Player clientPlayer = (Player) entityLiving;
			if ((canUseRedstoneFocusItem(clientPlayer)) && (!(clientPlayer.isShiftKeyDown()))) {
				int handIndex = 0;
				ItemStack mainStack = clientPlayer.getMainHandItem();
				ItemStack offStack = clientPlayer.getOffhandItem();
				if (stack == mainStack) {
					handIndex = 1;
				}
				if (stack == offStack) {
					handIndex = 2;
				}
				SoundEvent soundEvent = ModSounds.SPELL_FAILS;
				// casting a spell
				long netSpellCastingTime = (((stack.getUseDuration() - timeLeft) + 5) / 10);
				
				long netTicks = stack.getUseDuration() - timeLeft;
				// MyConfig.sendChat((PlayerEntity) entityLiving, "netTicks:"+netTicks);
				int preparedSpellNumber = compoundnbt.getInt("preparedSpellNumber");
				
				
				RedstoneMagicSpellItem spell = SpellManager
						.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));

				// Hack for mods that do not consume the click, maximum casting time.
				int maxCastingTime = 160;
				if (preparedSpellNumber == 0) maxCastingTime = 500;
				if (preparedSpellNumber == 1) maxCastingTime = 400;

				int minimumCastingTime = 1;
				if (spell.getSpellTranslationKey().equals("redstonemagic.tele")) {
					minimumCastingTime = 4;
					maxCastingTime = 180;
				}
				

				if (netTicks < maxCastingTime) {
					
					if (netSpellCastingTime < minimumCastingTime) {
						if (RedstoneMagicGuiEvent.getFizzleSpamLimiter() < 0) {
							RedstoneMagicGuiEvent.setFizzleSpamLimiter(120);
							MutableComponent msg = Component.translatable("redstonemagic.fizz");
							if (!MyConfig.getGuiSpamChatFilter()) {
								MyConfig.sendChat(clientPlayer, msg.toString(), ChatFormatting.RED);
							}
							clientPlayer.level.playSound(clientPlayer, clientPlayer.blockPosition(), soundEvent,
									SoundSource.AMBIENT, 0.6f, 0.3f);
						}
					} else {

						float volume = 0.4f + (0.1f * netSpellCastingTime);
						float pitch = 0.3f;

						Entity targetEntity = null;
						BlockPos targetPos = null;
						if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_OTHER)) {
							targetEntity = doLookForDistantTarget(clientPlayer);
							if (targetEntity != null) {
								soundEvent = SoundEvents.FIREWORK_ROCKET_LAUNCH;
							}
						}

						if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_BOTH)) {
							targetEntity = doLookForDistantTarget(clientPlayer);
							if (targetEntity == null) {
								targetEntity = clientPlayer;
							}
							int distance = 10 + (int) (7 * clientPlayer.level.random.nextFloat());
							targetPos = doLookForDistantBlock(clientPlayer, distance);
							soundEvent = SoundEvents.NOTE_BLOCK_CHIME.get();
						}

						if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_SELF)) {
							targetEntity = clientPlayer;
							targetPos = targetEntity.blockPosition();
							soundEvent = SoundEvents.NOTE_BLOCK_CHIME.get();
						}

						if (targetEntity != null) {
							clientPlayer.level.playSound(null, targetEntity.blockPosition(), soundEvent,
									SoundSource.PLAYERS, volume, pitch);
							if (targetPos != null) {
								Network.sendToServer(new RedstoneMagicPacket(preparedSpellNumber, targetEntity.getId(),
										(int) netSpellCastingTime, handIndex, targetPos.getX(), targetPos.getY(),
										targetPos.getZ()));
							} else {
								Network.sendToServer(new RedstoneMagicPacket(preparedSpellNumber, targetEntity.getId(),
										(int) netSpellCastingTime, handIndex, -1, -99999, -1));
							}
						} else {
							clientPlayer.level.playSound(null, clientPlayer.blockPosition(), soundEvent,
									SoundSource.PLAYERS, volume, pitch);
						}
					}
					
				}

			}

		}

		super.releaseUsing(stack, worldIn, entityLiving, timeLeft);

	}

	private void doStopFlying(ServerPlayer serverPlayer) {
		ServerLevel sw = serverPlayer.getLevel();
		BlockPos pos = serverPlayer.blockPosition();
		setIsFlying(serverPlayer, false, sw.getChunk(pos).getInhabitedTime());
		serverPlayer.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, true));
		sw.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5D + (double) pos.getX(), 0.5D + (double) pos.getY(),
				0.5D + (double) pos.getZ(), 7, 0, -1.15D, 0.03D, 0.26D);
		sw.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F,
				1.5F);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		long gameTime = worldIn.getGameTime();
		BlockPos pos = entityIn.blockPosition();
		int stopDamageSound = 0;
		if ((entityIn instanceof ServerPlayer)) {
			ServerPlayer sPlayer = (ServerPlayer) entityIn;
			CompoundTag tag= stack.getOrCreateTag();
        	if (!(tag.contains("ownerUUID"))) {
        		tag.putUUID("ownerUUID", sPlayer.getUUID());
        	}
        	if (!(tag.contains("ownerName"))) {
        		tag.putString("ownerName", sPlayer.getDisplayName().getString());
        	}
        	if (!(tag.contains("preparedSpellNumber"))) {
        		tag.putInt("preparedSpellNumber", 0);
        	}
        	
//			Put this on hold until I record a custom sound.
//        	if (tag.contains("stopDamageSound")) {
//        		stopDamageSound = tag.getInt("stopDamageSound");
//        		tag.putInt("stopDamageSound", 0);
//        	}
//
//        	if (stopDamageSound == 1) {
//    			sPlayer.level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.NOTE_BLOCK_COW_BELL, SoundCategory.BLOCKS,
//    					1.0F, 0.6F );
//        	} else if (stopDamageSound == 2) {
//        		sPlayer.level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SHIELD_BREAK, SoundCategory.BLOCKS,
//    					0.50F, 0.5F );
//        	}
        	
			if (!canUseRedstoneFocusItem(sPlayer)) {
				if (getIsFlying(sPlayer)) {
					doStopFlying(sPlayer);
				}
			}

			int chunkMana = NO_CHUNK_MANA_UPDATE;
			// update client with current chunk mana twice per second.
			if (gameTime%10==0) {
				LevelChunk baseChunk = worldIn.getChunkAt(entityIn.blockPosition());				
				IMagicStorage cap = baseChunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
				if (cap != null) {
					chunkMana = cap.getManaStored();
					if (chunkMana > -1) {
						Network.sendToClient(
								new SyncClientManaPacket(MyConfig.NO_PLAYER_MANA_UPDATE, chunkMana),
								sPlayer);
						
					}

				}
			}
			

			Block b = worldIn.getBlockState(pos.below()).getBlock();
			boolean fasterManaRegen = false;
			if (FREE_FLY_LIST.contains(b)) {
				fasterManaRegen = true;
				if (gameTime % 20 == 0) {
					((ServerLevel) worldIn).sendParticles(ParticleTypes.WITCH, 0.5D + (double) pos.getX(),
							0.75D + (double) pos.below(1).getY(), 0.5D + (double) pos.getZ(), 11, 0.5D, -1.15D, 0.5D,
							0.2D);
				}
			}
			if (gameTime % MANA_REGENERATION_DURATION == 0) { // every 300 ticks / 15 seconds.
				IMagicStorage playerManaStorage = sPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
				int manaLevel = playerManaStorage.getManaStored();
				// MyConfig.sendChat (sPlayer, "Manalevel:" + manaLevel);
				if (manaLevel < 20) {
					int maxMana = MyConfig.getMaxPlayerRedstoneMagic();
					if (maxMana < 1) {
						MyConfig.setMaxPlayerRedstoneMagic(300);
						maxMana = 300;
					}
					int manaLevelPercent = (100 * manaLevel) / maxMana;

					if (manaLevelPercent <= 2) {
						if (!NO_FLY_LIST.contains(b) && b != Blocks.AIR) {
							int m = 1;
							if (fasterManaRegen) {
								m = 2;
							}
							playerManaStorage.addMana(m);
							
//							MyConfig.sendChat(sPlayer,"Focus Regen Mana: "+ playerManaStorage.getManaStored() + ", max:" +maxMana + ", regen:"+ m, Color.fromLegacyFormat(TextFormatting.RED));

							Network.sendToClient(
									new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE),
									sPlayer);
						}
					}
				}
			}
		} else { // client side - update gui.
			long gametime = worldIn.getGameTime();
			Player p = (Player) entityIn;
			RandomSource rand = p.level.random;

			
			// show redstone sparkles if the chunk has over 64 mana.
				long chunkMana = RedstoneMagicGuiEvent.getCurrentChunkRedstoneMana();
				while (chunkMana > 64) {
// 					MyConfig.sendChat(p, "Particles chunkMana = " + chunkMana);
					chunkMana /= 4;
					double xOffset = 0.5f + pos.getX() + rand.nextInt(16) - 8;
					double zOffset = 0.5f + pos.getZ() + rand.nextInt(16) - 8;
					double yOffset = pos.getY() + rand.nextFloat();
					worldIn.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.05F), 0.95f), xOffset, yOffset, zOffset,
							0.0D, 0.1D, 0.06D);
					worldIn.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.05F), 0.75f), 16-xOffset, 1-yOffset, 15-zOffset,
							0.0D, 0.1D, 0.06D);
				}
			

			if (gametime % 5 == 0) {

				ItemStack mainHand = p.getMainHandItem();
				if (mainHand.getItem() instanceof RedstoneFocusItem) {
					CompoundTag compoundnbt = mainHand.getOrCreateTag();
					int preparedSpellNumber = compoundnbt != null
							&& compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
									? compoundnbt.getInt("preparedSpellNumber")
									: 0;
					if (MyConfig.getDebugLevel() > 1) {
						System.out.println("main hand prepared spell:");
					}
					RedstoneMagicGuiEvent.setPreparedSpellNumber(preparedSpellNumber);

				} else {
					ItemStack offHand = p.getOffhandItem();
					CompoundTag compoundnbt = offHand.getOrCreateTag();
					int preparedSpellNumber = compoundnbt != null
							&& compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
									? compoundnbt.getInt("preparedSpellNumber")
									: 0;
					RedstoneMagicGuiEvent.setPreparedSpellNumber(preparedSpellNumber);

				}
			}

		}

	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		long gameTime = player.level.getGameTime();

		if (player instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer) player;
			CompoundTag compoundnbt = stack.getOrCreateTag();

			long spellCastingStartTime = compoundnbt != null
					&& compoundnbt.contains("spellCastingStartTime", NBT_NUMBER_FIELD)
							? compoundnbt.getLong("spellCastingStartTime")
							: 0;
			if (spellCastingStartTime != SPELL_NOT_CASTING) {
				int preparedSpellNumber = compoundnbt != null
						&& compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
								? compoundnbt.getInt("preparedSpellNumber")
								: 0;
				doPlayCastingTickSounds(serverPlayer, stack, spellCastingStartTime, preparedSpellNumber);
			}
			IMagicStorage playerManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (gameTime %20 == 0) {
			// MyConfig.dbgPrintln(0, "mana:" + playerManaStorage.getManaStored());
			}
			if (getIsFlying((Player) serverPlayer)) {
				if (gameTime % 2 == 0) {
					doServerPlayerFlying(serverPlayer, stack);
				}
				if (doPayFlyingCost(serverPlayer, KEEP_FLYING)) {
					mySetForcedPose(serverPlayer);
				}
			}
		} else { // client
			if (getIsFlying((Player) player)) {
				if (gameTime % 2 == 0) {
					doClientPlayerFlying((Player) player);
				}
				mySetForcedPose((Player) player);
			}

		}

		super.onUsingTick(stack, player, count);

	}

	private void mySetForcedPose(Player player) {
		Pose p = Pose.FALL_FLYING;
		if (player.getViewXRot(1.0f) < -50.0f) {
			p = null;
		}
		player.setForcedPose(p);
	}

	private boolean doPayFlyingCost(ServerPlayer serverPlayer, int flyingType) {
		if (FLYING_REAGENT_STACK == null) {
			FLYING_REAGENT_STACK = new ItemStack(ModItems.FLYING_REAGENT);
		}
		BlockPos pos = serverPlayer.blockPosition();
		ServerLevel sw = serverPlayer.getLevel();
		LevelChunk chunk = (LevelChunk) sw.getChunk(serverPlayer.blockPosition());
		Block groundBlock = sw.getBlockState(pos.below()).getBlock();

		int flightTime = MyConfig.getFlightTime() * 20;
		int manaCost = 11;
		
		if (!playerCanFly(serverPlayer)) {
			return false;
		}

		if ((flyingType == START_FLYING) || ((serverPlayer.getLevel().getGameTime() % flightTime) == 0)) {
			// MyConfig.sendChat(serverPlayer, "Pay for flying",
			// Color.fromLegacyFormat(TextFormatting.RED));
			
			if (serverPlayer.getLevel().getBlockState(pos).getBlock() == Blocks.WATER) {
				setIsFlying(serverPlayer, false, serverPlayer.getLevel().getChunk(pos).getInhabitedTime());
				return false;
			}

			if (CastSpells.hasFalderal(serverPlayer, GHAST_TEAR_STACK)) {
				// 
			} else if (!CastSpells.useReagent(serverPlayer, FLYING_REAGENT_STACK)){
				// stop flying when out of flight reagents.
				return false;
			}
			
			if (flyingType == START_FLYING) {
				manaCost = 1;
			}

			if (FREE_FLY_LIST.contains(groundBlock)) {
				manaCost = 0;
			}

			IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (chunkManaStorage.useMana(manaCost)) {
				// MyConfig.sendChat(serverPlayer, "Pay Fly from Chunk: " + manaCost + " of " +
				// chunkManaStorage.getManaStored(),
				// Color.fromLegacyFormat(TextFormatting.RED));
				Network.sendToClient(
						new SyncClientManaPacket(MyConfig.NO_PLAYER_MANA_UPDATE, chunkManaStorage.getManaStored()),
						serverPlayer);

				setIsChunkFlying(serverPlayer, true);
			} else {

				setIsChunkFlying(serverPlayer, false);
				IMagicStorage playerManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
				if (playerManaStorage == null) {
					return false;
				}
				if (manaCost > 0) {
					manaCost = 2;
				}

				long chunkAgeFactor = Math.max((long) (6 - (getChunkAge(serverPlayer) / 500)), 0);
				manaCost += chunkAgeFactor;

				// distance to last seen mana gatherer.

				if (flyingType == START_FLYING) {
					// prevent scam flying.
					if (playerManaStorage.getManaStored() < 4) {
						doStopFlying(serverPlayer);
						return false;
					}
					manaCost = (manaCost / 2) + 1;
				}

				serverPlayer.causeFoodExhaustion(0.5f);
				ItemStack stack = serverPlayer.getMainHandItem();
				
				// pay some food exhaustion for super fast flying.
				if (stack.getItem() == GHAST_TEAR_STACK.getItem()) {
					serverPlayer.causeFoodExhaustion(0.5f);
				}

				MyConfig.dbgPrintln(1, "playerManaStorage.getMana() : "+playerManaStorage.getManaStored() + "  Flying manaCost: " + manaCost);

				if (playerManaStorage.useMana(manaCost)) {

					Network.sendToClient(
							new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE),
							serverPlayer);
					long chunkAge = serverPlayer.getLevel().getChunk(serverPlayer.blockPosition()).getInhabitedTime();
					setChunkAge(serverPlayer, chunkAge);
					Network.sendToClient(new SyncClientFlyingPacket(getIsFlying(serverPlayer),
							getIsChunkFlying(serverPlayer), chunkAge), (ServerPlayer) serverPlayer);
				} else {
					doStopFlying(serverPlayer);
					return false;
				}
			}

		}
		return true;
	}

	private void doServerPlayerFlying(ServerPlayer serverPlayer, ItemStack stack) {

		double newSpeed = calcNewSpeed(serverPlayer);
		serverPlayer.setDeltaMovement(serverPlayer.getLookAngle().scale(newSpeed));
		ServerLevel sw = serverPlayer.getLevel();
		BlockPos pos = serverPlayer.blockPosition();
		float pitchMod = (sw.random.nextFloat() - 0.5F);
		if (getIsChunkFlying(serverPlayer)) {
			sw.sendParticles(ParticleTypes.CRIMSON_SPORE, 0.5D + (double) pos.getX(), 0.5D + (double) pos.getY(),
					0.5D + (double) pos.getZ(), 11, 0.7D, -0.4D, 0.15D, 0.16D);
			sw.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.REDSTONEMAGIC_FLY, SoundSource.BLOCKS,
					0.30F, 0.05F + pitchMod / 16);
		} else {
			sw.sendParticles(DustParticleOptions.REDSTONE, 0.5D + (double) pos.getX(), 0.5D + (double) pos.getY(),
					0.5D + (double) pos.getZ(), 11, 0.8D, -.5D, 0.2D, 0.16D);
			sw.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.REDSTONEMAGIC_FLY, SoundSource.BLOCKS,
					0.15F, 2.0F + pitchMod);
		}

	}

	private double calcNewSpeed(Player player) {
		float currentSpeed = (float) player.getDeltaMovement().length();
		long chunkAge = getChunkAge(player);
		float potentialMaxFlightSpeed = (float) (MyConfig.getMaxFlightSpeed() / TICKS_PER_SECOND);
		float maxFlightSpeed = potentialMaxFlightSpeed;
		ItemStack stack = player.getMainHandItem();
		if (chunkAge != 0 && chunkAge < 5000) {
			maxFlightSpeed = potentialMaxFlightSpeed * 0.5f;
		}
		if (stack.getItem() == Items.GHAST_TEAR) {
			maxFlightSpeed = maxFlightSpeed * 1.25f;

		}

		Iterable<ItemStack> ar = player.getArmorSlots();
		float armorSpeedModifier = 0.92f;
		for (ItemStack is : ar) {
			if (is.getItem() instanceof RedstoneArmorItem) {
				RedstoneArmorItem r = (RedstoneArmorItem) is.getItem();
				if (r.getMaterial() == ModItems.REDSTONEMAGIC_MATERIAL) {
					armorSpeedModifier += 0.02f;
				} else if (r.getMaterial() == ModItems.REDSTONEMAGIC_LEATHER_MATERIAL) {
					armorSpeedModifier -= 0.03f;
				}
			} else {
				armorSpeedModifier -= 0.07f;
			}
		}

		maxFlightSpeed *= armorSpeedModifier;

		if (currentSpeed < maxFlightSpeed) {
			currentSpeed = (currentSpeed + (maxFlightSpeed) / 20) * 1.33f; // "magic" number that feels right.
			if (currentSpeed < 0.20f) {
				currentSpeed = 0.20f;
			}
		}

		if (currentSpeed > maxFlightSpeed) {
			currentSpeed = (currentSpeed - maxFlightSpeed) / 2 + maxFlightSpeed; // smooth slowdown
		}

		// MyConfig.sendChat(player, "currentSpeed:" + currentSpeed + ",
		// ArmorMod:"+armorSpeedModifier+ ", MaxFlySpeed: " + maxFlightSpeed,
		// Color.fromLegacyFormat(TextFormatting.RED));

		return currentSpeed;
	}

	private void doClientPlayerFlying(Player player) {
		if (getIsFlying(player)) {
			double newSpeed = calcNewSpeed(player);
			player.setDeltaMovement(player.getLookAngle().scale(newSpeed));
		}
	}
}
