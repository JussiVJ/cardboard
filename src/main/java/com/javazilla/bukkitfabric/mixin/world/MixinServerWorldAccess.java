package com.javazilla.bukkitfabric.mixin.world;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;

@Mixin(ServerWorldAccess.class)
public interface MixinServerWorldAccess extends WorldAccess {

    default boolean addAllEntities(Entity entity) {
        entity.streamPassengersRecursively().forEach(this::spawnEntity);
        //return !entity.removed();
        return entity.isAlive();
    }

}