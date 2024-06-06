package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfoV2;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractImmersiveV2<I extends AbstractImmersiveInfoV2, S extends NetworkStorage>
        implements Immersive<I, S> {

    private final List<I> infos = new ArrayList<>();

    @Override
    public Collection<I> getTrackedObjects() {
        return infos;
    }

    @Override
    public void tick(I info) {
        info.tick();
    }

    @Override
    public @Nullable I findImmersive(BlockPos pos) {
        for (I info : infos) {
            if (info.getBlockPosition().equals(pos)) {
                return info;
            }
        }
        return null;
    }
}
