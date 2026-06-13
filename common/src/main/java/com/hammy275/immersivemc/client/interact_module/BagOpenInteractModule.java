package com.hammy275.immersivemc.client.interact_module;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.InteractModule;

public class BagOpenInteractModule implements InteractModule {

    private static final ResourceLocation id = Util.id("bag_open");

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isActive(LocalPlayer localPlayer, InteractionHand interactionHand, Vec3 vec3) {
        return Immersives.immersiveHitboxes.canOpenBagFromInteractModule(interactionHand);
    }

    @Override
    public boolean onPress(LocalPlayer localPlayer, InteractionHand interactionHand) {
        ClientUtil.openBag(localPlayer, true);
        ImmersiveClientLogicHelpers.instance().setCooldown(ClientConstants.bagOpenCloseCooldown);
        return true;
    }
}
