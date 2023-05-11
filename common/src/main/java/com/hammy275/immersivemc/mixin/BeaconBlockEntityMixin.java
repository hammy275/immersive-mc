package com.hammy275.immersivemc.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityMixin {

    @Accessor("levels")
    public int getLevels();

    @Accessor("primaryPower")
    public MobEffect getPrimaryPower();

    @Accessor("secondaryPower")
    public MobEffect getSecondaryPower();

    @Accessor("dataAccess")
    public ContainerData getBeaconData();
}
