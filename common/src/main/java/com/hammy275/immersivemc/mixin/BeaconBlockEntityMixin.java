package com.hammy275.immersivemc.mixin;

import net.minecraft.core.Holder;
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
    public Holder<MobEffect> getPrimaryPower();

    @Accessor("secondaryPower")
    public Holder<MobEffect> getSecondaryPower();

    @Accessor("dataAccess")
    public ContainerData getBeaconData();
}
