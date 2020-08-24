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
import com.mactso.redstonemagic.spells.CastSpells;
import com.mactso.redstonemagic.util.helpers.KeyboardHelper;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RedstoneFocusItem extends ShieldItem {

	public static int NBT_NUMBER_FIELD = 99;
	private final int damageReduceAmount;
	private static float soundModifier = 0.3f;
	private static int tickSound = 0;
	private static int soundSpellNumber = 0;
	private static boolean castingASpell = false;
	private static boolean fullyCharged = false;
//	private static final IParticleData HARM_PARTICLE_DATA = new BlockParticleData(ParticleTypes.BLOCK, Blocks.BRICKS.getDefaultState()),
//	HEAL_PARTICLE_DATA = new BlockParticleData(ParticleTypes.BLOCK, Blocks.RED_CONCRETE.getDefaultState());

	@Override
	public boolean isShield(ItemStack stack, LivingEntity entity) {
		return true;
	}
	

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (amount > 2) {
			amount = 2;
		}
		return super.damageItem(stack, amount, entity, onBroken);
	}

	@Override
	public boolean isDamageable() {
		return true;
	}

	public RedstoneFocusItem(Properties builder) {
		super(builder);
		damageReduceAmount = 1;
		// TODO Auto-generated constructor stub
	}

//	private void getDamageReduceAmount() {
//
//	}

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

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

		if (entityLiving == null) {
			MyConfig.dbgPrintln(1, "onPlayerStoppedUsing: entityNull");
			return;
		}
		if (entityLiving instanceof ServerPlayerEntity) {
			MyConfig.dbgPrintln(1, "onPlayerStoppedUsing: servercall");
			return;
		}
		
		int baseWeaponDamage = 0;
		boolean handNotFocusOrWeaponOrEmpty = true;
		ItemStack handItem = entityLiving.getHeldItemMainhand();
		if (handItem.getItem() instanceof RedstoneFocusItem) {
			handNotFocusOrWeaponOrEmpty = false;
		}else
		if (handItem != null) {
			Collection<AttributeModifier> d = handItem.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE);
			while((d.iterator().hasNext()) && (baseWeaponDamage == 0)) {
				baseWeaponDamage = (int) d.iterator().next().getAmount();
				if (baseWeaponDamage > 1) {
					handNotFocusOrWeaponOrEmpty = false;
				}
			}
		} else {
			handNotFocusOrWeaponOrEmpty = false;
		}

		if (handNotFocusOrWeaponOrEmpty) {
			castingASpell = false;
			soundModifier = 0.3f;
			return;
		}

		int spellState = 0;
		CompoundNBT compoundnbt = stack.getOrCreateTag();
		if (compoundnbt == null)
			return;
		if (compoundnbt.contains("spellState",  NBT_NUMBER_FIELD ))
			spellState = compoundnbt.getInt("spellState"); // 99 : a "number" (int, float, double, long)
		if (spellState == 0) {
			MyConfig.dbgPrintln(1, "Not casting a spell.  do nothing.");
			return; // not casting a spell. not preparing a spell.
		}

		PlayerEntity player = (PlayerEntity) entityLiving;
		if (spellState == 1) { // just prepared a spell. TODO move chat message here.
			compoundnbt.putInt("spellState", 0); // not casting a spell.
			MyConfig.dbgPrintln(1,
					player.getName().toString() + "prepared spell: " + RedstoneMagicGuiEvent.getSpellPrepared());
			return;
		}
		// spellState == 2.. casting a spell.

		long netSpellCastingTime = (((stack.getUseDuration() - timeLeft) + 5) / 10);

		float soundVolumeModifier = 0.1f * netSpellCastingTime;

		if (player == null) {
			MyConfig.dbgPrintln(2, "playerEntity null on PlayerStoppedUsingCall.");
			return; // impossible error.
		}

		if (netSpellCastingTime == 0) {
			MyConfig.sendChat(player, "Your spell fizzled.  Cast slower.", Color.func_240744_a_(TextFormatting.RED));

			player.world.playSound(player, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO,SoundCategory.AMBIENT, 0.7f, 0.3f);
			castingASpell = false;
			soundModifier = 0.3f;
			RedstoneMagicGuiEvent.setSpellBeingCast("");
			RedstoneMagicGuiEvent.setCastTime(0);
			return;
		}

		int spellNumber = 0;
		if (compoundnbt == null) {
			castingASpell = false;
			soundModifier = 0.3f;
			System.out.println("no compound nbt. 'impossible' error. ");
			return; // "impossible" error.
		}
		
		if (compoundnbt.contains("spellKeyNumber", 99))
			spellNumber = compoundnbt.getInt("spellKeyNumber"); // 99 : a "number" (int, float, double, long)
		System.out.println ("OnStopUsing NBT spellNumber = " + spellNumber);
		if (spellNumber == 6) {
			boolean multiBuffbug = true;
		}
//    	playerEntity.world.playSound(playerEntity, playerEntity.getPosition(),SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.AMBIENT, 0.6f, 0.5f);
		RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
		String spellTargetType = spell.getSpellTargetType();
		Entity targetEntity = null;
		if (spellTargetType != "S") {
			targetEntity = CastSpells.lookForDistantTarget(player);
		}
		// spell "T"arget, "B"oth self or target, "S"elf only.
		SoundEvent soundEvent = null;
		Entity soundEntity = targetEntity;
		float volume = 0.4f + soundVolumeModifier;
		float pitch = 0.3f;
		boolean targetSpellHit = true;
		if (spellTargetType.equals(("T"))) {
			if (targetEntity == null) {
				targetEntity = player;
				MyConfig.sendChat(player, "Your spell missed.  SpellNumber is " + spellNumber, Color.func_240744_a_(TextFormatting.RED));
				targetSpellHit = false;
				soundEvent = SoundEvents.BLOCK_NOTE_BLOCK_SNARE;
				player.world.playSound(player, targetEntity.getPosition(), SoundEvents.ENTITY_CAT_HISS,
						SoundCategory.AMBIENT, volume / 2, pitch);
			} else {
				soundEvent = SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH;
			}
		}
		if (spellTargetType.equals("S")) {
			soundEvent = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
			targetEntity = player;
			pitch += 0.1F;
			volume = volume + soundVolumeModifier;
		}
		if (spellTargetType.equals("B")) {
			soundEvent = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
			if (targetEntity == null)
				targetEntity = player;
		}

		player.world.playSound(player, targetEntity.getPosition(), soundEvent, SoundCategory.AMBIENT, volume, pitch);
		if (targetSpellHit) {
			System.out.println ("OnStopUsing Send spellNumber = " + spellNumber);
			Network.sendToServer(
					new RedstoneMagicPacket(spellNumber, targetEntity.getEntityId(), (int) netSpellCastingTime));
		}
		RedstoneMagicGuiEvent.setCastTime((long) 0);
		RedstoneMagicGuiEvent.setSpellBeingCast("");
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
		castingASpell = false;
		soundModifier = 0.3f;

	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		if (castingASpell) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

		if (castingASpell) {
//			System.out.println( "castingASpell.  ticksound:" + tickSound);
			playCastingTickSounds(player);
		}
		super.onUsingTick(stack, player, count);
	}

	
	private void playCastingTickSounds(LivingEntity player) {

		if (!(player instanceof ServerPlayerEntity)) {
			return;
		}
			
		ServerPlayerEntity spE = (ServerPlayerEntity) player;
		ServerWorld serverWorld = (ServerWorld) spE.getServerWorld();	
		if (soundModifier < 0.9f) {
			soundModifier = soundModifier + 0.025f;
	//		System.out.println( "server castingASpell.  soundModifier:" + soundModifier);
			tickSound = (tickSound + 1) % 3;

			if (tickSound == 0) {
				if (soundModifier > 0.7f) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 0.3f, 0.14f);
				}
				System.out.println( "server castingASpell.  soundSpellNumber:" + soundSpellNumber);
				if (soundSpellNumber == 0) {
					playTickSpellSound(spE, serverWorld, SoundEvents.ENTITY_CAT_HISS, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 1) {
					playTickSpellSound(spE, serverWorld, SoundEvents.ENTITY_FOX_AMBIENT, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 2) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 3) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 4) {
					playTickSpellSound(spE, serverWorld, SoundEvents.ENTITY_COD_DEATH, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 5) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 6) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 7) {
					playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.WEATHER, 0.3f + soundModifier, 0.0f + soundModifier);
				} 
			}
		} else {
			if (!(fullyCharged)) {
				fullyCharged = true;
				tickSound = 19;
			}
			tickSound = (tickSound + 1) % 20;
			if (tickSound == 0) {
				playTickSpellSound(spE, serverWorld, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 0.3f, 0.14f);
			}
		}
	}



	private void playTickSpellSound(ServerPlayerEntity spE, ServerWorld serverWorld, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
		serverWorld.playSound(null, spE.getPosition(), soundEvent, soundCategory, volume, pitch);
		//System.out.println( "max spell sound created.");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

		if (!(playerIn instanceof ServerPlayerEntity)) {
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

			if ((canUseRedstoneFocus)) {
				MyConfig.dbgPrintln(1, "onItemRightClick: clientcall");
				ItemStack itemstack = playerIn.getHeldItem(handIn);
				CompoundNBT compoundnbt = itemstack.getOrCreateTag();
				int spellNumber = compoundnbt != null && compoundnbt.contains("spellKeyNumber", NBT_NUMBER_FIELD)
						? compoundnbt.getInt("spellKeyNumber")
						: 0;
				if (spellNumber == 6) {
					boolean multiBuffbug = true;
				}
				int spellState = compoundnbt != null && compoundnbt.contains("spellState", NBT_NUMBER_FIELD)
						? compoundnbt.getInt("spellState")
						: 0;
				if (KeyboardHelper.isHoldingShift()) { // change to a new spell.
					float headPitch = playerIn.rotationPitch;
					if (headPitch <= -0.1) { // looking up - go backwards thru list.
						System.out.println ("Change Spellnumber - From: " + spellNumber);	
						spellNumber = (spellNumber + 7) % 8;
						System.out.println ("Change Spellnumber - to: " + spellNumber);	
					} else { // looking straight - go forwards thru list
						System.out.println ("Change Spellnumber + From: " + spellNumber);	
						spellNumber = (spellNumber + 1) % 8;
						System.out.println ("Change Spellnumber + to: " + spellNumber);	
					}
					castingASpell = false;
					RedstoneMagicGuiEvent.setCastTime(0);
					RedstoneMagicGuiEvent.setSpellBeingCast("");
					compoundnbt.putInt("spellKeyNumber", spellNumber);
					if (spellNumber == 6) {
						boolean multiBuffbug = true;
					}
					spellState = 1;
					compoundnbt.putInt("spellState", spellState);

					RedstoneMagicSpellItem spell = SpellManager
							.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
					String spellComment = spell.getSpellComment();
					RedstoneMagicGuiEvent.setSpellPrepared(spellComment);
					// MyConfig.sendChat(playerIn, "You switch to " + spell.getSpellComment() + ".",
					// Color.func_240744_a_(TextFormatting.DARK_RED));
				} else { // start casting current spell
					soundModifier = 0.29f;
					tickSound = 0;
					castingASpell = true;
					fullyCharged = false;
					if (spellNumber == 6) {
						boolean multiBuffbug = true;
					}					
					soundSpellNumber = spellNumber;
					System.out.println ("Start Casting Spellnumber " + spellNumber);	
					RedstoneMagicGuiEvent.setCastTime(playerIn.world.getGameTime());
					RedstoneMagicSpellItem spell = SpellManager
							.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
					spellState = 2;
					compoundnbt.putInt("spellState", spellState); // currently casting a spell.
					// MyConfig.sendChat (playerIn,"You begin casting "+ spell.getSpellComment() +
					// ".",Color.func_240744_a_(TextFormatting.DARK_RED));

					RedstoneMagicGuiEvent.setSpellBeingCast(spell.getSpellComment());
				}

			} else {
				MyConfig.dbgPrintln(1, "player serverside call" + RedstoneMagicGuiEvent.getCastTime());
			}

		}

		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	public int getItemEnchantability() {
		return 1;
	}

	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	public void spawnDamageParticles(Entity entity, int particleCount, IParticleData particleType) {
		if (!(entity.world.isRemote())) {
			for (int i = 0; i < particleCount; ++i) {
				double posX = entity.getPosX();
				double posY = entity.getPosY();
				double posZ = entity.getPosZ();
				double motionX = entity.world.rand.nextGaussian() * 0.02D;
				double motionY = entity.world.rand.nextGaussian() * 0.02D;
				double motionZ = entity.world.rand.nextGaussian() * 0.02D;
				double PosXWidth = entity.getPosXWidth(1.0D);
				double PosYWidth = PosXWidth;
				double PosZWidth = entity.getPosZWidth(1.0D);
				entity.world.addParticle(particleType, posX + 0.5D + motionX, posY + 0.5D + motionY,
						posZ + 0.5D + motionZ, motionX, motionY, motionZ);
			}
		}

	}
}
