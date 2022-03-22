package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.immersive.ImmersiveFurnace;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber
public class ClientRenderSubscriber {

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        List<ImmersiveFurnaceInfo> toRemove = new LinkedList<>();
        for (ImmersiveFurnaceInfo info : ImmersiveFurnace.getSingleton().getTrackedObjects()) {
            ImmersiveFurnace.getSingleton().handleImmersion(info, event.getMatrixStack());
            info.changeTicksLeft(-1);
            if (info.getTicksLeft() <= 0) {
                toRemove.add(info);
            }
        }

        for (ImmersiveFurnaceInfo info : toRemove) {
            ImmersiveFurnace.getSingleton().getTrackedObjects().remove(info);
        }
    }

}
