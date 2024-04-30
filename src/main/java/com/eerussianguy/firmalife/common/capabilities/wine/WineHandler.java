package com.eerussianguy.firmalife.common.capabilities.wine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WineHandler implements IWine, ICapabilitySerializable<CompoundTag>
{
    private final ItemStack stack;
    private long creationDate = -1L;
    private long openDate = -1L;
    private final LazyOptional<IWine> capability = LazyOptional.of(() -> this);
    private boolean initialized = false;
    @Nullable private String labelText = null;

    public WineHandler(ItemStack stack)
    {
        this.stack = stack;
    }

    @Nullable
    @Override
    public String getLabelText()
    {
        return labelText;
    }

    @Override
    public void setLabelText(String labelText)
    {
        this.labelText = labelText;
        save();
    }

    @Override
    public long getCreationDate()
    {
        return creationDate;
    }

    @Override
    public void setCreationDate(long ticks)
    {
        this.creationDate = ticks;
        save();
    }

    @Override
    public long getOpenDate()
    {
        return openDate;
    }

    @Override
    public void setOpenDate(long ticks)
    {
        this.openDate = ticks;
        save();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) { }

    private void load()
    {
        if (!initialized)
        {
            initialized = true;

            final CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("wine", Tag.TAG_COMPOUND))
            {
                final CompoundTag wineTag = tag.getCompound("wine");
                creationDate = wineTag.getLong("creationDate");
                openDate = wineTag.getLong("openDate");
            }
        }
    }

    private void save()
    {
        final CompoundTag tag = stack.getOrCreateTag();
        final CompoundTag wineTag = new CompoundTag();
        wineTag.putLong("creationDate", creationDate);
        wineTag.putLong("openDate", openDate);
        tag.put("wine", wineTag);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == WineCapability.CAPABILITY)
        {
            load();
            return capability.cast();
        }
        return LazyOptional.empty();
    }
}