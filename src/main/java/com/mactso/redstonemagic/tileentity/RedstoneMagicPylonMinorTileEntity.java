package com.mactso.redstonemagic.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class RedstoneMagicPylonMinorTileEntity extends TileEntity implements ITickableTileEntity {

	int counter = 0;

	public RedstoneMagicPylonMinorTileEntity() {
		super(ModTileEntities.REDSTONE_MAGIC_PYLON_MINOR);
	}

	@Override
	public void tick() {
		if (this.world != null && !this.world.isRemote && this.world.getGameTime() % 200L == 0L) {
			BlockState blockstate = this.getBlockState();
			Block block = blockstate.getBlock();
			if (counter > 0) {
				if(this.world.getBlockState(pos.down()).getBlock() == Blocks.CHEST) {
				    BlockState cobState = Blocks.COBBLESTONE.getDefaultState();
					ItemStack cobStack = new ItemStack(cobState.getBlock());
					BlockState chestBlockState = this.world.getBlockState(pos.down());
				    Block chestBlock = this.world.getBlockState(pos.down()).getBlock();
				    ChestTileEntity chest = (ChestTileEntity) this.world.getTileEntity(pos.down());
				    IInventory chestInv = HopperTileEntity.getInventoryAtPosition(this.world, pos.down() );
				    HopperTileEntity.putStackInInventoryAllSlots(null, chestInv, cobStack, null);
				}
				counter--;
			}

//				if (block instanceof DaytimeSensorBlock) {
//					DaytimeSensorBlock.updatePower(blockstate, this.world, this.pos);
//				}
		}
	}

	
	
	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
}
