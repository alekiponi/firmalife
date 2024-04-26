package com.eerussianguy.firmalife.common.entities;

import java.util.Locale;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.eerussianguy.firmalife.FirmaLife;

public class FLEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FirmaLife.MOD_ID);

    public static final RegistryObject<EntityType<SeedBall>> SEED_BALL = register("seed_ball", EntityType.Builder.<SeedBall>of(SeedBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    public static final RegistryObject<EntityType<FLBee>> FLBEE = register("bee", EntityType.Builder.<FLBee>of(FLBee::new, MobCategory.CREATURE).sized(0.2F, 0.2F).clientTrackingRange(2));

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder)
    {
        return register(name, builder, true);
    }

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder, boolean serialize)
    {
        final String id = name.toLowerCase(Locale.ROOT);
        return ENTITIES.register(id, () -> {
            if (!serialize) builder.noSave();
            return builder.build(FirmaLife.MOD_ID + ":" + id);
        });
    }
}
