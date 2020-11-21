package com.mactso.redstonemagic.tileentity;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.sounds.ModSounds;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
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
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

public class RitualPylonTileEntity extends TileEntity implements ITickableTileEntity {
	static final int RITUAL_PLAYER_COST = 3;
	static final int RITUAL_CHUNK_COST = 16;
	static final int RITUAL_NONE = 90;
	static final int RITUAL_FARMING = 91;
	static final int RITUAL_MINING = 92;
	static final int RITUAL_LOGGING = 93;
	static final int RITUAL_LIGHTING = 94;
	static final int RITUAL_WARMUP_TIME = 100; // 5 seconds

	String spellTranslationKey;
	String spellComment;
	int spellBaseCost;
	String spellTargetType;
	int particleCount = 0;
	boolean mustPayChunkCost = false;

	int timeRitualWarmup = 0;
	int timeRitualCooldown = 0;
	int currentRitual = RITUAL_NONE;
	ItemStack harvestItemStack = null;
	long cursorRitualX = 0;
	long cursorRitualY = 0;
	long cursorRitualZ = 0;
	long minRitualX = 0;
	long minRitualY = 0;
	long minRitualZ = 0;
	long maxRitualX = 0;
	long maxRitualY = 0;
	long maxRitualZ = 0;
	int maxHeight = 0;

	int mine[] = { 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1 };

	public RitualPylonTileEntity() {
		super(ModTileEntities.RITUAL_PYLON);
	}

	public void changeRitual(PlayerEntity player, Hand handIn) {

		if (!(player.isServerWorld())) {
			return;
		}

		if ((timeRitualWarmup > 0) || (timeRitualCooldown > 0)) {
			return;
		}

		ItemStack handItemStack = player.getHeldItem(handIn);
		int newRitual = RITUAL_NONE;
		int newMaxHeight = 0;
		if (handItemStack.getItem() instanceof HoeItem) {
			newRitual = RITUAL_FARMING;
			newMaxHeight = 0;
		} else if (handItemStack.getItem() instanceof PickaxeItem) {
			newRitual = RITUAL_MINING;
			harvestItemStack = handItemStack;
			newMaxHeight = 3;
		} else if (handItemStack.getItem() instanceof AxeItem) {
			newRitual = RITUAL_LOGGING;
			newMaxHeight = 7;
		} else if (handItemStack.getItem() == Items.COAL_BLOCK) {
			newRitual = RITUAL_LIGHTING;
			newMaxHeight = 0;
		}

		if (!(IsRitualRunning())) {
			if (newRitual == RITUAL_NONE) {
				world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
				return;
			}
			IMagicStorage playerManaStorage = player.getCapability(CapabilityMagic.MAGIC).orElse(null);
			if (playerManaStorage == null) {
				MyConfig.sendChat(player, "Impossible Error: You do not have a mana pool.",
						Color.fromTextFormatting((TextFormatting.YELLOW)));
				return;
			}

			int xx = playerManaStorage.getManaStored();
			if (playerManaStorage.getManaStored() < RITUAL_PLAYER_COST) { // must have 3 mana to start a ritual spell
				world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
				return;
			}
			playerManaStorage.useMana(RITUAL_PLAYER_COST);

			Chunk chunk = (Chunk) world.getChunk(pos);
			IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			int zz = chunkManaStorage.getManaStored();

			if (chunkManaStorage.getManaStored() < RITUAL_CHUNK_COST) {
				world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
				return;
			}
			chunkManaStorage.useMana(RITUAL_CHUNK_COST);

			currentRitual = newRitual;
			mustPayChunkCost = false;
			world.playSound(null, pos, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.BLOCKS, 0.5f, 0.2f);
			maxHeight = newMaxHeight;
			cursorRitualX = minRitualX = pos.getX();
			cursorRitualY = minRitualY = pos.getY();
			cursorRitualZ = minRitualZ = pos.getZ();
			maxRitualX = minRitualX + 15;
			maxRitualY = minRitualY + maxHeight;
			maxRitualZ = minRitualZ + 15;
			timeRitualWarmup = 40;

			return;
		} else {
			processEndRitual();
		}

	}

	public void createNonBasicParticle(BlockPos pos, int particleCount, IParticleData particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerWorld) world).spawnParticle(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}

	private boolean isLightable(BlockPos cursorPos) {
		return (world.getBlockState(cursorPos).getBlock() instanceof AirBlock) &&
			(world.getBlockState(cursorPos.down()).isSolid()) &&
			(world.getLight(cursorPos) <= 8);
	}

	private boolean isMinable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		return (bS.getBlock() == Blocks.NETHERRACK) ||
			(Tags.Blocks.STONE.contains(bS.getBlock())) ||
			(Tags.Blocks.DIRT.contains(bS.getBlock())) ||
			(Tags.Blocks.GRAVEL.contains(bS.getBlock()) || 
			(Tags.Blocks.SAND.contains(bS.getBlock())));
	}
	
	private boolean isLoggable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		if (BlockTags.LEAVES.contains(bS.getBlock()) ||
			BlockTags.LOGS.contains(bS.getBlock())
		   ) {
			System.out.println(cursorPos.toString() + ", " + bS.getBlock().getRegistryName().toString());
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

	private void processEndRitual() {
		currentRitual = RITUAL_NONE;
		timeRitualCooldown = 40;
		world.playSound(null, pos, SoundEvents.ENTITY_GHAST_DEATH, SoundCategory.BLOCKS, 0.5f, 0.2f);
	}

	private void processFarmingRitual(BlockPos cursorPos) {
		if (world.getBlockState(cursorPos).getBlock() instanceof CropsBlock) {
			world.destroyBlock(cursorPos, false);
			mustPayChunkCost = true;
			ItemStack blockItemStack = new ItemStack(world.getBlockState(cursorPos).getBlock());
			IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
			if (chestInv != null) {
				HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, blockItemStack, null);
			}
		}
	}

	private void processLightingRitual(BlockPos cursorPos) {
		if (isLightable(cursorPos)) {
			world.setBlockState(cursorPos, Blocks.TORCH.getDefaultState());
			world.playSound(null, cursorPos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.5f, 0.2f);
			mustPayChunkCost = true;
		}
	}

	private void processLoggingRitual(BlockPos cursorPos) {

		if (BlockTags.LEAVES.contains(world.getBlockState(cursorPos).getBlock())) {
			world.destroyBlock(cursorPos, true);
			mustPayChunkCost = true;
		}
		if (BlockTags.LOGS.contains(world.getBlockState(cursorPos).getBlock())) {
			world.destroyBlock(cursorPos, false);
			mustPayChunkCost = true;
			ItemStack blockItemStack = new ItemStack(world.getBlockState(cursorPos).getBlock());
			IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
			if (chestInv != null) {
				HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, blockItemStack, null);
			}
		}
	}

	private void processMiningRitual(BlockPos cursorPos) {

		if (isMinable(cursorPos)) {
			world.destroyBlock(cursorPos, false);
			mustPayChunkCost = true;
			ItemStack blockItemStack;

			if (world.getBlockState(cursorPos).getBlock() == Blocks.STONE) {
				blockItemStack = new ItemStack(Blocks.COBBLESTONE);
			} else if (world.getBlockState(cursorPos).getBlock() instanceof GrassBlock) {
				blockItemStack = new ItemStack(Blocks.DIRT);
			} else {
				blockItemStack = new ItemStack(world.getBlockState(cursorPos).getBlock());
			}

			IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
			if (chestInv != null) {
				HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, blockItemStack, null);
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
	public void tick() {

		if (this.world != null && !(world instanceof ServerWorld)) {
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

		if (this.world.getGameTime() % 10L != 0L) {
			return;
		}

		if (IsRitualRunning()) {

			long dayTime = world.getDayTime();
			float humVolume = (float) (0.05 + (0.04 * Math.sin((double) dayTime)));
			float humPitch = (float) (0.7 + (0.3 * Math.sin((double) dayTime)));

			world.playSound(null, pos, ModSounds.RITUAL_PYLON_THRUMS, SoundCategory.BLOCKS, humVolume, humPitch);
			((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) this.pos.getX(),
					(double) this.pos.getY() + 0.35D, 0.5D + (double) this.pos.getZ(), 3, 0.0D, 0.1D, 0.0D, -0.04D);

			BlockPos cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
			BlockState tBS = world.getBlockState(cursorPos);
			boolean noValidRitualBlockFound = true;
			ItemStack stack = new ItemStack(Items.GLOWSTONE, 1);
			boolean doMineGalleries = false;

			while (noValidRitualBlockFound) {
//				System.out.println("looping " +cursorRitualX +", " +cursorRitualZ);
				cursorRitualX++;
				if (cursorRitualX > maxRitualX) {
					cursorRitualX = minRitualX;
					cursorRitualZ++;
					if (cursorRitualZ > maxRitualZ) {
						cursorRitualZ = minRitualZ;
						cursorRitualY++;
						if (cursorRitualY > maxRitualY) {
							processEndRitual();
							return;
						}
						Chunk chunk = (Chunk) world.getChunk(pos);
						IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
						if (!(chunkManaStorage.useMana(12))) {
							world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
							world.playSound(null, pos, SoundEvents.ENTITY_ENDERMITE_DEATH, SoundCategory.BLOCKS, 0.5f,
									0.2f);
							processEndRitual();
							return;
						}

					}
				}
				cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
				((ServerWorld) world).spawnParticle(ParticleTypes.POOF,
						0.5D +  cursorRitualX,
						0.35D + cursorRitualY,
						0.5D +  cursorRitualZ, 3, 0.0D, 0.1D, 0.0D, -0.04D);
				createNonBasicParticle(cursorPos, 1, new ItemParticleData(ParticleTypes.ITEM, stack));

				if ((currentRitual == RITUAL_LIGHTING) && (isLightable(cursorPos))) {
						noValidRitualBlockFound = false;
				} else if ((currentRitual == RITUAL_MINING) && (isMinable(cursorPos))) {
					doMineGalleries = (mine[(int) (cursorRitualX - minRitualX)] + mine[(int) (cursorRitualZ - minRitualZ)]) > 0;
					if (doMineGalleries) {
						noValidRitualBlockFound = false;
						if ((world.getRandom().nextFloat() * 100.0f) <25.0f) {
							world.playSound(null, cursorPos, SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.BLOCKS, 0.5f, 0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_LOGGING) && (isLoggable(cursorPos))){
					noValidRitualBlockFound = false;
				} else { // not mining, lighting, or logging
					if (!(tBS.getBlock() instanceof AirBlock)) {
						noValidRitualBlockFound = false;
					}
				}
			}

			((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + (double) minRitualX + cursorRitualX,
					(double) minRitualY + cursorRitualY + 0.10D, 0.5D + (double) minRitualZ + cursorRitualZ, (int) 3,
					0.0D, 0.1D, 0.0D, 0.04D);

			if (currentRitual == RITUAL_FARMING) {
				processFarmingRitual(cursorPos);
			} else {
				if (currentRitual == RITUAL_MINING) {
					if (doMineGalleries) {
						processMiningRitual(cursorPos);
					}
				} else {
					if (currentRitual == RITUAL_LOGGING) {
						processLoggingRitual(cursorPos);
					} else {
						if (currentRitual == RITUAL_LIGHTING) {
							processLightingRitual(cursorPos);
						}

					}
				}
			}
		}

	}

}
