package com.eerussianguy.firmalife.compat.jei;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.eerussianguy.firmalife.FirmaLife;
import com.eerussianguy.firmalife.common.FLHelpers;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.items.FLItems;
import com.eerussianguy.firmalife.common.recipes.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.KnappingRecipe;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;


@JeiPlugin
public class FLJEIPlugin implements IModPlugin
{
    private static <T> RecipeType<T> type(String name, Class<T> tClass)
    {
        return RecipeType.create(FirmaLife.MOD_ID, name, tClass);
    }

    public static final RecipeType<DryingRecipe> DRYING = type("drying", DryingRecipe.class);
    public static final RecipeType<SmokingRecipe> SMOKING = type("smoking", SmokingRecipe.class);
    public static final RecipeType<StompingRecipe> STOMPING = type("stomping", StompingRecipe.class);
    public static final RecipeType<PressRecipe> PRESS = type("press", PressRecipe.class);
    public static final RecipeType<MixingBowlRecipe> MIXING_BOWL = type("mixing_bowl", MixingBowlRecipe.class);
    public static final RecipeType<KnappingRecipe> PUMPKIN_KNAPPING = type("pumpkin_knapping", KnappingRecipe.class);
    public static final RecipeType<OvenRecipe> OVEN = type("oven", OvenRecipe.class);
    public static final RecipeType<VatRecipe> VAT = type("vat", VatRecipe.class);
    public static final RecipeType<PotRecipe> BOWL_POT = type("bowl_pot", PotRecipe.class);
    public static final RecipeType<PotRecipe> STINKY_SOUP = type("stinky_soup", PotRecipe.class);

    @Override
    public ResourceLocation getPluginUid()
    {
        return FLHelpers.identifier("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r)
    {
        IGuiHelper gui = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new DryingCategory(DRYING, gui));
        r.addRecipeCategories(new SmokingCategory(SMOKING, gui));
        r.addRecipeCategories(new StompingCategory(STOMPING, gui));
        r.addRecipeCategories(new PressCategory(PRESS, gui));
        r.addRecipeCategories(new MixingCategory(MIXING_BOWL, gui));
        r.addRecipeCategories(new OvenCategory(OVEN, gui));
        r.addRecipeCategories(new VatCategory(VAT, gui));
        r.addRecipeCategories(new StinkySoupCategory(STINKY_SOUP, gui));
        r.addRecipeCategories(new BowlPotCategory(BOWL_POT, gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r)
    {
        r.addRecipes(DRYING, recipes(FLRecipeTypes.DRYING.get()));
        r.addRecipes(SMOKING, recipes(FLRecipeTypes.SMOKING.get()));
        r.addRecipes(STOMPING, recipes(FLRecipeTypes.STOMPING.get()));
        r.addRecipes(PRESS, recipes(FLRecipeTypes.PRESS.get()));
        r.addRecipes(MIXING_BOWL, recipes(FLRecipeTypes.MIXING_BOWL.get()));
        r.addRecipes(OVEN, recipes(FLRecipeTypes.OVEN.get()));
        r.addRecipes(VAT, recipes(FLRecipeTypes.VAT.get()));
        r.addRecipes(BOWL_POT, recipes(TFCRecipeTypes.POT.get(), recipe -> recipe.getSerializer() == FLRecipeSerializers.BOWL_POT.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r)
    {
        cat(r, FLBlocks.DRYING_MAT, DRYING);
        cat(r, FLBlocks.SOLAR_DRIER, DRYING);
        cat(r, TFCItems.WOOL_YARN.get(), SMOKING);
        cat(r, FLBlocks.MIXING_BOWL, MIXING_BOWL);
        cat(r, FLItems.SPOON.get(), MIXING_BOWL);
        cat(r, TFCBlocks.PUMPKIN, PUMPKIN_KNAPPING);
        FLBlocks.CURED_OVEN_TOP.values().forEach(oven -> cat(r, oven, OVEN));
        cat(r, FLBlocks.VAT, VAT);
        cat(r, TFCItems.POT.get(), BOWL_POT);
        cat(r, TFCItems.POT.get(), STINKY_SOUP);
        FLBlocks.STOMPING_BARRELS.values().forEach(barrel -> cat(r, barrel, STOMPING));
        FLBlocks.BARREL_PRESSES.values().forEach(barrel -> cat(r, barrel, PRESS));
    }

    private static void cat(IRecipeCatalystRegistration r, Supplier<? extends Block> supplier, RecipeType<?> type)
    {
        r.addRecipeCatalyst(new ItemStack(supplier.get()), type);
    }

    private static void cat(IRecipeCatalystRegistration r, Item item, RecipeType<?> type)
    {
        r.addRecipeCatalyst(new ItemStack(item), type);
    }

    private static <C extends Container, T extends Recipe<C>> List<T> recipes(net.minecraft.world.item.crafting.RecipeType<T> type, Predicate<T> filter)
    {
        return recipes(type).stream().filter(filter).collect(Collectors.toList());
    }

    private static <C extends Container, T extends Recipe<C>> List<T> recipes(net.minecraft.world.item.crafting.RecipeType<T> type)
    {
        return ClientHelpers.getLevelOrThrow().getRecipeManager().getAllRecipesFor(type);
    }

}
