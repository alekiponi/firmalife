package com.eerussianguy.firmalife.common;

import com.eerussianguy.firmalife.common.blockentities.OvenBottomBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.OvenTopBlockEntity;
import com.eerussianguy.firmalife.common.blocks.JarbnetBlock;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.eerussianguy.firmalife.common.blocks.OvenTopBlock;
import com.eerussianguy.firmalife.common.capabilities.player.FLPlayerData;
import com.eerussianguy.firmalife.common.capabilities.player.FLPlayerDataCapability;
import com.eerussianguy.firmalife.common.util.FLSelfTests;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;

import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.FLFluids;
import com.eerussianguy.firmalife.common.network.FLPackets;
import com.eerussianguy.firmalife.common.util.ExtraFluid;
import com.eerussianguy.firmalife.common.util.GreenhouseType;
import com.eerussianguy.firmalife.common.util.Plantable;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.items.CandleBlockItem;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.dries007.tfc.util.events.DouseFireEvent;
import net.dries007.tfc.util.events.StartFireEvent;

public class FLForgeEvents
{
    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;

        bus.addListener(FLForgeEvents::onFireStart);
        bus.addListener(FLForgeEvents::onFireStop);
        bus.addListener(FLForgeEvents::addReloadListeners);
        bus.addListener(FLForgeEvents::onDataPackSync);
        bus.addListener(FLForgeEvents::onAnimalProduce);
        bus.addListener(FLForgeEvents::onLogin);
        bus.addListener(FLForgeEvents::onLevelLoad);
        //bus.addListener(FLForgeEvents::onEntityCaps); use generic listener
    }

    public static void onLevelLoad(LevelEvent.Load event)
    {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD)
        {
            FLSelfTests.runServerSelfTests();
        }
    }

    public static void onEntityCaps(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player)
        {
            event.addCapability(FLPlayerDataCapability.KEY, new FLPlayerData(player));
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        TFCConfig.SERVER.enablePumpkinCarving.set(false);
    }

    public static void addReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(GreenhouseType.MANAGER);
        event.addListener(Plantable.MANAGER);
    }

    public static void onDataPackSync(OnDatapackSyncEvent event)
    {
        final ServerPlayer player = event.getPlayer();
        final PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);

        FLPackets.send(target, GreenhouseType.MANAGER.createSyncPacket());
        FLPackets.send(target, Plantable.MANAGER.createSyncPacket());
    }

    public static void onFireStart(StartFireEvent event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (block instanceof OvenBottomBlock)
        {
            level.getBlockEntity(pos, FLBlockEntities.OVEN_BOTTOM.get()).ifPresent(oven -> oven.light(state));
            event.setCanceled(true);
        }
        else if (block instanceof CarvedPumpkinBlock)
        {
            FLBlocks.CARVED_PUMPKINS.forEach((carve, reg) -> {
                if (block == reg.get())
                {
                    level.setBlockAndUpdate(pos, Helpers.copyProperty(FLBlocks.JACK_O_LANTERNS.get(carve).get().defaultBlockState(), state, HorizontalDirectionalBlock.FACING));
                    FLHelpers.resetCounter(level, pos);
                    event.setCanceled(true);
                }
            });
        }
        else if (block instanceof JarbnetBlock)
        {
            FLHelpers.readInventory(level, pos, FLBlockEntities.JARBNET, (jarbnet, inv) -> {
                for (ItemStack stack : Helpers.iterate(inv))
                {
                    if (stack.getItem() instanceof CandleBlockItem)
                    {
                        level.setBlockAndUpdate(pos, state.setValue(JarbnetBlock.LIT, true));
                        jarbnet.resetCounter();
                        event.setCanceled(true);
                        break;
                    }
                }
            });
        }
    }

    public static void onFireStop(DouseFireEvent event)
    {
        final BlockState state = event.getState();
        final Level level = event.getLevel();
        final BlockPos pos = event.getPos();

        if (state.getBlock() instanceof OvenBottomBlock && level.getBlockEntity(pos) instanceof OvenBottomBlockEntity bottom)
        {
            bottom.extinguish(state);
            Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
            event.setCanceled(true);
        }
        else if (state.getBlock() instanceof OvenTopBlock && level.getBlockEntity(pos) instanceof OvenTopBlockEntity top)
        {
            top.extinguish();
            Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
            event.setCanceled(true);
        }
    }

    public static void onAnimalProduce(AnimalProductEvent event)
    {
        final EntityType<?> type = event.getEntity().getType();
        if (type == TFCEntities.YAK.get())
        {
            event.setProduct(new FluidStack(FLFluids.EXTRA_FLUIDS.get(ExtraFluid.YAK_MILK).getSource(), 1000));
        }
        else if (type == TFCEntities.GOAT.get())
        {
            event.setProduct(new FluidStack(FLFluids.EXTRA_FLUIDS.get(ExtraFluid.GOAT_MILK).getSource(), 1000));
        }
    }

}
