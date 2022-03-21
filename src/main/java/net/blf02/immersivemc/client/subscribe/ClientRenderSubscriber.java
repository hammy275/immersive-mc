package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.render.ImmersiveFurnace;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber
public class ClientRenderSubscriber {

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        List<ImmersiveFurnace.ImmersiveFurnaceInfo> toRemove = new LinkedList<>();
        for (ImmersiveFurnace.ImmersiveFurnaceInfo info : ImmersiveFurnace.furnaces) {
            ImmersiveFurnace.handleFurnace(info, event.getMatrixStack());
            info.ticksLeft--;
            if (info.ticksLeft <= 0) {
                toRemove.add(info);
            }
        }

        for (ImmersiveFurnace.ImmersiveFurnaceInfo info : toRemove) {
            ImmersiveFurnace.furnaces.remove(info);
        }
    }

}
