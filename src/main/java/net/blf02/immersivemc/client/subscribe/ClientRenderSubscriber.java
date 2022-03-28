package net.blf02.immersivemc.client.subscribe;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

public class ClientRenderSubscriber {

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            handleInfos(singleton, event.getMatrixStack());
        }

    }

    protected <I extends AbstractImmersiveInfo> void handleInfos(AbstractImmersive<I> singleton,
                                                                 MatrixStack stack) {
        List<I> infos = singleton.getTrackedObjects();
        List<I> toRemove = new LinkedList<>();
        for (I info : infos) {
            singleton.doImmersion(info, stack);
            if (info.getTicksLeft() <= 0) {
                toRemove.add(info);
            }
        }

        for (I info : toRemove) {
            infos.remove(info);
        }
    }

}
