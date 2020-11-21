package com.mactso.redstonemagic.item;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.mactso.redstonemagic.client.gui.RedstoneMagicGuiEvent;
import com.mactso.redstonemagic.config.ModExclusionListDataManager;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.config.SpellManager;
import com.mactso.redstonemagic.config.SpellManager.RedstoneMagicSpellItem;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.RedstoneMagicPacket;
import com.mactso.redstonemagic.network.SyncClientGuiPacket;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
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
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneFocusItem extends ShieldItem {

	public final static int NBT_NUMBER_FIELD = 99;
	public final static long SPELL_NOT_CASTING = -1;
	static final int NO_CHUNK_MANA_UPDATE = -1;

	@OnlyIn(value = Dist.CLIENT)
	public static LivingEntity doLookForDistantTarget(PlayerEntity clientPlayer) {
		double d0 = 30.0;
		double d1 = d0 * d0;
		Vector3d vector3d = clientPlayer.getEyePosition(1.0F);
		Vector3d vector3d1 = clientPlayer.getLook(1.0F);
		Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

		AxisAlignedBB axisalignedbb = clientPlayer.getBoundingBox().expand(vector3d1.scale(d0)).grow(1.0D, 1.0D, 1.0D);
		EntityRayTraceResult entityRayTraceResult = ProjectileHelper.rayTraceEntities(clientPlayer, vector3d, vector3d2,
				axisalignedbb, (p_215312_0_) -> {
					return !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith();
				}, d1);
		if (entityRayTraceResult != null) {
			Entity entity1 = entityRayTraceResult.getEntity();
			Vector3d vector3d3 = entityRayTraceResult.getHitVec();
			if (entity1 instanceof LivingEntity) {
				LivingEntity livingEntity = (LivingEntity) entity1;
				if (livingEntity.canEntityBeSeen(clientPlayer)) {
					return livingEntity;
				}
			}
		}
		return null;
	}

	@OnlyIn(value = Dist.CLIENT)
	public static BlockPos doLookForDistantBlock(PlayerEntity clientPlayer) {
		
		double d0 = 30.0;
		double d1 = d0 * d0;
		Vector3d vector3d = clientPlayer.getEyePosition(1.0F);
		Vector3d vector3d1 = clientPlayer.getLook(1.0F);
		Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);

		World world = clientPlayer.getEntityWorld();

		
		RayTraceContext r1 = new RayTraceContext(vector3d, vector3d2, BlockMode.COLLIDER, FluidMode.NONE, clientPlayer);
		Vector3d hitPosition = world.rayTraceBlocks(r1).getHitVec();

		Vector3d eyePos = clientPlayer.getEyePosition(0);
		Vector3d lookVector = clientPlayer.getLookVec().scale(30.0D);

		RayTraceContext r2 = new RayTraceContext(eyePos, lookVector, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, clientPlayer);
		Vector3d hitPosition2 = world.rayTraceBlocks(r2).getHitVec();

		BlockPos targetPos = null;
	
		
		if (hitPosition2 != null) {
			Vector3d vL = clientPlayer.getLook(1.0F);			
			targetPos = new BlockPos (hitPosition.getX()-vL.getX(),
					hitPosition.getY()-vL.getY(),
					hitPosition.getZ()-vL.getZ());
			Block b = world.getBlockState(targetPos).getBlock();
			if (!(b instanceof AirBlock)) {
				targetPos = null;
			}
		}
//		System.out.println(" " + p.getX() + " " + p.getY() + " " + p.getZ());
		Direction d = clientPlayer.getHorizontalFacing();
		d = d.getOpposite();
// server world command		world.setBlockState(p, Blocks.WALL_TORCH.getDefaultState().with(BlockStateProperties.FACING, d));
		return targetPos;

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
		if (handItem.getUseDuration() == 0) {
			canUseRedstoneFocus = true;
		}
		
		if ((handItem.getItem() == Items.LADDER) ||
			(handItem.getItem() instanceof ShovelItem)) {
			canUseRedstoneFocus = false;
			return canUseRedstoneFocus;
		}
		
		// replace this with a list later but hard coded for now.
		String modName= handItem.getItem().getRegistryName().getNamespace();
		if (ModExclusionListDataManager.getModExclusionListItem(modName) != null) {
			canUseRedstoneFocus = false;
			return canUseRedstoneFocus;
		}
		
		if (handItem.getItem() instanceof RedstoneFocusItem) {
			canUseRedstoneFocus = true;
			return canUseRedstoneFocus;
		}
		Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier attr : d)
        {
            baseWeaponDamage = (int) attr.getAmount();
            if (baseWeaponDamage >= 1) {
                canUseRedstoneFocus = true;
                break;
            }
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
				if (preparedSpellNumber == 0) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_NUKE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 1) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_HEAL, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 2) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_DOT, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 3) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_SDOT, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 4) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_RESI, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 5) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_TELE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 6) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_BUFF, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (preparedSpellNumber == 7) {
					doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.REDSTONEMAGIC_RCRS, SoundCategory.WEATHER, 0.3f + soundModifier, 0.0f + soundModifier);
				} 
			}
		} else {
			if ( castingDuration %20 == 0) {
				doPlayTickSpellSound(serverPlayer, serverWorld, ModSounds.SPELL_RESONATES, SoundCategory.BLOCKS, 0.3f, 0.14f);
			}
		}
			
	}

	private void doPlayTickSpellSound(ServerPlayerEntity serverPlayer, ServerWorld serverWorld, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
		serverWorld.playSound(null, serverPlayer.getPosition(), soundEvent, soundCategory, volume, pitch);
	}


	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	@Override
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
			CompoundNBT compoundnbt = stack.getOrCreateTag();	
			compoundnbt.putLong("spellCastingStartTime", SPELL_NOT_CASTING);
			
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

				SoundEvent soundEvent = ModSounds.SPELL_FAILS; 
				// casting a spell
				long netSpellCastingTime = (((stack.getUseDuration() - timeLeft) + 5) / 10);

				int preparedSpellNumber = compoundnbt.getInt("preparedSpellNumber");
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(preparedSpellNumber));

				int minimumCastingTime = 1;
				if (spell.getSpellTranslationKey().equals("redstonemagic.tele")) {
					minimumCastingTime = 4;
				}

				if (netSpellCastingTime < minimumCastingTime) {
					if (RedstoneMagicGuiEvent.getFizzleSpamLimiter() < 0 ) {
						RedstoneMagicGuiEvent.setFizzleSpamLimiter(120);
						TextComponent msg = new TranslationTextComponent("redstonemagic.fizz");
						MyConfig.sendChat(clientPlayer, msg.getString(),
								Color.fromTextFormatting((TextFormatting.RED)));
						clientPlayer.world.playSound(clientPlayer, clientPlayer.getPosition(), soundEvent, SoundCategory.AMBIENT, 0.7f,
								0.3f);
					}
				} else {

					float volume = 0.4f + (0.1f * netSpellCastingTime);
					float pitch = 0.3f;

					Entity targetEntity = null;
					BlockPos targetPos = null;
					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_OTHER)) {
						targetEntity = doLookForDistantTarget(clientPlayer);
						if (targetEntity != null) {
							soundEvent = SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH;
						}
					}

					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_BOTH)) {
						targetEntity = doLookForDistantTarget(clientPlayer);
						if (targetEntity == null) {
							targetEntity = clientPlayer;
						}
						targetPos = doLookForDistantBlock(clientPlayer);
						soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
					}

					if (spell.getSpellTargetType().equals(SpellManager.SPELL_TARGET_SELF)) {
						targetEntity = clientPlayer;
						targetPos = targetEntity.getPosition();
						soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
					}

					if (targetEntity != null) {
						clientPlayer.world.playSound(null, targetEntity.getPosition(), soundEvent, SoundCategory.PLAYERS,
								volume, pitch);
						if (targetPos != null) {
						Network.sendToServer(new RedstoneMagicPacket(preparedSpellNumber,targetEntity.getEntityId(),
								(int) netSpellCastingTime, handIndex, targetPos.getX(), targetPos.getY(), targetPos.getZ()) );
						} else
						{
							Network.sendToServer(new RedstoneMagicPacket(preparedSpellNumber,targetEntity.getEntityId(),
									(int) netSpellCastingTime, handIndex, -1, -99999, -1) );
						}
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
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		if ((entityIn instanceof ServerPlayerEntity)) {
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityIn;
			if ((int) (worldIn.getGameTime())%300 == 0) { // every 15 seconds.
				IMagicStorage playerManaStorage = serverPlayer.getCapability(CapabilityMagic.MAGIC).orElse(null);
				int manaLevel = playerManaStorage.getManaStored();
				if (manaLevel < 20) {
					int maxMana = MyConfig.getMaxPlayerRedstoneMagic();
					if (maxMana < 1 ) maxMana = 300;
					int manaLevelPercent = (100 * manaLevel) / maxMana; 
					if (manaLevelPercent <= 2) {
						playerManaStorage.addMana(1);
						Network.sendToClient(new SyncClientManaPacket(playerManaStorage.getManaStored(), NO_CHUNK_MANA_UPDATE),
								serverPlayer);
					}
				}
				
			}
		} else { // client side - update gui.
			PlayerEntity p = (PlayerEntity) entityIn;

			if ((long) (worldIn.getGameTime())%5 == 0) {

				ItemStack mainHand = p.getHeldItemMainhand();
				if (mainHand.getItem() instanceof RedstoneFocusItem) {
					CompoundNBT compoundnbt = mainHand.getOrCreateTag();
					int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
						? compoundnbt.getInt("preparedSpellNumber")
						: 0;
						if (MyConfig.getDebugLevel() > 1) {
							System.out.println("main hand prepared spell:");
						}
					RedstoneMagicGuiEvent.setPreparedSpellNumber(preparedSpellNumber);
//					Network.sendToClient(new SyncClientGuiPacket(preparedSpellNumber,-1), serverPlayer);

				} else {
					ItemStack offHand = p.getHeldItemOffhand();
					CompoundNBT compoundnbt = offHand.getOrCreateTag();
					int preparedSpellNumber = compoundnbt != null && compoundnbt.contains("preparedSpellNumber", NBT_NUMBER_FIELD)
						? compoundnbt.getInt("preparedSpellNumber")
						: 0;
					RedstoneMagicGuiEvent.setPreparedSpellNumber(preparedSpellNumber);
//					Network.sendToClient(new SyncClientGuiPacket(preparedSpellNumber,-1), serverPlayer);

				}
			}

		}

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
