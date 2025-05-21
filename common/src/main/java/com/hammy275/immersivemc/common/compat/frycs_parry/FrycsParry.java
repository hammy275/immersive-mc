package com.hammy275.immersivemc.common.compat.frycs_parry;

import com.hammy275.immersivemc.common.compat.util.CompatModule;
import net.minecraft.world.item.ItemStack;

public interface FrycsParry {
    public static final FrycsParry INSTANCE = CompatModule.create(new FrycsParryImpl(), FrycsParry.class, FrycsParryImpl.compatData);

    public boolean isShield(ItemStack stack);
}
