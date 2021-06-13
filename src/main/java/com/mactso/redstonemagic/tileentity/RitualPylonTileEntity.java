package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

public class RitualPylonTileEntity extends TileEntity implements ITickableTileEntity {
	static final int RITUAL_PLAYER_COST = 5;
	static final int RITUAL_CHUNK_COST = 16;
	static final int RITUAL_NONE = -1;
	static final int RITUAL_TESTING = 90;
	static final int RITUAL_FARMING = 91;
	static final int RITUAL_MINING = 92;
	static final int RITUAL_LOGGING = 93;
	static final int RITUAL_LIGHTING_TORCH= 94;
	static final int RITUAL_LIGHTING_LANTERN = 95;
	static final int RITUAL_CLEARING = 96;

	static final int RITUAL_WARMUP_TIME = 100; // 5 seconds
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	static final ItemStack REDSTONE_BLOCK_STACK = new ItemStack(Items.REDSTONE_BLOCK, 1);


	int ritualSpeed = 0;
	int currentRitual = RITUAL_NONE;
	float harvestWorkTotal = 0.0f;
	int minRitualX = 0;
	int minRitualZ = 0;
	int cursorRitualX = 0;
	int cursorRitualY = 0;
	int cursorRitualZ = 0;

	boolean mustPayChunkCost = false;
	int timeRitualWarmup = 0;
	int timeRitualCooldown = 0;

	int mine[] = { 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1 };

	public RitualPylonTileEntity() {
		super(ModTileEntities.RITUAL_PYLON);
	}

	private int calcChunkRitualManaCost() {
		int newRitualChunkCost = RITUAL_CHUNK_COST;
		if (pos.getY()< 22	) {
			newRitualChunkCost += RITUAL_CHUNK_COST;	
		}
		if (pos.getY()< 11) {
			newRitualChunkCost += RITUAL_CHUNK_COST;	
		}
		return newRitualChunkCost;
	}

	private int calcPlayerRitualManaCost() {
		int newRitualCost = RITUAL_PLAYER_COST;
		if (pos.getY()< 22) {
			newRitualCost += RITUAL_PLAYER_COST;	
		}
		if (pos.getY()< 11) {
			newRitualCost += RITUAL_PLAYER_COST;	
		}
		return newRitualCost;
	}

	public void calcRitualArea() {

		BlockPos eastPos = null;
		BlockPos westPos = null;

		for (int i = 1; i < 16; i++) {
			if (world.getBlockState(pos.east(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualX = minRitualX = pos.east(i).getX() - 15;
				eastPos = pos.east(i);
			} else {
				break;
			}
		}
		if (eastPos == null) {
			for (int i = 1; i < 16; i++) {
				if (world.getBlockState(pos.west(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualX = minRitualX = pos.west(i).getX();
					westPos = pos.west(i + 1);
				} else {
					break;
				}
			}
		}
		if (eastPos == null && westPos == null) {
			cursorRitualX = minRitualX = world.getChunk(pos).getPos().getXStart() - 1;
		}


		BlockPos southPos = null;
		BlockPos northPos = null;

		for (int i = 1; i < 16; i++) {
			if (world.getBlockState(pos.south(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualZ = minRitualZ = pos.south(i).getZ() - 15;
				southPos = pos.south(i);
			} else {
				break;
			}
		}
		if (southPos == null) {
			for (int i = 1; i < 16; i++) {
				if (world.getBlockState(pos.north(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualZ = minRitualZ = pos.north(i).getZ();
					northPos = pos.north(i + 1);
				} else {
					break;
				}
			}
		}
		if (southPos == null && northPos == null) {
			cursorRitualZ = minRitualZ = world.getChunk(pos).getPos().getZStart();
		}
		
		cursorRitualY = pos.getY();

	}

	public void utilCreateNonBasicParticle(BlockPos pos, int particleCount, IParticleData particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerWorld) world).spawnParticle(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}

	public LootContext.Builder utilDoGetLootBuilder(BlockPos cursorPos) {
		LootContext.Builder builder = (new LootContext.Builder((ServerWorld) world)).withRandom(world.rand)
				.withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(cursorPos))
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
				.withNullableParameter(LootParameters.THIS_ENTITY, null)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, this);
		return builder;
	}

	public boolean doRitualPylonInteraction(PlayerEntity player, Hand handIn) {

		if ((timeRitualWarmup > 0) || (timeRitualCooldown > 0)) {
			return false;
		}

		if (IsRitualRunning()) {
			processEndRitual();
			return false;
		}

		if (player instanceof ServerPlayerEntity) {

			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			ItemStack handItemStack = player.getHeldItem(handIn);
			Item newRitualItem = handItemStack.getItem();

			if (getRitualID(newRitualItem) == RITUAL_NONE) {
				return false;
			}

			if ((useRitualItem(handItemStack)) == false) { // damages or destroys
				return false;
			}

			IMagicStorage playerManaStorage = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
			Chunk chunk = (Chunk) world.getChunk(pos);
			IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);

			if (playerManaStorage == null) {
				MyConfig.sendChat(player, "Impossible Error: You do not have a mana pool.",
						Color.fromTextFormatting((TextFormatting.YELLOW)));
				return false;
			}

			if ((playerManaStorage.getManaStored() < calcPlayerRitualManaCost())
					|| (chunkManaStorage.getManaStored() < calcChunkRitualManaCost())) {
				return false;
			}

			playerManaStorage.useMana(calcPlayerRitualManaCost());
			chunkManaStorage.useMana(calcChunkRitualManaCost());

			Network.sendToClient(
					new SyncClientManaPacket(playerManaStorage.getManaStored(), chunkManaStorage.getManaStored()),
					serverPlayer);

			chunk.markDirty();
			ritualSpeed = getRitualSpeed(newRitualItem);
			currentRitual = getRitualID(newRitualItem);
			harvestWorkTotal = 0.0f;
			mustPayChunkCost = false;
			world.playSound(null, pos, ModSounds.RITUAL_BEGINS, SoundCategory.BLOCKS, 0.5f, 0.2f);
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
			return  1;
		} else if (ritualID == RITUAL_FARMING) {
			return  0;
		} else if (ritualID == RITUAL_MINING) {
			return  3;
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
		BlockState bS = world.getBlockState(cursorPos);
		return (BlockTags.FLOWERS.contains(bS.getBlock()) || (bS.getBlock() instanceof TallGrassBlock)
				|| (bS.getBlock() instanceof DoublePlantBlock)
				|| Tags.Blocks.DIRT.contains(bS.getBlock())
				|| (Tags.Blocks.GRAVEL.contains(bS.getBlock()) || (Tags.Blocks.SAND.contains(bS.getBlock()))));
	}

	private boolean isItemDamagingRitual(Item ritualItem) {
		if (getRitualID(ritualItem) == RITUAL_TESTING) {
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
		return (world.getBlockState(cursorPos).getBlock() instanceof AirBlock) && (world.getLightFor(LightType.BLOCK, cursorPos) <= 8);
	}

	private boolean isLoggable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		if (BlockTags.LEAVES.contains(bS.getBlock()) || BlockTags.LOGS.contains(bS.getBlock())) {
			return true;
		}
		return false;
	}

	private boolean isMineable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		return (bS.getBlock() == Blocks.NETHERRACK) || (Tags.Blocks.STONE.contains(bS.getBlock()))
				|| (Tags.Blocks.DIRT.contains(bS.getBlock()))
				|| (Tags.Blocks.GRAVEL.contains(bS.getBlock()) || (Tags.Blocks.SAND.contains(bS.getBlock())));
	}

	private boolean isReadyCropBlock(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = world.getBlockState(cursorPos).getBlock();
		if (tBlock instanceof CropsBlock) {
			CropsBlock c = (CropsBlock) tBlock;
			if (c.isMaxAge(tBS)) {
				return true;
			}
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
		BlockState tBS = world.getBlockState(cursorPos);
		if (isClearable(cursorPos)) {
			mustPayChunkCost = true;
			world.destroyBlock(cursorPos, false);
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
			}
		}
	}

	private void processEndRitual() {
		currentRitual = RITUAL_NONE;
		timeRitualCooldown = 40;
		world.playSound(null, pos, ModSounds.RITUAL_ENDS, SoundCategory.BLOCKS, 0.5f, 0.2f);
	}

	private void processFarmingRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (tBlock instanceof CropsBlock) {
			CropsBlock c = (CropsBlock) tBlock;
			if (c.isMaxAge(tBS)) {
				world.playSound(null, cursorPos, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.5f, 0.2f);
				BlockState bC = c.getDefaultState().with(c.getAgeProperty(), Integer.valueOf(0));
				world.setBlockState(cursorPos, bC);
				mustPayChunkCost = true;

				for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
					if (dropsStack.getItem() instanceof BlockNamedItem) {
						int v = Math.max((dropsStack.getCount() - 2), 0);
						dropsStack.setCount(v);
						;
					}
					IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
					if (chestInv != null) {
						HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
					}
				}

			}
		}
	}

	private void processLightingRitual(BlockPos cursorPos) {
		if (isLightable(cursorPos)) {
			world.setBlockState(cursorPos, ModBlocks.LIGHT_SPELL.getDefaultState());
			world.playSound(null, cursorPos,ModSounds.REDSTONEMAGIC_LIGHT,
					SoundCategory.BLOCKS, 0.7f, 0.86f);
			mustPayChunkCost = true;
		}
	}

	private void processLoggingRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (BlockTags.LEAVES.contains(tBlock)) {
			world.destroyBlock(cursorPos, false);
			harvestWorkTotal += 0.1f;
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
			}
		}
		if (BlockTags.LOGS.contains(world.getBlockState(cursorPos).getBlock())) {
			world.destroyBlock(cursorPos, false);
			harvestWorkTotal += 1.0f;
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
			}
		}
		if (harvestWorkTotal > 64.0f) {
			harvestWorkTotal = 0.0f;
			mustPayChunkCost = true;
		}

	}

	private void processMiningRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		if (isMineable(cursorPos)) {
			mustPayChunkCost = true;
			world.destroyBlock(cursorPos, false);
			for (ItemStack dropsStack : tBS.getDrops(utilDoGetLootBuilder(cursorPos))) {
				IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
			}
		}
	}

	private void processRitualCooldown() {
		((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) this.pos.getX(),
				(double) this.pos.getY() + 0.7D, 0.5D + (double) this.pos.getZ(), (int) timeRitualCooldown / 8, 0.0D,
				-0.1D, 0.0D, -0.04D);
		((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + (double) this.pos.getX(),
				(double) this.pos.getY() + 0.10D, 0.5D + (double) this.pos.getZ(), (int) timeRitualWarmup / 8, 0.0D,
				0.1D, 0.0D, 0.04D);
		timeRitualCooldown--;
	}

	private void processRitualWarmup() {
		((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) this.pos.getX(),
				(double) this.pos.getY() + 0.10D, 0.5D + (double) this.pos.getZ(), (int) timeRitualWarmup / 8, 0.0D,
				0.1D, 0.0D, 0.04D);
		((ServerWorld) world).spawnParticle(ParticleTypes.LAVA, 0.5D + (double) this.pos.getX(),
				(double) this.pos.getY() + 0.10D, 0.5D + (double) this.pos.getZ(), (int) timeRitualWarmup / 8, 0.0D,
				0.1D, 0.0D, 0.04D);
		timeRitualWarmup--;
	}

	@Override
	// restore state when chunk reloads
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		

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
	public CompoundNBT write(CompoundNBT compound) {

		compound.putInt("currentRitual", currentRitual);
		compound.putFloat("harvestWorkTotal", harvestWorkTotal);
		compound.putInt("ritualSpeed", ritualSpeed);
		compound.putInt("minRitualX", minRitualX);
		compound.putInt("minRitualZ", minRitualZ);
		compound.putInt("cursorRitualX", cursorRitualX);
		compound.putInt("cursorRitualZ", cursorRitualZ);
		compound.putInt("cursorRitualY", cursorRitualY);

		//		if (currentRitual != null)
//			compound.putUniqueId("currentRitual", currentRitual);
		return super.write(compound);
	}
	
	@Override
	public void tick() {

		if (world != null && !(world instanceof ServerWorld)) {
			return;
		}

		if (timeRitualWarmup > 0) {
			processRitualWarmup();
			return;
		}
		if (timeRitualCooldown > 0) {
			processRitualCooldown();
			return;
		}

		if (IsRitualRunning()) {
			long speed = ritualSpeed;
			if (speed < 8)
				speed = 8L;
			if (this.world.getGameTime() % speed != 0L) {
				return;
			}
			long dayTime = world.getDayTime();
			float humVolume = (float) (0.05 + (0.04 * Math.sin((double) dayTime)));
			float humPitch = (float) (0.7 + (0.3 * Math.sin((double) dayTime)));

			world.playSound(null, pos, ModSounds.RITUAL_PYLON_THRUMS, SoundCategory.BLOCKS, humVolume / 3, humPitch);
			((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) this.pos.getX(),
					(double) this.pos.getY() + 0.35D, 0.5D + (double) this.pos.getZ(), 3, 0.0D, 0.1D, 0.0D, -0.04D);

			BlockPos cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
			boolean noValidRitualBlockFound = true;
			boolean doMineGalleries = false;

			while (noValidRitualBlockFound) {
//				System.out.println("looping " +cursorRitualX +", " +cursorRitualZ);
				cursorRitualX++;
				if (cursorRitualX > minRitualX+15) {
					cursorRitualX = minRitualX;
					cursorRitualZ++;
					if (cursorRitualZ > minRitualZ+15) {
						cursorRitualZ = minRitualZ;
						cursorRitualY++;
						if (cursorRitualY > pos.getY() + getRitualHeight(currentRitual)) {
							processEndRitual();
							return;
						}
						Chunk chunk = (Chunk) world.getChunk(pos);
						IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
						if (mustPayChunkCost) {
							if (!(chunkManaStorage.useMana(16))) {
								world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
								world.playSound(null, pos, SoundEvents.ENTITY_ENDERMITE_DEATH, SoundCategory.BLOCKS,
										0.5f, 0.2f);
								processEndRitual();
								return;
							}
							mustPayChunkCost = false;
						}

					}
				}
				cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
				((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + cursorRitualX, 0.35D + cursorRitualY,
						0.5D + cursorRitualZ, 3, 0.0D, 0.1D, 0.0D, -0.04D);
				utilCreateNonBasicParticle(cursorPos, 1, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));

				if ((currentRitual == RITUAL_LIGHTING_TORCH) || (currentRitual == RITUAL_LIGHTING_LANTERN)) {
					if (isLightable(cursorPos)) {
						noValidRitualBlockFound = false;
						if ((world.getRandom().nextFloat() * 100.0f) < 50.0f) {
							world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_FARMING) && isReadyCropBlock(cursorPos)) {
					noValidRitualBlockFound = false;
					if ((world.getRandom().nextFloat() * 100.0f) < 25.0f) {
						world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
					}
				} else if ((currentRitual == RITUAL_CLEARING) && isClearable(cursorPos)) {
					noValidRitualBlockFound = false;
					if ((world.getRandom().nextFloat() * 100.0f) < 15.0f) {
						world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
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
						if ((world.getRandom().nextFloat() * 100.0f) < 25.0f) {
							world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f,
									0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_LOGGING) && (isLoggable(cursorPos))) {
					noValidRitualBlockFound = false;
					if ((world.getRandom().nextFloat() * 100.0f) < 25.0f) {
						world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
					}
				}
			}

			if (currentRitual != RITUAL_TESTING ) {
				((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + (double) minRitualX + cursorRitualX,
						(double) pos.getY() + cursorRitualY + 0.10D, 0.5D + (double) minRitualZ + cursorRitualZ, (int) 3,
						0.0D, 0.1D, 0.0D, 0.04D);
			} else {
				((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + (double) minRitualX + cursorRitualX,
						(double) pos.getY() + cursorRitualY + 0.10D, 0.5D + (double) minRitualZ + cursorRitualZ, (int) 1,
						0.0D, 0.1D, 0.0D, 0.04D);
				
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
			} else if ((currentRitual == RITUAL_LIGHTING_TORCH)||(currentRitual == RITUAL_LIGHTING_LANTERN)) {
				processLightingRitual(cursorPos);
			}

		}

	}

	private boolean useRitualItem(ItemStack handItemStack) {
		Item ritualItem = handItemStack.getItem();
		if (((ritualItem == Items.TORCH) || (ritualItem == Items.LANTERN)) ) {
			handItemStack.setCount(handItemStack.getCount() - 1);
			return true;
		}
		if (isItemDamagingRitual(ritualItem)) {
			int itemDamage = handItemStack.getMaxDamage() - handItemStack.getDamage();
			if (itemDamage < 48) return false; // item is too weak to start ritual
			if (handItemStack.attemptDamageItem(64, world.rand, null)) {
				handItemStack.setCount(handItemStack.getCount() - 1);
			}
			return true;
		}
		return true;
	}

}
