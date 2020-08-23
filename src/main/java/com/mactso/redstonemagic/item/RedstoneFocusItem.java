package com.mactso.redstonemagic.item;

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
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.Color;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.particles.BasicParticleType;

public class RedstoneFocusItem extends ShieldItem {

	public static int NBT_NUMBER_FIELD = 99;
	private final int damageReduceAmount;
	private static float soundModifier = 0.3f;
	private static int tickSound = 0;
	private static int soundSpellNumber = 0;
	private static boolean castingASpell = false;
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
		int spellNumberKey = compoundnbt != null && compoundnbt.contains("spellNumber", NBT_NUMBER_FIELD)
				? compoundnbt.getInt("spellNumber")
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
		int spellState = 0;
		CompoundNBT compoundnbt = stack.getOrCreateTag();
		if (compoundnbt == null)
			return;
		if (compoundnbt.contains("spellState", 99))
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
			player.world.playSound(player, player.getPosition(), SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE,
					SoundCategory.AMBIENT, 0.7f, 0.3f);
			return;
		}

		int spellNumber = 0;
		if (compoundnbt == null)
			return; // impossible error.
		if (compoundnbt.contains("spellNumber", 99))
			spellNumber = compoundnbt.getInt("spellNumber"); // 99 : a "number" (int, float, double, long)

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
				MyConfig.sendChat(player, "Your spell missed.", Color.func_240744_a_(TextFormatting.RED));
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
		}
		return super.hasEffect(stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

		if (castingASpell) {
			System.out.println( "castingASpell.  ticksound:" + tickSound);
			playCastingTickSounds(player);
		}
		super.onUsingTick(stack, player, count);
	}

	private void playCastingTickSounds(LivingEntity player) {

		PlayerEntity pE = (PlayerEntity) player;
		if (soundModifier < 0.9f) {
			System.out.println( "castingASpell.  soundModifier:" + soundModifier);
			tickSound = (tickSound + 1) % 6;
			if (tickSound == 0) {
				System.out.println( "castingASpell.  soundSpellNumber:" + soundSpellNumber);
				soundModifier = soundModifier + 0.015f;
				if (soundSpellNumber == 0) {
					System.out.println( "castingASpell.  playing Redbolt Sound.");
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.ENTITY_CAT_HISS, SoundCategory.HOSTILE,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 1) {
					System.out.println( "castingASpell.  playing Crimson HealSound.");
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.ENTITY_FOX_AMBIENT, SoundCategory.AMBIENT,
							0.3f + soundModifier, 0.3f + soundModifier);
				} else if (soundSpellNumber == 2) {
					System.out.println( "castingASpell.  playing Sepsis Sound.");
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH,
							SoundCategory.AMBIENT, 0.3f + soundModifier, 0.4f + soundModifier);
				} else if (soundSpellNumber == 3) {
					System.out.println( "castingASpell.  playing Crimson Cloud Sound.");
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH,
							SoundCategory.AMBIENT, 0.2f + soundModifier, 0.01f + soundModifier);
				} else if (soundSpellNumber == 4) {
					System.out.println( "castingASpell.  playing spell 4 Sound.");
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BANJO,
							SoundCategory.AMBIENT, 0.3f + soundModifier, 0.2f + soundModifier);
				} else if (soundSpellNumber == 5) {
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE,
							SoundCategory.AMBIENT, 0.3f + soundModifier, 0.2f + soundModifier);
				} else if (soundSpellNumber == 6) {
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_WOOL_PLACE,
							SoundCategory.AMBIENT, 0.3f + soundModifier, 0.2f + soundModifier);
				} else if (soundSpellNumber == 7) {
					pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_BASALT_BREAK,
							SoundCategory.AMBIENT, 0.3f + soundModifier, 0.2f + soundModifier);
				} // TODO assign sounds for other spells- possibly in array or hashtable.
			}
		} else {
			tickSound = (tickSound + 1) % 20;
			System.out.println( "max spell sound tickSound=" + tickSound);
			if (tickSound == 0) {
				pE.world.playSound(pE, pE.getPosition(), SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.WEATHER,
						0.4f, 0.14f);
				System.out.println( "max spell sound created.");
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

		if (!(playerIn instanceof ServerPlayerEntity)) {
			MyConfig.dbgPrintln(1, "onItemRightClick: clientcall");
			ItemStack itemstack = playerIn.getHeldItem(handIn);
			CompoundNBT compoundnbt = itemstack.getOrCreateTag();
			int spellNumber = compoundnbt != null && compoundnbt.contains("spellNumber", 99)
					? compoundnbt.getInt("spellNumber")
					: 0;
			int spellState = compoundnbt != null && compoundnbt.contains("spellState", 99)
					? compoundnbt.getInt("spellState")
					: 0;
			if (KeyboardHelper.isHoldingShift()) { // change to a new spell.
				float headPitch = playerIn.rotationPitch;
				if (headPitch <= -0.1) { // looking up - go backwards thru list.
					spellNumber = (spellNumber + 7) % 8;
				} else { // looking straight - go forwards thru list
					spellNumber = (spellNumber + 1) % 8;
				}
				castingASpell = false;
				RedstoneMagicGuiEvent.setCastTime(0);	
				RedstoneMagicGuiEvent.setSpellBeingCast("");
				compoundnbt.putInt("spellNumber", spellNumber);
				spellState = 1;
				compoundnbt.putInt("spellState", spellState);
				
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
				String spellComment = spell.getSpellComment();
				RedstoneMagicGuiEvent.setSpellPrepared(spellComment);
				//MyConfig.sendChat(playerIn, "You switch to " + spell.getSpellComment() + ".", Color.func_240744_a_(TextFormatting.DARK_RED));
			} else { // start casting current spell
				soundModifier = 0.29f;
				tickSound = 0;
				castingASpell = true;
				soundSpellNumber = spellNumber;
				RedstoneMagicGuiEvent.setCastTime(playerIn.world.getGameTime());				
				RedstoneMagicSpellItem spell = SpellManager.getRedstoneMagicSpellItem(Integer.toString(spellNumber));
				spellState = 2;
				compoundnbt.putInt("spellState", spellState); // currently casting a spell.
				// MyConfig.sendChat (playerIn,"You begin casting "+ spell.getSpellComment() +
				// ".",Color.func_240744_a_(TextFormatting.DARK_RED));

				RedstoneMagicGuiEvent.setSpellBeingCast(spell.getSpellComment());
			}

		} else {
			MyConfig.dbgPrintln(1, "player serverside call" + RedstoneMagicGuiEvent.getCastTime());
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
