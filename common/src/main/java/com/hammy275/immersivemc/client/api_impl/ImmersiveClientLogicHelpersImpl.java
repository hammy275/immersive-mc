package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import com.hammy275.immersivemc.client.subscribe.ClientVRSubscriber;
import com.hammy275.immersivemc.common.api_impl.ImmersiveLogicHelpersImpl;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.AttachmentSwapPacket;
import com.hammy275.immersivemc.common.network.packet.BlockSwapPacket;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;

import java.util.List;

public class ImmersiveClientLogicHelpersImpl extends ImmersiveLogicHelpersImpl implements ImmersiveClientLogicHelpers {

    public static final ImmersiveClientLogicHelpers INSTANCE = new ImmersiveClientLogicHelpersImpl();

    @Override
    public void setCooldown(int cooldown) {
        SwapTracker.c0.setCooldown(cooldown);
        ClientVRSubscriber.setCooldown((int) (cooldown * ClientConstants.cooldownVRMultiplier));
    }

    @Override
    public void sendSwapPacket(BlockPos pos, List<Integer> slots, InteractionHand hand, boolean modifierPressed) {
        Network.INSTANCE.sendToServer(new BlockSwapPacket(pos, slots, hand, getSwapMode(slots.size(), modifierPressed)));
    }

    @Override
    public void sendSwapPacket(PlayerAttachmentImmersiveHandler<?> handler, AbstractClientPlayer owner, List<Integer> slots, InteractionHand hand, boolean modifierPressed) {
        Network.INSTANCE.sendToServer(new AttachmentSwapPacket(handler.getID(), owner.getUUID(), slots, hand, getSwapMode(slots.size(), modifierPressed)));
    }

    private SwapMode getSwapMode(int numSlots, boolean modifierPressed) {
        if (numSlots > 1) {
            return modifierPressed ? SwapMode.SPLIT : SwapMode.SINGLE;
        } else {
            if ((Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCrouching() && ActiveConfig.active().crouchMode.swapAll())
                    || (modifierPressed && !VRVerify.clientInVR())) {
                return SwapMode.ALL;
            } else {
                return SwapMode.SINGLE;
            }
        }
    }

    @Override
    public int getLight(BlockPos pos) {
        // TODO: Return maxLight here if full bright in ImmersiveMC settings
        return LevelRenderer.getLightCoords(Minecraft.getInstance().level, pos);
    }

    @Override
    public int getLight(Iterable<BlockPos> positions) {
        // TODO: Return maxLight here if full bright in ImmersiveMC settings
        int maxBlock = 0;
        int maxSky = 0;
        for (BlockPos pos : positions) {
            if (pos == null) {
                continue;
            }

            int packedLight = getLight(pos);
            int blockLight = LightCoordsUtil.block(packedLight);
            int skyLight = LightCoordsUtil.sky(packedLight);

            if (blockLight > maxBlock) {
                maxBlock = blockLight;
            }
            if (skyLight > maxSky) {
                maxSky = skyLight;
            }

            // Have max light for both, no need to continue light calculations!
            if (maxBlock == 15 && maxSky == 15) {
                break;
            }
        }
        return LightCoordsUtil.pack(maxBlock, maxSky);
    }
}
