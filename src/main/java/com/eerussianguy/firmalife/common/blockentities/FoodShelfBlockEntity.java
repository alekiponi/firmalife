package com.eerussianguy.firmalife.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import com.eerussianguy.firmalife.common.FLHelpers;
import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.dries007.tfc.util.Helpers;

public class FoodShelfBlockEntity extends InventoryBlockEntity<ItemStackHandler> implements ClimateReceiver
{
    private boolean climateValid = false;

    public FoodShelfBlockEntity(BlockPos pos, BlockState state)
    {
        this(FLBlockEntities.FOOD_SHELF.get(), pos, state);
    }

    public FoodShelfBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, defaultInventory(1), FLHelpers.blockEntityName("food_shelf"));
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        super.loadAdditional(nbt);
        climateValid = nbt.getBoolean("climateValid");
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putBoolean("climateValid", climateValid);
    }

    public boolean isClimateValid()
    {
        return climateValid;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        updatePreservation(climateValid);
    }

    @Override
    public void ejectInventory()
    {
        updatePreservation(false);
        super.ejectInventory();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        final ItemStack current = inventory.getStackInSlot(slot);
        if (!current.isEmpty())
        {
            ItemStack currentWithoutTrait = current.copy();
            FoodCapability.removeTrait(currentWithoutTrait, getFoodTrait());
            return FoodCapability.areStacksStackableExceptCreationDate(stack, currentWithoutTrait);
        }
        return stack.getCapability(FoodCapability.CAPABILITY).isPresent();
    }

    public FoodTrait getFoodTrait()
    {
        return FLFoodTraits.SHELVED;
    }

    public InteractionResult use(Player player, InteractionHand hand)
    {
        assert level != null;
        var res = InteractionResult.PASS;
        final ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && isItemValid(0, held))
        {
            if (climateValid)
            {
                FoodCapability.applyTrait(held, getFoodTrait());
            }
            player.setItemInHand(hand, Helpers.mergeInsertStack(inventory, 0, held));

            FoodCapability.removeTrait(player.getItemInHand(hand), getFoodTrait());
            res = InteractionResult.sidedSuccess(level.isClientSide);
        }
        else if (player.isShiftKeyDown())
        {
            ItemStack stack = inventory.extractItem(0, Integer.MAX_VALUE, false);
            if (stack.isEmpty()) return InteractionResult.PASS;
            FoodCapability.removeTrait(stack, getFoodTrait());
            ItemHandlerHelper.giveItemToPlayer(player, stack);
            res = InteractionResult.sidedSuccess(level.isClientSide);
        }
        updatePreservation(climateValid);
        markForSync();
        return res;
    }

    public void updatePreservation(boolean preserved)
    {
        if (preserved)
        {
            FoodCapability.applyTrait(inventory.getStackInSlot(0), getFoodTrait());
        }
        else
        {
            FoodCapability.removeTrait(inventory.getStackInSlot(0), getFoodTrait());
        }
    }

    @Override
    public void addWater(float amount) { }

    @Override
    public void setValid(Level level, BlockPos pos, boolean valid, int tier, boolean cellar)
    {
        if (cellar)
        {
            climateValid = valid;
            updatePreservation(valid);
        }
        markForSync();
    }

}