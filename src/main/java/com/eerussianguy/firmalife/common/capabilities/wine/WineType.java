package com.eerussianguy.firmalife.common.capabilities.wine;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.util.climate.KoppenClimateClassification;

public enum WineType implements StringRepresentable
{
    RED(0xFFa10d00),
    WHITE(0xFFfff3f2),
    ROSE(0xFFf5cac6),
    SPARKLING(0xFFffeed4),
    DESSERT(0xFFf2ebdf)
    ;

    public static final WineType[] VALUES = values();
    public static final KoppenClimateClassification[] KOPPEN_VALUES = KoppenClimateClassification.values();

    private final String name;
    private final int color;

    WineType(int color)
    {
       this.name = name().toLowerCase(Locale.ROOT) + "_wine";
       this.color = color;
    }

    public int getColor()
    {
        return color;
    }

    @Override
    @NotNull
    public String getSerializedName()
    {
        return name;
    }
}
