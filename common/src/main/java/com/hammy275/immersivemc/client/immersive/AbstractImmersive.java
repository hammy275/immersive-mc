package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractImmersive<I extends AbstractImmersiveInfo, S extends NetworkStorage>
        implements Immersive<I, S> {

    public AbstractImmersive() {

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

}
