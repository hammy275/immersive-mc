package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.immersive.ImmersiveBrewing;
import net.blf02.immersivemc.client.immersive.ImmersiveCrafting;
import net.blf02.immersivemc.client.immersive.ImmersiveFurnace;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

public class ClientRenderSubscriber {

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        List<ImmersiveFurnaceInfo> toRemoveFurnace = new LinkedList<>();
        for (ImmersiveFurnaceInfo info : ImmersiveFurnace.getSingleton().getTrackedObjects()) {
            ImmersiveFurnace.getSingleton().doImmersion(info, event.getMatrixStack());
            if (info.getTicksLeft() <= 0) {
                toRemoveFurnace.add(info);
            }
        }

        for (ImmersiveFurnaceInfo info : toRemoveFurnace) {
            ImmersiveFurnace.getSingleton().getTrackedObjects().remove(info);
        }

        List<BrewingInfo> toRemoveStand = new LinkedList<>();
        for (BrewingInfo info : ImmersiveBrewing.getSingleton().getTrackedObjects()) {
            ImmersiveBrewing.getSingleton().doImmersion(info, event.getMatrixStack());
            if (info.getTicksLeft() <= 0) {
                toRemoveStand.add(info);
            }
        }

        for (BrewingInfo info : toRemoveStand) {
            ImmersiveBrewing.getSingleton().getTrackedObjects().remove(info);
        }

        List<CraftingInfo> toRemoveTable = new LinkedList<>();
        for (CraftingInfo info : ImmersiveCrafting.singleton.getTrackedObjects()) {
            ImmersiveCrafting.singleton.doImmersion(info, event.getMatrixStack());
            if (info.getTicksLeft() <= 0) {
                toRemoveTable.add(info);
            }
        }

        for (CraftingInfo info : toRemoveTable) {
            ImmersiveCrafting.singleton.getTrackedObjects().remove(info);
        }

    }

}
