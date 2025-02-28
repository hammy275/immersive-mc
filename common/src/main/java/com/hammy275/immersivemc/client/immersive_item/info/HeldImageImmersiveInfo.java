package com.hammy275.immersivemc.client.immersive_item.info;

import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.function.BiConsumer;

public class HeldImageImmersiveInfo<T> extends AbstractHandImmersiveInfo {

    public final ResourceLocation heldImage;
    public final BiConsumer<HeldImageImmersiveInfo<T>, IVRData> ticker;
    public final ResourceLocation immersiveId;
    public final T heldData;
    public final float size;
    public int light;

    public HeldImageImmersiveInfo(InteractionHand hand, ResourceLocation heldImage, ResourceLocation immersiveId,
                                  T heldData, float size, BiConsumer<HeldImageImmersiveInfo<T>, IVRData> ticker) {
        super(hand);
        this.heldImage = heldImage;
        this.immersiveId = immersiveId;
        this.heldData = heldData;
        this.size = size;
        this.ticker = ticker;
    }
}
