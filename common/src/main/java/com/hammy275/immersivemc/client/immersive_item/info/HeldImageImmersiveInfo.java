package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import org.vivecraft.api.data.VRBodyPartData;

import java.util.function.BiConsumer;

public class HeldImageImmersiveInfo<T> extends AbstractHandImmersiveInfo {

    public final Identifier heldImage;
    public final BiConsumer<HeldImageImmersiveInfo<T>, VRBodyPartData> ticker;
    public final Identifier immersiveId;
    public final T heldData;
    public final float size;
    public int light;

    public HeldImageImmersiveInfo(InteractionHand hand, Identifier heldImage, Identifier immersiveId,
                                  T heldData, float size, BiConsumer<HeldImageImmersiveInfo<T>, VRBodyPartData> ticker) {
        super(hand);
        this.heldImage = heldImage;
        this.immersiveId = immersiveId;
        this.heldData = heldData;
        this.size = size;
        this.ticker = ticker;
    }
}
