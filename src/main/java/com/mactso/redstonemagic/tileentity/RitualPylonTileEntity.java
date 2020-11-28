package com.mactso.redstonemagic.tileentity;

import com.electronwill.nightconfig.core.conversion.ForceBreakdown;
import com.mactso.redstonemagic.block.ModBlocks;
import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.mana.CapabilityMagic;
import com.mactso.redstonemagic.mana.IMagicStorage;
import com.mactso.redstonemagic.network.Network;
import com.mactso.redstonemagic.network.SyncClientManaPacket;
import com.mactso.redstonemagic.sounds.ModSounds;
import com.mojang.datafixers.types.templates.Tag.TagType;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.command.impl.SeedCommand;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

public class RitualPylonTileEntity extends TileEntity implements ITickableTileEntity {
	static final int RITUAL_PLAYER_COST = 5;
	static final int RITUAL_CHUNK_COST = 16;
	static final int RITUAL_NONE = 90;
	static final int RITUAL_FARMING = 91;
	static final int RITUAL_MINING = 92;
	static final int RITUAL_LOGGING = 93;
	static final int RITUAL_LIGHTING = 94;
	static final int RITUAL_CLEARING = 95;
	static final int RITUAL_TESTING = 96;
	
	static final int RITUAL_WARMUP_TIME = 100; // 5 seconds
	static final ItemStack GLOWSTONE_STACK = new ItemStack(Items.GLOWSTONE, 1);
	static final ItemStack REDSTONE_BLOCK_STACK = new ItemStack(Items.REDSTONE_BLOCK, 1);

	String spellTranslationKey;
	String spellComment;
	int spellBaseCost;
	String spellTargetType;
	int particleCount = 0;
	boolean mustPayChunkCost = false;

	int ritualSpeed = 0;
	float harvestCount = 0.0f;
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

	int mine[] = {1,0,0,1, 0,0,0,1,1,0,0,0, 1,0,0,1 };

	public RitualPylonTileEntity() {
		super(ModTileEntities.RITUAL_PYLON);
	}

	@SuppressWarnings("deprecation")
	public void changeRitual(PlayerEntity player, Hand handIn) {

		if (!(player.isServerWorld())) {
			return;
		}

		if ((timeRitualWarmup > 0) || (timeRitualCooldown > 0)) {
			return;
		}

		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
		ItemStack handItemStack = player.getHeldItem(handIn);
		int newRitual = RITUAL_NONE;
		int newMaxHeight = 0;
		Item newRitualItem = handItemStack.getItem();
		int newRitualSpeed = 20;
		int newRitualCost= RITUAL_PLAYER_COST;
		boolean damageItem = false;
		boolean useItem = false;


		if (newRitualItem instanceof SwordItem) {
			newRitual = RITUAL_TESTING;
			newMaxHeight = 1;
			newRitualSpeed = 51 - (int) Math.sqrt(100+newRitualItem.getMaxDamage());
		} else if (newRitualItem instanceof ShovelItem) {
			damageItem = true;
			newRitual = RITUAL_CLEARING;
			newMaxHeight = 1;
			newRitualSpeed = 51 - (int) Math.sqrt(100+newRitualItem.getMaxDamage());
		} else if (newRitualItem instanceof HoeItem) {
			damageItem = true;
			newRitual = RITUAL_FARMING;
			newMaxHeight = 0;
			newRitualSpeed = 51 - (int) Math.sqrt(100+newRitualItem.getMaxDamage());
		} else if (newRitualItem instanceof PickaxeItem) {
			damageItem = true;
			newRitualSpeed = 51 - (int) Math.sqrt(100+newRitualItem.getMaxDamage());
			newRitual = RITUAL_MINING;
			int yCost = 4-pos.getY()/10;
			if (yCost < 1) yCost = 1;
			newRitualCost= RITUAL_PLAYER_COST+RITUAL_PLAYER_COST * yCost;
			newMaxHeight = 3;
		} else if (newRitualItem instanceof AxeItem) {
			damageItem = true;
			newRitualSpeed = 51 - (int) Math.sqrt(100+newRitualItem.getMaxDamage());
			newRitual = RITUAL_LOGGING;
			newMaxHeight = 19;
		} else if ((newRitualItem == Items.TORCH) || (newRitualItem == Items.LANTERN))  {
			useItem = true;
			newRitualSpeed = 50;
			newRitualCost= RITUAL_PLAYER_COST*2;
			newRitual = RITUAL_LIGHTING;
			newMaxHeight = 0;
			if (newRitualItem == Items.LANTERN) {
				newMaxHeight = 1;
				newRitualSpeed = 30;
			}
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
			if (playerManaStorage.getManaStored() < newRitualCost) { // player must have this much mana to start a ritual spell
				world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
				return;
			}
			playerManaStorage.useMana(newRitualCost);

			Chunk chunk = (Chunk) world.getChunk(pos);
			IMagicStorage chunkManaStorage = chunk.getCapability(CapabilityMagic.MAGIC).orElse(null);
			int zz = chunkManaStorage.getManaStored();

			if (chunkManaStorage.getManaStored() < RITUAL_CHUNK_COST) {
				world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
				return;
			}
			chunkManaStorage.useMana(RITUAL_CHUNK_COST);

			Network.sendToClient(new SyncClientManaPacket(playerManaStorage.getManaStored(), chunkManaStorage.getManaStored()),
					serverPlayer);
			
			if (damageItem) {
				int itemDamage = handItemStack.getMaxDamage() - handItemStack.getDamage() ;
				if (itemDamage > 64) {
					itemDamage = 64;
				}
				if (world.rand.nextFloat() * 100 < MyConfig.getNeverBreakTools()) {
					itemDamage = itemDamage / 2;
				}
				handItemStack.attemptDamageItem(itemDamage, world.rand, null);
			}
			if (useItem) {
				handItemStack.setCount(handItemStack.getCount()-1);
			}

			ritualSpeed = newRitualSpeed+3;
			if (ritualSpeed < 8) ritualSpeed = 8;
			harvestCount = 0.0f;
			currentRitual = newRitual;
			mustPayChunkCost = false;
			world.playSound(null, pos, ModSounds.RITUAL_BEGINS, SoundCategory.BLOCKS, 0.5f, 0.2f);
			maxHeight = newMaxHeight;
			calculateRitualArea();			
			cursorRitualY = minRitualY = pos.getY();
			maxRitualY = minRitualY + maxHeight;
			maxRitualZ = minRitualZ + 15;
			timeRitualWarmup = 40;

			return;
		} else {
			processEndRitual();
		}

	}

	public void calculateRitualArea() {

		BlockPos eastPos = null;
		BlockPos westPos = null;

		for(int i = 1;i<16;i++) {
			if (world.getBlockState(pos.east(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualX = minRitualX = pos.east(i).getX()-15;
				eastPos = pos.east(i);
			} else {
				break;
			}
		}
		if (eastPos== null) {
			for(int i = 1;i<16;i++) {
				if (world.getBlockState(pos.west(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualX = minRitualX = pos.west(i).getX();
					westPos = pos.west(i+1);
				} else {
					break;
				}
			}
		}
		if (eastPos == null && westPos == null) {
			cursorRitualX = minRitualX =  world.getChunk(pos).getPos().getXStart();
		}
		maxRitualX = minRitualX + 15;
		
		BlockPos southPos = null;
		BlockPos northPos = null;
		
		for(int i = 1;i<16;i++) {
			if (world.getBlockState(pos.south(i)).getBlock() == Blocks.REDSTONE_WIRE) {
				cursorRitualZ = minRitualZ = pos.south(i).getZ()-15;
				southPos = pos.south(i);
			} else {
				break;
			}
		}
		if (southPos== null) {
			for(int i = 1;i<16;i++) {
				if (world.getBlockState(pos.north(i)).getBlock() == Blocks.REDSTONE_WIRE) {
					cursorRitualZ = minRitualZ = pos.north(i).getZ();
					northPos = pos.north(i+1);
				} else {
					break;
				}
			}
		}
		if (southPos == null && northPos == null) {
			cursorRitualZ = minRitualZ =  world.getChunk(pos).getPos().getZStart();
		}
		maxRitualZ = minRitualZ + 15;		
	}
	
	public void createNonBasicParticle(BlockPos pos, int particleCount, IParticleData particleType) {
		double xOffset = 0.5D;
		double yOffset = 0.25D;
		double zOffset = 0.5D;
		((ServerWorld) world).spawnParticle(particleType, pos.getX(), pos.getY(), pos.getZ(), particleCount, xOffset,
				yOffset, zOffset, -0.04D);
	}

	private boolean isLightable(BlockPos cursorPos) {
		return (world.getBlockState(cursorPos).getBlock() instanceof AirBlock)
				&& (world.getLight(cursorPos) <= 8);
	}

	private boolean isMineable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		return (bS.getBlock() == Blocks.NETHERRACK) || (Tags.Blocks.STONE.contains(bS.getBlock()))
				|| (Tags.Blocks.DIRT.contains(bS.getBlock()))
				|| (Tags.Blocks.GRAVEL.contains(bS.getBlock()) || (Tags.Blocks.SAND.contains(bS.getBlock())));
	}

	private boolean isClearable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		return (BlockTags.FLOWERS.contains(bS.getBlock()) ||
				(bS.getBlock() instanceof TallGrassBlock) ||
				Tags.Blocks.DIRT.contains(bS.getBlock())
				|| (Tags.Blocks.GRAVEL.contains(bS.getBlock()) || (Tags.Blocks.SAND.contains(bS.getBlock()))));
	}

	private boolean isLoggable(BlockPos cursorPos) {
		BlockState bS = world.getBlockState(cursorPos);
		if (BlockTags.LEAVES.contains(bS.getBlock()) || BlockTags.LOGS.contains(bS.getBlock())) {
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
		world.playSound(null, pos, ModSounds.RITUAL_ENDS, SoundCategory.BLOCKS, 0.5f, 0.2f);
	}

	
	private void processClearingRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (isClearable(cursorPos)) {
			mustPayChunkCost = true;
			world.destroyBlock(cursorPos, false);
	        for (ItemStack dropsStack: tBS.getDrops(doGetLootBuilder(cursorPos)))
	        {
				IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
	        }
		}
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
				
		        for (ItemStack dropsStack: tBS.getDrops(doGetLootBuilder(cursorPos)))
		        {
		        	if (dropsStack.getItem() instanceof BlockNamedItem) {
		        		int v = Math.max((dropsStack.getCount()-2),0);
		        		dropsStack.setCount(v); ;
		        	}
		        	IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
					if (chestInv != null) {
						HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
					}
		        }

			}
		}
	}

	public LootContext.Builder doGetLootBuilder(BlockPos cursorPos) {
		LootContext.Builder builder = (new LootContext.Builder((ServerWorld) world)).withRandom(world.rand)
		        .withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(cursorPos))
		        .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
		        .withNullableParameter(LootParameters.THIS_ENTITY, null)
		        .withNullableParameter(LootParameters.BLOCK_ENTITY, this);
		return builder;
	}

	   
	private void processLightingRitual(BlockPos cursorPos) {
		if (isLightable(cursorPos)) {
			world.setBlockState(cursorPos, ModBlocks.LIGHT_SPELL.getDefaultState());
			world.playSound(null, cursorPos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.5f, 0.2f);
			mustPayChunkCost = true;
		}
	}

	private void processLoggingRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (BlockTags.LEAVES.contains(tBlock)) {
			world.destroyBlock(cursorPos, true);
        	harvestCount += 0.1f;
		}
		if (BlockTags.LOGS.contains(world.getBlockState(cursorPos).getBlock())) {
			world.destroyBlock(cursorPos, false);
	        for (ItemStack dropsStack: tBS.getDrops(doGetLootBuilder(cursorPos)))
	        {
	        	harvestCount += 1.0f;
	        	IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down());
				if (chestInv != null) {
					HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, dropsStack, null);
				}
	        }
		}
    	if (harvestCount > 64.0f) {
    		harvestCount = 0.0f;
        	mustPayChunkCost = true;
    	}

	}

	private void processMiningRitual(BlockPos cursorPos) {
		BlockState tBS = world.getBlockState(cursorPos);
		Block tBlock = tBS.getBlock();
		if (isMineable(cursorPos)) {
			mustPayChunkCost = true;
			world.destroyBlock(cursorPos, false);
	        for (ItemStack dropsStack: tBS.getDrops(doGetLootBuilder(cursorPos)))
	        {
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

		long speed = ritualSpeed;
		if (speed < 8) speed = 8L;
		if (this.world.getGameTime() % speed != 0L) {
			return;
		}

		if (IsRitualRunning()) {

			long dayTime = world.getDayTime();
			float humVolume = (float) (0.05 + (0.04 * Math.sin((double) dayTime)));
			float humPitch = (float) (0.7 + (0.3 * Math.sin((double) dayTime)));

			world.playSound(null, pos, ModSounds.RITUAL_PYLON_THRUMS, SoundCategory.BLOCKS, humVolume/3, humPitch);
			((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, 0.5D + (double) this.pos.getX(),
					(double) this.pos.getY() + 0.35D, 0.5D + (double) this.pos.getZ(), 3, 0.0D, 0.1D, 0.0D, -0.04D);

			BlockPos cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
			BlockState tBS = world.getBlockState(cursorPos);
			boolean noValidRitualBlockFound = true;
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
						int x = chunkManaStorage.getManaStored();
						if (mustPayChunkCost) {
							if (!(chunkManaStorage.useMana(16))) {
								world.playSound(null, pos, ModSounds.SPELL_FAILS, SoundCategory.BLOCKS, 0.5f, 0.2f);
								world.playSound(null, pos, SoundEvents.ENTITY_ENDERMITE_DEATH, SoundCategory.BLOCKS, 0.5f,
										0.2f);
								processEndRitual();
								return;
							}
							mustPayChunkCost = false;
						}

					}
				}
				cursorPos = new BlockPos(cursorRitualX, cursorRitualY, cursorRitualZ);
				((ServerWorld) world).spawnParticle(ParticleTypes.POOF,
						0.5D +  cursorRitualX,
						0.35D + cursorRitualY,
						0.5D +  cursorRitualZ, 3, 0.0D, 0.1D, 0.0D, -0.04D);
				createNonBasicParticle(cursorPos, 1, new ItemParticleData(ParticleTypes.ITEM, GLOWSTONE_STACK));

				if ((currentRitual == RITUAL_LIGHTING) && (isLightable(cursorPos))) {
						noValidRitualBlockFound = false;
						if ((world.getRandom().nextFloat() * 100.0f) <50.0f) {
							world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
						}
				} else if ((currentRitual == RITUAL_FARMING) && isReadyCropBlock(cursorPos)) {
						noValidRitualBlockFound = false;
						if ((world.getRandom().nextFloat() * 100.0f) <25.0f) {
							world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
						}
				} else if ((currentRitual == RITUAL_CLEARING) && isClearable(cursorPos)) {
					noValidRitualBlockFound = false;
					if ((world.getRandom().nextFloat() * 100.0f) <15.0f) {
						world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
					}
				}
				else if ((currentRitual == RITUAL_MINING) && (isMineable(cursorPos))) {

					int x = Math.abs((cursorPos.getX())%16);
					if (cursorPos.getX() < 0) {
						x = 15 - x;
					}
					int z = Math.abs((cursorPos.getZ())%16);
					if (cursorPos.getZ() < 0) {
						z = 15 - z;
					}

					doMineGalleries = (mine[x] + mine[z]) > 0;
					if (doMineGalleries) {
						noValidRitualBlockFound = false;
						if ((world.getRandom().nextFloat() * 100.0f) <25.0f) {
							world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
						}
					}
				} else if ((currentRitual == RITUAL_LOGGING) && (isLoggable(cursorPos))){
					noValidRitualBlockFound = false;
					if ((world.getRandom().nextFloat() * 100.0f) <25.0f) {
						world.playSound(null, cursorPos, ModSounds.RED_SPIRIT_WORKS, SoundCategory.BLOCKS, 0.5f, 0.2f);
					}
				} 
			}

			((ServerWorld) world).spawnParticle(ParticleTypes.POOF, 0.5D + (double) minRitualX + cursorRitualX,
					(double) minRitualY + cursorRitualY + 0.10D, 0.5D + (double) minRitualZ + cursorRitualZ, (int) 3,
					0.0D, 0.1D, 0.0D, 0.04D);

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
			} else if (currentRitual == RITUAL_LIGHTING) {
				processLightingRitual(cursorPos);
			}
			
		}

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

}
