package com.mactso.redstonemagic.events;

import com.mactso.redstonemagic.config.MyConfig;
import com.mactso.redstonemagic.magic.CapabilityMagic;
import com.mactso.redstonemagic.magic.IMagicStorage;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MyBreakEvent {

    @SubscribeEvent
    public void onBlockBreak(BreakEvent event)
    {
    	BlockState state = event.getState();
    	Block block = state.getBlock();
    	if (block == Blocks.REDSTONE_ORE && event.getExpToDrop() > 0)
    	{
    		int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, event.getPlayer().getHeldItemMainhand());

    		BlockPos pos = event.getPos();
    		IWorld world = event.getWorld();
    		IChunk ichunk = world.getChunk(pos);
			double randint = world.getRandom().nextDouble();
			randint *= 3.0;
			int redstoneMagicIncrease = 3+(int)randint+bonusLevel;

    		if (event.getPlayer() instanceof ServerPlayerEntity) {
    			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) event.getPlayer();
    			LazyOptional<IMagicStorage> optPlayer = serverPlayerEntity.getCapability(CapabilityMagic.MAGIC);
    			if (optPlayer.isPresent())
    			{
    				IMagicStorage cap = optPlayer.orElseGet(null);
   					cap.addMana(redstoneMagicIncrease);  // checks for max capacity internally based on object type.
    			}
    		}
    		
//    		java.util.List<ItemStack> list = event.getDrops();
//    		int redstoneMagicAmount = 0;
//    		for (ItemStack stack:list) {
//    			if (stack.getItem()==Items.REDSTONE) {
//                    redstoneMagicAmount += stack.getCount(); 				
//    			}
//    		}
    		if (ichunk instanceof Chunk)
    		{
    			Chunk chunk = (Chunk) ichunk;
    			LazyOptional<IMagicStorage> optChunk = chunk.getCapability(CapabilityMagic.MAGIC);
    			if (optChunk.isPresent())
    			{
    				IMagicStorage cap = optChunk.orElseGet(null);
    				if (cap.getManaStored()+redstoneMagicIncrease <= MyConfig.maxChunkRedstoneMagic) {
    					cap.addMana(redstoneMagicIncrease);
    				}
    				System.out.println("Increase Redstone Chunk Magic by " + redstoneMagicIncrease + " to " + cap.getManaStored() + ".");
    			}
    		}
    	}
    }

}
