
package com.mactso.redstonemagic.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mactso.redstonemagic.Main;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class RedstoneMagicRecipe extends ShapelessRecipe {
	protected final String operation;
	private final boolean copyDamage;
	public static final Serializer CRAFTING_REDSTONEMAGIC = (Serializer) new Serializer().setRegistryName(Serializer.NAME);

	public RedstoneMagicRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn,
			NonNullList<Ingredient> recipeItemsIn, String operationIn) {
		super(idIn, groupIn, recipeOutputIn, recipeItemsIn);
		operation = operationIn;
		boolean copyDamage = false;
		if (recipeOutputIn.isDamageable()) {
			for (Ingredient thing : recipeItemsIn) {
				for (ItemStack stack : thing.getMatchingStacks()) {
					if (stack.isDamageable() && stack.getMaxDamage() == recipeOutputIn.getMaxDamage()) {
						copyDamage = true;
						break;
					}
				}
			}
		}
		this.copyDamage = copyDamage;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return CRAFTING_REDSTONEMAGIC;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack ret = this.getRecipeOutput().copy();
		if (copyDamage) {
			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				ItemStack itemstack = inv.getStackInSlot(j);
				if (!itemstack.isEmpty()) {
					if (itemstack.isDamageable() && itemstack.getMaxDamage() == ret.getMaxDamage()) {
						ret.setDamage(itemstack.getDamage());
						ret.setRepairCost(itemstack.getRepairCost());
						ret.setDisplayName(itemstack.getDisplayName());
						if (itemstack.hasTag()) {
							ret.setTag(itemstack.getTag().copy());
						}
						break;
					}
				}
			}
		}
		if (operation.equals("reset_color")) {
			CompoundNBT compoundnbt = ret.getChildTag("display");
			if (compoundnbt != null && compoundnbt.contains("color", 99))
				compoundnbt.remove("color");
		} else if (operation.equals("set_color")) {
			int color = -1;
			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				ItemStack itemstack = inv.getStackInSlot(j);
				if (!itemstack.isEmpty()) {
					Item item = itemstack.getItem();
					if (item instanceof DyeItem) {
						if (item.equals(Items.WHITE_DYE)) {
							color = 0xFFFFFF;
						} else if (item.equals(Items.RED_DYE)) {
							color = 0xFF0000;
						} else if (item.equals(Items.GREEN_DYE)) {
							color = 0x00FF00;
						} else if (item.equals(Items.BLUE_DYE)) {
							color = 0x0000FF;
						} else if (item.equals(Items.BLACK_DYE)) {
							color = 0x000000;
						}
						break;
					}
				}
			}
			if (color >= 0) {
				CompoundNBT compoundnbt = ret.getOrCreateChildTag("display");
				compoundnbt.putInt("color", color);
			} else
				ret = ItemStack.EMPTY;
		} else if (operation.equals("remove")) {
			CompoundNBT compoundnbt = ret.getChildTag("display");
			if (compoundnbt != null) {
				compoundnbt.remove("color");
				compoundnbt.remove("glint");
				if (compoundnbt.isEmpty())
					ret.removeChildTag("display");
			}
		}
		return ret;
	}

	public static class Serializer extends ShapelessRecipe.Serializer {
		private static final ResourceLocation NAME = new ResourceLocation(Main.MODID, "crafting_shapeless");

		@Override
		public RedstoneMagicRecipe read(ResourceLocation recipeId, JsonObject json) {
			String s = JSONUtils.getString(json, "group", "");
			NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
//		        } else if (nonnulllist.size() > ShapedRecipe.getWidth() * ShapedRecipe.getHeight()) {
//		           throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + (ShapedRecipe.getWidth() * ShapedRecipe.getHeight()));
			} else {
				ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
				String s2 = JSONUtils.getString(json, "operation", "");
				return new RedstoneMagicRecipe(recipeId, s, itemstack, nonnulllist, s2);
			}
		}

		private static NonNullList<Ingredient> readIngredients(JsonArray array) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for (int i = 0; i < array.size(); ++i) {
				Ingredient ingredient = Ingredient.deserialize(array.get(i));
				if (!ingredient.hasNoMatchingItems()) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		@Override
		public RedstoneMagicRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			String s = buffer.readString(32767);
			int i = buffer.readVarInt();
			NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

			for (int j = 0; j < nonnulllist.size(); ++j) {
				nonnulllist.set(j, Ingredient.read(buffer));
			}

			ItemStack itemstack = buffer.readItemStack();
			String s2 = buffer.readString(32767);
			return new RedstoneMagicRecipe(recipeId, s, itemstack, nonnulllist, s2);
		}

		@Override
		public void write(PacketBuffer buffer, ShapelessRecipe recipe) {
			super.write(buffer, recipe);
			buffer.writeString(((RedstoneMagicRecipe) recipe).operation);
		}
	}
}
