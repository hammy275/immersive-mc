package net.blf02.immersivemc.client.subscribe;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

public class ClientRenderSubscriber {

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            renderInfos(singleton, event.getMatrixStack());
        }

    }

    protected <I extends AbstractImmersiveInfo> void renderInfos(AbstractImmersive<I> singleton,
                                                                 MatrixStack stack) {
        // Use a copy so we don't interfere with the logical thread
        List<I> infos = new LinkedList<>(singleton.getTrackedObjects());
        for (I info : infos) {
            singleton.doRender(info, stack, VRPluginVerify.hasVR);
        }
    }

}
