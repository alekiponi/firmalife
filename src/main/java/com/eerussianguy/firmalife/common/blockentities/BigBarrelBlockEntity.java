package com.eerussianguy.firmalife.common.blockentities;

import com.eerussianguy.firmalife.common.FLHelpers;
import com.eerussianguy.firmalife.common.container.BigBarrelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.BarrelInventoryCallback;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.DelegateFluidHandler;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.PartialFluidHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.BarrelBlockItem;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.dries007.tfc.util.Helpers;


public class BigBarrelBlockEntity extends InventoryBlockEntity<BigBarrelBlockEntity.BigBarrelInventory> implements BarrelInventoryCallback
{
    public static final int SLOTS = 38;
    public static final int SLOT_FLUID_CONTAINER_IN = 36;
    public static final int SLOT_FLUID_CONTAINER_OUT = 37;
    public static final int CAPACITY = 80000;

    private final SidedHandler.Builder<IFluidHandler> sidedFluidInventory;

    public BigBarrelBlockEntity(BlockPos pos, BlockState state)
    {
        super(FLBlockEntities.BIG_BARREL.get(), pos, state, BigBarrelInventory::new, FLHelpers.blockEntityName("big_barrel"));

        sidedFluidInventory = new SidedHandler.Builder<>(inventory);
        sidedFluidInventory
            .on(new PartialFluidHandler(inventory).insert(), d -> d.getAxis().isHorizontal())
            .on(new PartialFluidHandler(inventory).extract(), d -> d.getAxis().isVertical());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        if (slot == SLOT_FLUID_CONTAINER_IN)
        {
            return Helpers.mightHaveCapability(stack, Capabilities.FLUID_ITEM);
        }
        if (slot == SLOT_FLUID_CONTAINER_OUT)
        {
            return true;
        }
        return ItemSizeManager.get(stack).getSize(stack).isSmallerThan(Size.VERY_LARGE) && !(stack.getItem() instanceof BarrelBlockItem);
    }

    @Override
    public boolean canModify()
    {
        return true;
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        if (slot == SLOT_FLUID_CONTAINER_IN)
        {
            updateFluidIOSlots();
        }
    }

    @Override
    public void fluidTankChanged()
    {
        setChanged();
    }

    private void updateFluidIOSlots()
    {
        assert level != null;
        final ItemStack input = inventory.getStackInSlot(SLOT_FLUID_CONTAINER_IN);
        if (!input.isEmpty() && inventory.getStackInSlot(SLOT_FLUID_CONTAINER_OUT).isEmpty())
        {
            FluidHelpers.transferBetweenBlockEntityAndItem(input, this, level, worldPosition, (newOriginalStack, newContainerStack) -> {
                if (newContainerStack.isEmpty())
                {
                    // No new container was produced, so shove the first stack in the output, and clear the input
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_IN, ItemStack.EMPTY);
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_OUT, newOriginalStack);
                }
                else
                {
                    // We produced a new container - this will be the 'filled', so we need to shove *that* in the output
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_IN, newOriginalStack);
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_OUT, newContainerStack);
                }
            });
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
    {
        if (cap == Capabilities.FLUID)
        {
            return sidedFluidInventory.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return BigBarrelContainer.create(this, player.getInventory(), containerId);
    }

    public static class BigBarrelInventory implements EmptyInventory, DelegateItemHandler, INBTSerializable<CompoundTag>, DelegateFluidHandler, FluidTankCallback
    {
        private final InventoryItemHandler inventory;
        private final InventoryFluidTank tank;

        BigBarrelInventory(InventoryBlockEntity<?> inventory)
        {
            this((BarrelInventoryCallback) inventory);
        }

        BigBarrelInventory(BarrelInventoryCallback inventory)
        {
            this.inventory = new InventoryItemHandler(inventory, SLOTS);
            tank = new InventoryFluidTank(CAPACITY, stack -> Helpers.isFluid(stack.getFluid(), TFCTags.Fluids.USABLE_IN_BARREL), this);
        }

        @Override
        public IFluidHandler getFluidHandler()
        {
            return tank;
        }

        @Override
        public IItemHandlerModifiable getItemHandler()
        {
            return inventory;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", inventory.serializeNBT());
            nbt.put("tank", tank.writeToNBT(new CompoundTag()));
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            tank.readFromNBT(nbt.getCompound("tank"));
        }
    }
}
