package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MelonBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

public class RitualPylonTileEntity extends BlockEntity {
	static final int RITUAL_PLAYER_COST = 5;
	static final int RITUAL_CHUNK_COST = 16;
	static final int RITUAL_REDSTONE_COST = 256;
	static final int RITUAL_NONE = -1;
	static final int RITUAL_TESTING = 90;
	static final int RITUAL_FARMING = 91;
	static final int RITUAL_MINING = 92;
	static final int RITUAL_LOGGING = 93;
	static final int RITUAL_LIGHTING_TORCH = 94;
	static final int RITUAL_LIGHTING_LANTERN = 95;
	static final int RITUAL_CLEARING = 96;
	static final int RITUAL_REDSTONE = 98;
	static final int RITUAL_BUILDING = 99;
	
	static final int RITUAL_WARMUP_TIME = 100; // 5 seconds
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	static final ItemStack REDSTONE_BLOCK_STACK = new ItemStack(Items.REDSTONE_BLOCK, 1);

	int ritualSpeed = 0;
	Item activationItem = null;
	int activationToolTier;
	int currentRitual = RITUAL_NONE;
	float harvestWorkTotal = 0.0f;

	int minRitualX = 0;
	int minRitualZ = 0;
	int cursorRitualX = 0;
	int cursorRitualY = 0;
	int cursorRitualZ = 0;
	float luck = 0.0f;

	boolean mustPayChunkCost = false;
	int timeRitualWarmup = 0;
	int timeRitualCooldown = 0;

	int mine[] = { 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1 };

	public RitualPylonTileEntity(BlockPos pos, BlockState state) {
		super(ModTileEntities.RITUAL_PYLON, pos, state);
	}

	private int calcChunkRitualManaCost() {
		int newRitualChunkCost = RITUAL_CHUNK_COST;
		if (worldPosition.getY() < 22) {
			newRitualChunkCost += RITUAL_CHUNK_COST;
		}
		if (worldPosition.getY() < 11) {
			newRitualChunkCost += RITUAL_CHUNK_COST;
		}
		if (currentRitual == RITUAL_REDSTONE) {
			newRitualChunkCost = RITUAL_REDSTONE_COST;
			
			if (this.activationItem == Items.REDSTONE_ORE)
			newRitualChunkCost = RITUAL_REDSTONE_COST * 4;

		}
		return newRitualChunkCost;
	}

	private int calcPlayerRitualManaCost() {
		int newRitualCost = RITUAL_PLAYER_COST;
		if (worldPosition.getY() < 22) {
			newRitualCost += RITUAL_PLAYER_COST;
		}
		if (worldPosition.getY() < 11) {
			newRitualCost += RITUAL_PLAYER_COST;
		}
		return newRitualCost;
	}

	public void calcRitualArea() {

		BlockPos eastPos = null;
		BlockPos westPos = null;

		for (int i = 1; i < 16; i++) {
			if (level.getBlockState(worldPosition.east(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualX = minRitualX = worldPosition.east(i).getX() - 15;
				eastPos = worldPosition.east(i);
			} else {
				break;
			}
		}
		if (eastPos == null) {
			for (int i = 1; i < 16; i++) {
				if (level.getBlockState(worldPosition.west(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualX = minRitualX = worldPosition.west(i).getX();
					westPos = worldPosition.west(i + 1);
				} else {
					break;
				}
			}
		}
		if (eastPos == null && westPos == null) {
			cursorRitualX = minRitualX = level.getChunk(worldPosition).getPos().getMinBlockX();
			if (minRitualX >0 ) {
				minRitualX--;
				cursorRitualX--;
			}
		}

		BlockPos southPos = null;
		BlockPos northPos = null;

		for (int i = 1; i < 16; i++) {
			if (level.getBlockState(worldPosition.south(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualZ = minRitualZ = worldPosition.south(i).getZ() - 15;
				southPos = worldPosition.south(i);
			} else {
				break;
			}
		}
		if (southPos == null) {
			for (int i = 1; i < 16; i++) {
				if (level.getBlockState(worldPosition.north(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualZ = minRitualZ = worldPosition.north(i).getZ();
					northPos = worldPosition.north(i + 1);
				} else {
					break;
				}
			}
		}
		if (southPos == null && northPos == null) {
			cursorRitualZ = minRitualZ = level.getChunk(worldPosition).getPos().getMinBlockZ()-1;
			if (minRitualZ >0 ) {
				minRitualZ--;
				cursorRitualZ--;
			}
		}

		cursorRitualY = worldPosition.getY();

	}

	public void utilCreateNonBasicParticle(BlockPos pos, int particleCount, ParticleOptions particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerLevel) level).sendParticles(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}

	public LootContext.Builder utilDoGetLootBuilder(BlockPos cursorPos) {
		LootContext.Builder builder = (new LootContext.Builder((ServerLevel) level)).withRandom(level.random)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(cursorPos))
				.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
				.withOptionalParameter(LootContextParams.THIS_ENTITY, null)
				.withOptionalParameter(LootContextParams.BLOCK_ENTITY, this);
		return builder;
	}

	public boolean doRitualPylonInteraction(Player player, InteractionHand handIn) {

		if ((timeRitualWarmup > 0) || (timeRitualCooldown > 0)) {
			return false;
		}

		if (IsRitualRunning()) {
			processEndRitual();
			return false;
		}

		if (player instanceof ServerPlayer) {

			ServerPlayer serverPlayer = (ServerPlayer) player;
			ItemStack handItemStack = player.getItemInHand(handIn);

			this.activationToolTier = Tiers.WOOD.getLevel();
			if (handItemStack.getItem() instanceof TieredItem) {
				TieredItem ti = (TieredItem) handItemStack.getItem();
				this.activationToolTier = ti.getTier().getLevel();
			}

			if (getRitualID(handItemStack.getItem()) == RITUAL_NONE) {
				return false;
			}

			this.activationItem = handItemStack.getItem();
			Item newRitualItem = handItemStack.getItem();	
			this.currentRitual = getRitualID(newRitualItem);

			IMagicStorage playerManaStorage = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
			LevelChunk chunk = (LevelChunk) level.getChunk(worldPosition);
			IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);

			if (playerManaStorage == null) {
				MyConfig.sendChat(player, "Impossible Error: You do not have a mana pool.",
						ChatFormatting.YELLOW);
				this.currentRitual = RITUAL_NONE;
				return false;
			}

			if ((playerManaStorage.getManaStored() < calcPlayerRitualManaCost())
					|| (chunkManaStorage.getManaStored() < calcChunkRitualManaCost())) {
				this.currentRitual = RITUAL_NONE;
				return false;
			}

			if ((useRitualItem(player, handItemStack)) == false) { // damages or destroys
				this.currentRitual = RITUAL_NONE;
				return false;
			}

			playerManaStorage.useMana(calcPlayerRitualManaCost());
			chunkManaStorage.useMana(calcChunkRitualManaCost());

			Network.sendToClient(
					new SyncClientManaPacket(playerManaStorage.getManaStored(), chunkManaStorage.getManaStored()),
					serverPlayer);

			chunk.setUnsaved(true);

			ritualSpeed = getRitualSpeed(newRitualItem);
			harvestWorkTotal = 0.0f;
			mustPayChunkCost = false;
			luck = player.getLuck();
			level.playSound(null, worldPosition, ModSounds.RITUAL_BEGINS, SoundSource.BLOCKS, 0.5f, 0.2f);
			if (getRitualID(handItemStack.getItem()) == RITUAL_REDSTONE) {
				player.hurt(player.level.damageSources().generic(), 19.0f);
				level.playSound(null, worldPosition, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 0.5f, 0.2f);
			}
			calcRitualArea();
			timeRitualWarmup = 40;
			return true;
		}

		return false;
	}

	private int getRitualHeight(int ritualID) {

		if (ritualID == RITUAL_TESTING) {
			return 2;
		} else if (ritualID == RITUAL_CLEARING) {
			if (this.activationToolTier == Tiers.NETHERITE.getLevel()) {
				return 3;
			}
			if (this.activationToolTier == Tiers.DIAMOND.getLevel()) {
				return 2;
			}
			return 1;
		} else if (ritualID == RITUAL_FARMING) {
			return 0;
		} else if (ritualID == RITUAL_MINING) {
			return 3;
		} else if (ritualID == RITUAL_LOGGING) {
			return 24;
		} else if (ritualID == RITUAL_LIGHTING_TORCH) {
			return 0;
		} else if (ritualID == RITUAL_LIGHTING_LANTERN) {
			return 1;
		}
		return 0;
	}

	private int getRitualID(Item ritualItem) {
		if (ritualItem instanceof SwordItem) {
			return RITUAL_TESTING;
		} else if (ritualItem == Items.NETHER_STAR) {
			return RITUAL_REDSTONE;
		} else if (ritualItem == Items.REDSTONE_ORE) {
			return RITUAL_REDSTONE;
		} else if (ritualItem instanceof ShovelItem) {
			return RITUAL_CLEARING;
		} else if (ritualItem instanceof HoeItem) {
			return RITUAL_FARMING;
		} else if (ritualItem instanceof PickaxeItem) {
			return RITUAL_MINING;
		} else if (ritualItem instanceof AxeItem) {
			return RITUAL_LOGGING;
		} else if (ritualItem == Items.TORCH) {
			return RITUAL_LIGHTING_TORCH;
		} else if (ritualItem == Items.LANTERN) {
			return RITUAL_LIGHTING_LANTERN;
		}
		return RITUAL_NONE;
	}

	@SuppressWarnings("deprecation")
	private int getRitualSpeed(Item ritualItem) {

		int newRitualSpeed;

		if (getRitualID(ritualItem) == RITUAL_LIGHTING_TORCH) {
			newRitualSpeed = 54;
		} else if (getRitualID(ritualItem) == RITUAL_LIGHTING_LANTERN) {
			newRitualSpeed = 33;
		} else {
			newRitualSpeed = 54 - (int) Math.sqrt(100 + ritualItem.getMaxDamage());
		}
		if (newRitualSpeed < 8) {
			newRitualSpeed = 8;
		}
		return newRitualSpeed;
	}

	private boolean isClearable(BlockPos cursorPos) {
		BlockState bS = level.getBlockState(cursorPos);
		if (this.activationToolTier == Tiers.NETHERITE.getLevel()) {
			if (bS.getBlock() == Blocks.SOUL_SAND) return true;
			if (bS.getBlock() == Blocks.SOUL_SOIL) return true;
		}

		return (
				(bS.is(BlockTags.FLOWERS)) ||
				(bS.getBlock() instanceof TallGrassBlock) ||
				(bS.getBlock() instanceof DoublePlantBlock) ||
				(bS.is(BlockTags.DIRT)) ||
				(bS.is(Tags.Blocks.GRAVEL)) ||
				(bS.is(BlockTags.SAND))
				);
	}

	private boolean isItemDamagingRitual(Item ritualItem) {
		if (getRitualID(ritualItem) == RITUAL_TESTING) {
			return false;
		} if (getRitualID(ritualItem) == RITUAL_REDSTONE) {
			return false;
		} else if (getRitualID(ritualItem) == RITUAL_CLEARING) {
			return true;
		} else if (getRitualID(ritualItem) == RITUAL_FARMING) {
			return true;
		} else if (getRitualID(ritualItem) == RITUAL_MINING) {
			return true;
		} else if (getRitualID(ritualItem) == RITUAL_LOGGING) {
			return true;
		} else if (getRitualID(ritualItem) == RITUAL_LIGHTING_TORCH) {
			return true;
		} else if (getRitualID(ritualItem) == RITUAL_LIGHTING_LANTERN) {
			return true;
		}
		return false;
	}

	private boolean isLightable(BlockPos cursorPos) {
		return (level.getBlockState(cursorPos).getBlock() instanceof AirBlock)
				&& (level.getBrightness(LightLayer.BLOCK, cursorPos) <= 8);
	}

	private boolean isLoggable(BlockPos cursorPos) {
		BlockState bS = level.getBlockState(cursorPos);

		if ((bS.is(BlockTags.LEAVES)) || (bS.is(BlockTags.LOGS))) {
			return true;
		}
		return false;
	}

	private boolean isMineable(BlockPos cursorPos) {
		BlockState bS = level.getBlockState(cursorPos);

		if (this.activationToolTier == Tiers.NETHERITE.getLevel()) {
			if (bS.getBlock() == Blocks.BLACKSTONE) return true;
			if (bS.getBlock() == Blocks.BASALT) return true;
			if (bS.getBlock() == Blocks.SOUL_SAND) return true;
			if (bS.getBlock() == Blocks.SOUL_SOIL) return true;
		}

		return (bS.is(Tags.Blocks.NETHERRACK)) || (bS.is(Tags.Blocks.STONE))
				|| (bS.is(BlockTags.DIRT))
				|| (bS.is(Tags.Blocks.GRAVEL)) || (bS.is(Tags.Blocks.SAND));
	}

	private boolean isReadyCropBlock(BlockPos cursorPos) {
		BlockState tBS = level.getBlockState(cursorPos);
		Block tBlock = level.getBlockState(cursorPos).getBlock();
		if (tBlock instanceof CropBlock) {
			CropBlock c = (CropBlock) tBlock;
			if (c.isMaxAge(tBS)) {
				return true;
			}
		} else if (tBlock instanceof MelonBlock) {
			return true;
		} else if (tBlock instanceof PumpkinBlock) {
			return true;
		} else if (tBlock instanceof SugarCaneBlock) {
			return true;
		}
		return false;
	}

	public boolean IsRitualRunning() {
		if (currentRitual != RITUAL_NONE) {
			return true;
		}
		return false;
	}

	private void processClearingRitual(BlockPos cursorPos) {
		BlockState tBS = level.getBlockState(cursorPos);
		if (isClearable(cursorPos)) {
			mustPayChunkCost = true;
			level.destroyBlock(cursorPos, false);
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
				if (chestInv != null) {
					HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
				}
			}
		}
	}

	private void processEndRitual() {
		currentRitual = RITUAL_NONE;
		timeRitualCooldown = 40;
		level.playSound(null, worldPosition, ModSounds.RITUAL_ENDS, SoundSource.BLOCKS, 0.5f, 0.2f);
	}

	private void processFarmingRitual(BlockPos cursorPos) {
		BlockState tBS = level.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (tBlock instanceof CropBlock) {
			CropBlock c = (CropBlock) tBlock;
			if (c.isMaxAge(tBS)) {
				level.playSound(null, cursorPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.5f, 0.2f);
				BlockState bC = c.defaultBlockState().setValue(c.getAgeProperty(), Integer.valueOf(0));
				level.setBlockAndUpdate(cursorPos, bC);
				mustPayChunkCost = true;

				for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
					if (dropsStack.getItem() instanceof ItemNameBlockItem) {
						int v = Math.max((dropsStack.getCount() - 2), 0);
						dropsStack.setCount(v);
						;
					}
					Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
					if (chestInv != null) {
						HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
					}
				}

			}
		} else if ((tBlock instanceof MelonBlock) || (tBlock instanceof PumpkinBlock)) {
			mustPayChunkCost = true;
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				if (dropsStack.getItem() instanceof ItemNameBlockItem) {
					int v = Math.max((dropsStack.getCount() - 2), 0);
					dropsStack.setCount(v);
					;
				}
				level.destroyBlock(cursorPos, false);
				Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
				if (chestInv != null) {
					HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
				}
			}
		} else if ((tBlock instanceof SugarCaneBlock)){
			mustPayChunkCost = true;
			for (int i = 2; i>0; i--) {
				BlockState sctBS = level.getBlockState(cursorPos.above(i));
				Block sctBlock = sctBS.getBlock();
				if (sctBlock instanceof SugarCaneBlock) {
					for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos.above(i)))) {
						if (dropsStack.getItem() instanceof ItemNameBlockItem) {
							int v = Math.max((dropsStack.getCount() - 2), 0);
							dropsStack.setCount(v);
							;
						}
						level.destroyBlock(cursorPos.above(i), false);
						Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
						if (chestInv != null) {
							HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
						}
					}
				}
			}
		}
	}

	private void processLightingRitual(BlockPos cursorPos) {
		if (isLightable(cursorPos)) {
			level.setBlockAndUpdate(cursorPos, ModBlocks.LIGHT_SPELL.defaultBlockState());
			level.playSound(null, cursorPos, ModSounds.REDSTONEMAGIC_LIGHT, SoundSource.BLOCKS, 0.7f, 0.86f);
			mustPayChunkCost = true;
		}
	}

	private void processLoggingRitual(BlockPos cursorPos) {
		BlockState tBS = level.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (tBS.is(BlockTags.LEAVES)) {
			level.destroyBlock(cursorPos, false);
			harvestWorkTotal += 0.1f;
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
				if (chestInv != null) {
					HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
				}
			}
		}
		if (tBS.is(BlockTags.LOGS)) {
			level.destroyBlock(cursorPos, false);
			harvestWorkTotal += 1.0f;
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
				if (chestInv != null) {
					HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
				}
			}
		}
		if (harvestWorkTotal > 64.0f) {
			harvestWorkTotal = 0.0f;
			mustPayChunkCost = true;
		}

	}

	private void processMiningRitual(BlockPos cursorPos) {
		BlockState tBS = level.getBlockState(cursorPos);
		if (isMineable(cursorPos)) {
			mustPayChunkCost = true;
			level.destroyBlock(cursorPos, false);
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				Container chestInv = HopperBlockEntity.getContainerAt(this.level, worldPosition.below());
				if (chestInv != null) {
					HopperBlockEntity.addItem(null, chestInv, dropsStack, null);
				}
			}
		}
	}

	private void processRitualCooldown() {
		((ServerLevel) level).sendParticles(ParticleTypes.WITCH, 0.5D + (double) this.worldPosition.getX(),
				(double) this.worldPosition.getY() + 0.7D, 0.5D + (double) this.worldPosition.getZ(),
				(int) timeRitualCooldown / 8, 0.0D, -0.1D, 0.0D, -0.04D);
		((ServerLevel) level).sendParticles(ParticleTypes.POOF, 0.5D + (double) this.worldPosition.getX(),
				(double) this.worldPosition.getY() + 0.10D, 0.5D + (double) this.worldPosition.getZ(),
				(int) timeRitualWarmup / 8, 0.0D, 0.1D, 0.0D, 0.04D);
		timeRitualCooldown--;
	}

	private void processRitualWarmup() {
		if (currentRitual == RITUAL_REDSTONE) {
			((ServerLevel) level).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, 0.5D + (double) this.worldPosition.getX(),
					(double) this.worldPosition.getY() + 0.10D, 0.5D + (double) this.worldPosition.getZ(),
					(int) timeRitualWarmup / 8, 0.0D, 0.1D, 0.0D, 0.04D);
		}
		((ServerLevel) level).sendParticles(ParticleTypes.WITCH, 0.5D + (double) this.worldPosition.getX(),
				(double) this.worldPosition.getY() + 0.10D, 0.5D + (double) this.worldPosition.getZ(),
				(int) timeRitualWarmup / 8, 0.0D, 0.1D, 0.0D, 0.04D);
		((ServerLevel) level).sendParticles(ParticleTypes.LAVA, 0.5D + (double) this.worldPosition.getX(),
				(double) this.worldPosition.getY() + 0.10D, 0.5D + (double) this.worldPosition.getZ(),
				(int) timeRitualWarmup / 8, 0.0D, 0.1D, 0.0D, 0.04D);
		timeRitualWarmup--;
	}

	@Override
	// restore state when chunk reloads
	public void load(CompoundTag nbt) {
		super.load(nbt);

		currentRitual = nbt.getInt("currentRitual");
		ritualSpeed = nbt.getInt("ritualSpeed");
		harvestWorkTotal = nbt.getInt("harvestWorkTotal");
		minRitualZ = nbt.getInt("minRitualZ");
		minRitualX = nbt.getInt("minRitualX");
		cursorRitualX = nbt.getInt("cursorRitualX");
		cursorRitualZ = nbt.getInt("cursorRitualZ");
		cursorRitualY = nbt.getInt("cursorRitualY");
	}

	@Override
	// save state when chunk unloads
	public void saveAdditional(CompoundTag compound) {

		super.saveAdditional(compound);
		compound.putInt("currentRitual", currentRitual);
		compound.putFloat("harvestWorkTotal", harvestWorkTotal);
		compound.putInt("ritualSpeed", ritualSpeed);
		compound.putInt("minRitualX", minRitualX);
		compound.putInt("minRitualZ", minRitualZ);
		compound.putInt("cursorRitualX", cursorRitualX);
		compound.putInt("cursorRitualZ", cursorRitualZ);
		compound.putInt("cursorRitualY", cursorRitualY);

		// if (currentRitual != null)
//			compound.putUniqueId("currentRitual", currentRitual);
	}

	public void serverTick() {

//		if (level != null && !(level instanceof ServerLevel)) {
//			return;
//		}

		if (timeRitualWarmup > 0) {
			processRitualWarmup();
			return;
		}
		if (timeRitualCooldown > 0) {
			processRitualCooldown();
			return;
		}

		if (IsRitualRunning()) {
			if (currentRitual == RITUAL_REDSTONE) {
				processRedstoneRitual();
				processEndRitual();
				return;
			}
			long speed = ritualSpeed;
			if (speed < 8)
				speed = 8L;
			if (this.level.getGameTime() % speed != 0L) {
				return;
			}
			long dayTime = level.getDayTime();
			float humVolume = (float) (0.05 + (0.04 * Math.sin((double) dayTime)));
			float humPitch = (float) (0.7 + (0.3 * Math.sin((double) dayTime)));

			level.playSound(null, worldPosition, ModSounds.RITUAL_PYLON_THRUMS, SoundSource.BLOCKS, humVolume / 3,
					humPitch);
			((ServerLevel) level).sendParticles(ParticleTypes.WITCH, 0.5D + (double) this.worldPosition.getX(),
					(double) this.worldPosition.getY() + 0.35D, 0.5D + (double) this.worldPosition.getZ(), 3, 0.0D,
					0.1D, 0.0D, -0.04D);

			BlockPos cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
			boolean noValidRitualBlockFound = true;
			boolean doMineGalleries = false;

			while (noValidRitualBlockFound) {
//				System.out.println("looping " +cursorRitualX +", " +cursorRitualZ);
				cursorRitualX++;
				if (cursorRitualX > minRitualX + 16) {
					cursorRitualX = minRitualX;
					cursorRitualZ++;
					if (cursorRitualZ > minRitualZ + 16) {
						cursorRitualZ = minRitualZ;
						cursorRitualY++;
						if (cursorRitualY > worldPosition.getY() + getRitualHeight(currentRitual)) {
							processEndRitual();
							return;
						}
						LevelChunk chunk = (LevelChunk) level.getChunk(worldPosition);
						IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
						if (mustPayChunkCost) {
							if (!(chunkManaStorage.useMana(16))) {
								level.playSound(null, worldPosition, ModSounds.SPELL_FAILS, SoundSource.BLOCKS, 0.5f,
										0.2f);
								level.playSound(null, worldPosition, SoundEvents.ENDERMITE_DEATH, SoundSource.BLOCKS,
										0.5f, 0.2f);
								processEndRitual();
								return;
							}
							mustPayChunkCost = false;
						}

					}
				}
				cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
				if (currentRitual == RITUAL_TESTING) {
					if (cursorRitualX == minRitualX +1 
							|| cursorRitualZ == minRitualZ +1 
							|| cursorRitualX == minRitualX + 15 
							|| cursorRitualZ == minRitualZ + 15) {
						((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, 1.5D + cursorRitualX, 0.35D + cursorRitualY,
								-0.5D + cursorRitualZ, (int) 1, 0.0D, 0.0D, 0.001D, 0.00D);
					}
				} else {
					((ServerLevel) level).sendParticles(ParticleTypes.POOF, 0.5D + cursorRitualX, 0.35D + cursorRitualY,
							0.5D + cursorRitualZ, 3, 0.0D, 0.1D, 0.0D, -0.04D);
					utilCreateNonBasicParticle(cursorPos, 1, new ItemParticleOption(ParticleTypes.ITEM, GLOWSTONE_STACK));
				}

				if ((currentRitual == RITUAL_LIGHTING_TORCH) || (currentRitual == RITUAL_LIGHTING_LANTERN)) {
					if (isLightable(cursorPos)) {
						noValidRitualBlockFound = false;
						if ((level.getRandom().nextFloat() * 100.0f) < 50.0f) {
							level.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundSource.BLOCKS, 0.5f,
									0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_FARMING) && isReadyCropBlock(cursorPos)) {
					noValidRitualBlockFound = false;
					if ((level.getRandom().nextFloat() * 100.0f) < 25.0f) {
						level.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundSource.BLOCKS, 0.5f, 0.2f);
					}
				} else if ((currentRitual == RITUAL_CLEARING) && isClearable(cursorPos)) {
					noValidRitualBlockFound = false;
					if ((level.getRandom().nextFloat() * 100.0f) < 15.0f) {
						level.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundSource.BLOCKS, 0.5f, 0.2f);
					}
				} else if ((currentRitual == RITUAL_MINING) && (isMineable(cursorPos))) {

					int x = Math.abs((cursorPos.getX()) % 16);
					if (cursorPos.getX() < 0) {
						x = 15 - x;
					}
					int z = Math.abs((cursorPos.getZ()) % 16);
					if (cursorPos.getZ() < 0) {
						z = 15 - z;
					}

					doMineGalleries = (mine[x] + mine[z]) > 0;
					if (doMineGalleries) {
						noValidRitualBlockFound = false;
						if ((level.getRandom().nextFloat() * 100.0f) < 25.0f) {
							level.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundSource.BLOCKS, 0.5f,
									0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_LOGGING) && (isLoggable(cursorPos))) {
					noValidRitualBlockFound = false;
					if ((level.getRandom().nextFloat() * 100.0f) < 25.0f) {
						level.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundSource.BLOCKS, 0.5f, 0.2f);
					}
				}
			}

			if (currentRitual != RITUAL_TESTING) {
				((ServerLevel) level).sendParticles(ParticleTypes.POOF, 0.5D + (double) minRitualX + cursorRitualX,
						(double) worldPosition.getY() + cursorRitualY + 0.10D,
						0.5D + (double) minRitualZ + cursorRitualZ, (int) 3, 0.0D, 0.1D, 0.0D, 0.04D);
			} else if (cursorRitualX == 0 || cursorRitualZ == 0 || cursorRitualX == 15 || cursorRitualZ == 15) {
				((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, 0.5D + (double) minRitualX + cursorRitualX,
						(double) worldPosition.getY() + cursorRitualY + 0.10D,
						0.5D + (double) minRitualZ + cursorRitualZ, (int) 1, 0.0D, 0.0D, 0.001D, 0.00D);

			}

			if (currentRitual == RITUAL_CLEARING) {
				processClearingRitual(cursorPos);
			} else if (currentRitual == RITUAL_FARMING) {
				processFarmingRitual(cursorPos);
			} else if (currentRitual == RITUAL_MINING) {
				if (doMineGalleries) {
					processMiningRitual(cursorPos);
				}
			} else if (currentRitual == RITUAL_LOGGING) {
				processLoggingRitual(cursorPos);
			} else if ((currentRitual == RITUAL_LIGHTING_TORCH) || (currentRitual == RITUAL_LIGHTING_LANTERN)) {
				processLightingRitual(cursorPos);
			}

		}

	}

	private void processRedstoneRitual() {
		int iy = this.getBlockPos().getY();
		int yCtr = 1;
		int num = this.level.getRandom().nextInt(3) + 1;
		while (iy-yCtr>0) {
			int depthY = iy-yCtr;
			int spread = yCtr/2;
			int eastOffset = this.level.getRandom().nextInt(yCtr) - spread;
			int northOffset = this.level.getRandom().nextInt(yCtr) - spread;
			BlockPos bpos = this.getBlockPos();
			BlockState b = this.level.getBlockState(this.getBlockPos().below(yCtr).east(eastOffset).north(northOffset));
			if ((b != null) && (b == Blocks.STONE.defaultBlockState())) {
				this.level.setBlockAndUpdate(this.getBlockPos().below(yCtr).east(eastOffset).north(northOffset),Blocks.REDSTONE_ORE.defaultBlockState());
				level.playSound(null, worldPosition, ModSounds.RED_SPIRIT_WORKS, SoundSource.HOSTILE, 0.5f, 0.2f);
				num--;
				if (num <1) 
					return;
			}
			yCtr++;
		}
	}

	private boolean useRitualItem(Player player, ItemStack handItemStack) {
		if (player.isCreative()) {
			return true;
		}
		Item ritualItem = handItemStack.getItem();
		if (((ritualItem == Items.TORCH) || (ritualItem == Items.LANTERN))) {
			handItemStack.setCount(handItemStack.getCount() - 1);
			return true;
		}
		if (isItemDamagingRitual(ritualItem)) {
			int itemDamage = handItemStack.getMaxDamage() - handItemStack.getDamageValue();
			if (itemDamage < 48)
				return false; // item is too weak to start ritual
			if (handItemStack.hurt(47, level.random, null)) {
				handItemStack.setCount(handItemStack.getCount() - 1);
			}
			return true;
		}
		return true;
	}

}
