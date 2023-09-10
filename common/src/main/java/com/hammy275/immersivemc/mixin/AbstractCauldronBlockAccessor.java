package com.hammy275.immersivemc.mixin;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockAccessor {

    @Accessor("interactions")
    public Map<Item, CauldronInteraction> getInteractions();
}
