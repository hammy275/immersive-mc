package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersive;
import com.hammy275.immersivemc.client.immersive.info.AbstractBlockBasedImmersiveInfo;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.immersive.info.render_state.AbstractImmersiveRenderState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractBlockBasedImmersive<I extends AbstractBlockBasedImmersiveInfo, R extends AbstractImmersiveRenderState, S extends NetworkStorage>
        implements BlockBasedImmersive<I, R, S> {

    public AbstractBlockBasedImmersive() {

    }

    protected final List<I> infos = new ArrayList<>();

    @Override
    public Collection<I> getTrackedObjects() {
        return infos;
    }

    @Override
    public void tick(I info) {
        info.tick();
    }

    @Override
    public void extractRenderState(I info, R renderState, float partialTicks) {
        renderState.hitboxes = info.hitboxes.stream().map(hitboxItemPair -> hitboxItemPair.getRenderHitbox(partialTicks)).toList();
        renderState.items = info.hitboxes.stream().map(hitboxItemPair -> hitboxItemPair.item).toList();
        renderState.pos = info.getBlockPosition();
        renderState.slotsHovered = new int[]{info.getSlotHovered(0), info.getSlotHovered(1)};
        renderState.ticksExisted = info.getTicksExisted();
    }
}
