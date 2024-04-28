package com.eerussianguy.firmalife.common.blockentities;

import com.eerussianguy.firmalife.common.FLHelpers;
import com.eerussianguy.firmalife.common.FLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.util.Helpers;

public class WineShelfBlockEntity extends InventoryBlockEntity<ItemStackHandler>
{
    public WineShelfBlockEntity(BlockPos pos, BlockState state)
    {
        super(FLBlockEntities.WINE_SHELF.get(), pos, state, defaultInventory(4), FLHelpers.blockEntityName("wine_shelf"));
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return Helpers.isItem(stack, FLTags.Items.WINE_BOTTLES);
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return 1;
    }
}
