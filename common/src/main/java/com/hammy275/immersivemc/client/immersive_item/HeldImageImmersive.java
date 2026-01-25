package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.client.immersive_item.info.HeldImageImmersiveInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.vivecraft.api.data.VRBodyPartData;

import java.util.List;
import java.util.function.BiConsumer;

public class HeldImageImmersive extends AbstractHandImmersive<HeldImageImmersiveInfo<?>> {

    private int lastSlot = -1;

    @Override
    protected void render(HeldImageImmersiveInfo<?> info, PoseStack stack, VRBodyPartData hand) {
        ImmersiveRenderHelpers.instance().renderImage(stack, info.heldImage, 0, 0, 1, 1,
                hand.getPos(), info.size, info.light, null);
    }

    @Override
    protected void tick(HeldImageImmersiveInfo<?> info, VRBodyPartData hand, VRBodyPartData other) {
        info.light = ImmersiveClientLogicHelpers.instance().getLight(BlockPos.containing(hand.getPos()));
        doTickerTick(info, hand);
    }

    @Override
    public boolean activeForHand(InteractionHand hand) {
        // Keep instances matching hand active, but don't spin up any new ones.
        return this.infos.stream().anyMatch(info -> info.hand == hand);
    }

    @Override
    protected HeldImageImmersiveInfo<?> createInfo(InteractionHand hand) {
        throw new RuntimeException("createInfo() doesn't make sense for HeldImageImmersive!");
    }

    @Override
    public boolean isEnabled() {
        // Always enabled, used with other Immersives to do things.
        return true;
    }

    @Override
    public boolean onLeftClick(HeldImageImmersiveInfo<?> info, VRBodyPartData hand, VRBodyPartData other) {
        // No left-click interactions
        return false;
    }

    @Override
    public void registerAndTickAll() {
        // Clear held images on hotbar change
        int currentSlot = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (lastSlot != currentSlot) {
            lastSlot = currentSlot;
            this.infos.clear();
        }
        
        super.registerAndTickAll();
    }

    public <T> void setHeldImage(InteractionHand hand, ResourceLocation heldImage, ResourceLocation immersiveId,
                                 T heldData, float size, BiConsumer<HeldImageImmersiveInfo<T>, VRBodyPartData> ticker) {
        this.infos.removeIf(info -> info.hand == hand);
        this.infos.add(new HeldImageImmersiveInfo<>(hand, heldImage, immersiveId, heldData, size, ticker));
    }

    public List<HeldImageImmersiveInfo<?>> getHeldImages(ResourceLocation immersiveId) {
        return this.infos.stream().filter(info -> info.immersiveId.equals(immersiveId)).toList();
    }

    public void removeImages(ResourceLocation immersiveId) {
        this.infos.removeIf(info -> info.immersiveId.equals(immersiveId));
    }

    private <T> void doTickerTick(HeldImageImmersiveInfo<T> info, VRBodyPartData handData) {
        info.ticker.accept(info, handData);
    }
}
