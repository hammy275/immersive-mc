package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Intentionally not added to the JSON. Instead, this is added by the plugin so Forge doesn't have
// it.
@Mixin(FenceGateBlock.class)
public interface FenceGateBlockMixin {

    @Accessor("type")
    public WoodType immersiveMC$getType();
}
