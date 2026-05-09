package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.ItemRotationType;
import com.hammy275.immersivemc.client.immersive.TextData;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RelativeHitboxRenderState {

    public AABB aabb;
    public boolean holdsItems;
    public boolean renderItem;
    public ItemStack item;
    public boolean itemSpins;
    public boolean isInput;
    public float itemRenderSizeMultiplier;
    public ItemRotationType itemRotationType;
    public boolean renderItemCount;
    public Direction upDownRenderDir;
    public List<TextData> textData;

}
