package com.eerussianguy.firmalife.client;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.eerussianguy.firmalife.FirmaLife;
import com.eerussianguy.firmalife.client.model.BonsaiPlanterBlockModel;
import com.eerussianguy.firmalife.client.model.FoodShelfBlockModel;
import com.eerussianguy.firmalife.client.model.HangerBlockModel;
import com.eerussianguy.firmalife.client.model.HangingPlanterBlockModel;
import com.eerussianguy.firmalife.client.model.HydroponicPlanterBlockModel;
import com.eerussianguy.firmalife.client.model.JarbnetBlockModel;
import com.eerussianguy.firmalife.client.model.JarringStationBlockModel;
import com.eerussianguy.firmalife.client.model.LargePlanterBakedModel;
import com.eerussianguy.firmalife.client.model.PeelModel;
import com.eerussianguy.firmalife.client.model.DynamicBlockModel;
import com.eerussianguy.firmalife.client.model.QuadPlanterBlockModel;
import com.eerussianguy.firmalife.client.model.TrellisPlanterBlockModel;
import com.eerussianguy.firmalife.client.model.WineShelfBlockModel;
import com.eerussianguy.firmalife.client.screen.BigBarrelScreen;
import com.eerussianguy.firmalife.client.screen.StovetopGrillScreen;
import com.eerussianguy.firmalife.client.screen.StovetopPotScreen;
import com.eerussianguy.firmalife.common.FLCreativeTabs;
import com.eerussianguy.firmalife.common.capabilities.bee.BeeCapability;
import com.eerussianguy.firmalife.common.capabilities.bee.IBee;
import com.eerussianguy.firmalife.common.items.WineBottleItem;
import com.eerussianguy.firmalife.common.misc.SprinklerParticle;
import com.eerussianguy.firmalife.common.util.FLFruit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.eerussianguy.firmalife.client.render.*;
import com.eerussianguy.firmalife.client.screen.BeehiveScreen;
import com.eerussianguy.firmalife.common.FLHelpers;
import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.container.FLContainerTypes;
import com.eerussianguy.firmalife.common.entities.FLEntities;
import com.eerussianguy.firmalife.common.misc.FLParticles;
import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.eerussianguy.firmalife.common.items.FLItems;
import net.minecraftforge.registries.ForgeRegistries;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.client.particle.GlintParticleProvider;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.items.TFCItems;

public class FLClientEvents
{

    public static void init()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(FLClientEvents::clientSetup);
        bus.addListener(FLClientEvents::registerEntityRenderers);
        bus.addListener(FLClientEvents::onLayers);
        bus.addListener(FLClientEvents::onModelRegister);
        bus.addListener(FLClientEvents::onBlockColors);
        bus.addListener(FLClientEvents::onItemColors);
        bus.addListener(FLClientEvents::registerParticleFactories);
        bus.addListener(FLClientEvents::registerModels);
        bus.addListener(FLClientEvents::registerLoaders);
        bus.addListener(FLCreativeTabs::onBuildCreativeTab);
    }

    @SuppressWarnings("deprecation")
    public static void clientSetup(FMLClientSetupEvent event)
    {
        // Render Types
        final RenderType solid = RenderType.solid();
        final RenderType cutout = RenderType.cutout();
        final RenderType cutoutMipped = RenderType.cutoutMipped();
        final RenderType translucent = RenderType.translucent();

        Stream.of(FLBlocks.OVEN_BOTTOM, FLBlocks.OVEN_TOP, FLBlocks.OVEN_CHIMNEY, FLBlocks.OVEN_HOPPER,
            FLBlocks.QUAD_PLANTER, FLBlocks.LARGE_PLANTER, FLBlocks.HANGING_PLANTER, FLBlocks.BONSAI_PLANTER, FLBlocks.TRELLIS_PLANTER,
            FLBlocks.COMPOST_TUMBLER, FLBlocks.CHEDDAR_WHEEL,
            FLBlocks.RAJYA_METOK_WHEEL, FLBlocks.CHEVRE_WHEEL, FLBlocks.SHOSHA_WHEEL, FLBlocks.FETA_WHEEL, FLBlocks.GOUDA_WHEEL, FLBlocks.SMALL_CHROMITE,
            FLBlocks.MIXING_BOWL, FLBlocks.BUTTERFLY_GRASS, FLBlocks.VAT, FLBlocks.HYDROPONIC_PLANTER, FLBlocks.STOVETOP_GRILL, FLBlocks.STOVETOP_POT,
            FLBlocks.DARK_LADDER, FLBlocks.JARRING_STATION, FLBlocks.GRAPE_TRELLIS_POST_RED, FLBlocks.GRAPE_TRELLIS_POST_WHITE, FLBlocks.GRAPE_TRELLIS_POST,
            FLBlocks.GRAPE_STRING, FLBlocks.GRAPE_STRING_RED, FLBlocks.GRAPE_STRING_WHITE, FLBlocks.GRAPE_STRING_PLANT_RED, FLBlocks.GRAPE_STRING_PLANT_WHITE
        ).forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));

        ItemBlockRenderTypes.setRenderLayer(FLBlocks.SOLAR_DRIER.get(), translucent);

        FLBlocks.CHROMITE_ORES.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));
        FLBlocks.GREENHOUSE_BLOCKS.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));
        FLBlocks.FRUIT_TREE_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == solid));
        FLBlocks.FRUIT_TREE_SAPLINGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.FRUIT_TREE_POTTED_SAPLINGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.STATIONARY_BUSHES.values().forEach(bush -> ItemBlockRenderTypes.setRenderLayer(bush.get(), cutoutMipped));
        FLBlocks.HERBS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.POTTED_HERBS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.FOOD_SHELVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.HANGERS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.JARBNETS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.STOMPING_BARRELS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.BARREL_PRESSES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.WINE_SHELVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.CURED_OVEN_BOTTOM.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.CURED_OVEN_TOP.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.CURED_OVEN_HOPPER.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.CURED_OVEN_CHIMNEY.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        FLBlocks.INSULATED_OVEN_BOTTOM.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        ItemBlockRenderTypes.setRenderLayer(FLBlocks.REINFORCED_POURED_GLASS.get(), translucent);

        event.enqueueWork(() -> {
            MenuScreens.register(FLContainerTypes.BEEHIVE.get(), BeehiveScreen::new);
            MenuScreens.register(FLContainerTypes.STOVETOP_GRILL.get(), StovetopGrillScreen::new);
            MenuScreens.register(FLContainerTypes.STOVETOP_POT.get(), StovetopPotScreen::new);
            MenuScreens.register(FLContainerTypes.BIG_BARREL.get(), BigBarrelScreen::new);

            TFCItems.FOOD.forEach((food, item) -> {
                if (FLItems.TFC_FRUITS.contains(food))
                {
                    registerDryProperty(item);
                }
            });
            FLItems.FRUITS.forEach((food, item) -> registerDryProperty(item));

            ItemProperties.register(FLItems.BEEHIVE_FRAME.get(), FLHelpers.identifier("queen"), (stack, a, b, c) -> stack.getCapability(BeeCapability.CAPABILITY).map(IBee::hasQueen).orElse(false) ? 1f : 0f);

        });
    }

    public static void onBlockColors(RegisterColorHandlersEvent.Block event)
    {
        final BlockColor tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);

        event.register(tallGrassColor, FLBlocks.BUTTERFLY_GRASS.get());
        event.register(tallGrassColor, FLBlocks.POTTED_BUTTERFLY_GRASS.get());
    }

    public static void onItemColors(RegisterColorHandlersEvent.Item event)
    {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);

        Stream.of(FLBlocks.BUTTERFLY_GRASS).forEach(reg -> event.register(grassColor, reg.get()));

        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues())
        {
            if (Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).getNamespace().equals(FirmaLife.MOD_ID))
            {
                event.register(new DynamicFluidContainerModel.Colors(), fluid.getBucket());
            }
        }
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(FLParticles.GROWTH.get(), set -> new GlintParticleProvider(set, ChatFormatting.GREEN));
        event.registerSpriteSet(FLParticles.SPRINKLER.get(), set -> SprinklerParticle.provider(set, SprinklerParticle::new));
    }

    public static void registerModels(ModelEvent.RegisterAdditional event)
    {
        for (FLFruit fruit : FLFruit.values())
        {
            event.register(FLHelpers.identifier("block/jar/" + fruit.getSerializedName()));
            event.register(FLHelpers.identifier("block/jar/" + fruit.getSerializedName() + "_unsealed"));
        }
        event.register(FLHelpers.identifier("block/jar/compost"));
        event.register(FLHelpers.identifier("block/jar/rotten_compost"));
        event.register(FLHelpers.identifier("block/jar/guano"));
        event.register(FLHelpers.identifier("block/jar/honey"));
        event.register(FLHelpers.identifier("block/barrel_press_piston"));

        for (Item item : ForgeRegistries.ITEMS.getValues())
        {
            if (item instanceof WineBottleItem wine)
            {
                event.register(wine.getModelLocation());
            }
        }
    }

    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("large_planter", new DynamicBlockModel.Loader(LargePlanterBakedModel::new));
        event.register("hanging_planter", new DynamicBlockModel.Loader(HangingPlanterBlockModel::new));
        event.register("bonsai_planter", new DynamicBlockModel.Loader(BonsaiPlanterBlockModel::new));
        event.register("quad_planter", new DynamicBlockModel.Loader(QuadPlanterBlockModel::new));
        event.register("hydroponic_planter", new DynamicBlockModel.Loader(HydroponicPlanterBlockModel::new));
        event.register("trellis_planter", new DynamicBlockModel.Loader(TrellisPlanterBlockModel::new));
        event.register("jarbnet", new DynamicBlockModel.Loader(JarbnetBlockModel::new));
        event.register("jarring_station", new DynamicBlockModel.Loader(JarringStationBlockModel::new));
        event.register("food_shelf", new DynamicBlockModel.Loader(FoodShelfBlockModel::new));
        event.register("hanger", new DynamicBlockModel.Loader(HangerBlockModel::new));
        event.register("wine_shelf", new DynamicBlockModel.Loader(WineShelfBlockModel::new));
    }

    private static void registerDryProperty(Supplier<Item> item)
    {
        ItemProperties.register(item.get(), FLHelpers.identifier("dry"), (stack, a, b, c) ->
            stack.getCapability(FoodCapability.CAPABILITY)
                .map(cap -> cap.getTraits().contains(FLFoodTraits.DRIED))
                .orElse(false) ? 1f : 0f);
    }


    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(FLBlockEntities.OVEN_TOP.get(), ctx -> new OvenBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.DRYING_MAT.get(), ctx -> new DryingMatBlockEntityRenderer(2f / 16));
        event.registerBlockEntityRenderer(FLBlockEntities.SOLAR_DRIER.get(), ctx -> new DryingMatBlockEntityRenderer(1f / 16));
        event.registerBlockEntityRenderer(FLBlockEntities.STRING.get(), ctx -> new StringBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.VAT.get(), ctx -> new VatBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.MIXING_BOWL.get(), ctx -> new MixingBowlBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.STOVETOP_GRILL.get(), ctx -> new StovetopGrillBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.STOVETOP_POT.get(), ctx -> new StovetopPotBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.PLATE.get(), ctx -> new PlateBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.HYDROPONIC_PLANTER.get(), ctx -> new HydroponicPlanterBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.COMPOST_TUMBLER.get(), ctx -> new CompostTumblerBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.STOMPING_BARREL.get(), ctx -> new StompingBarrelBlockEntityRenderer());
        event.registerBlockEntityRenderer(FLBlockEntities.BARREL_PRESS.get(), ctx -> new BarrelPressBlockEntityRenderer());

        event.registerEntityRenderer(FLEntities.SEED_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FLEntities.FLBEE.get(), FLBeeRenderer::new);
    }

    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(FLClientHelpers.modelIdentifier("peel"), PeelModel::createBodyLayer);
    }


    public static void onModelRegister(ModelEvent.RegisterAdditional event)
    {
        event.register(MixingBowlBlockEntityRenderer.SPOON_LOCATION);
        event.register(JarbnetBlockModel.JUG_LOCATION);
        event.register(CompostTumblerBlockEntityRenderer.OPEN_MODEL);
        event.register(CompostTumblerBlockEntityRenderer.CLOSED_MODEL);
    }
}
