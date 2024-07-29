package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.common.api_impl.ImmersiveLogicHelpersImpl;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.LightLayer;

public class ImmersiveClientLogicHelpersImpl extends ImmersiveLogicHelpersImpl implements ImmersiveClientLogicHelpers {

    public static final ImmersiveClientLogicHelpers INSTANCE = new ImmersiveClientLogicHelpersImpl();

    @Override
    public void sendSwapPacket(BlockPos pos, int slot, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new SwapPacket(pos, slot, hand));
    }

    @Override
    public int getLight(BlockPos pos) {
        // TODO: Return maxLight here if full bright in ImmersiveMC settings
        return LightTexture.pack(Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, pos),
                Minecraft.getInstance().level.getBrightness(LightLayer.SKY, pos));
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

            int blockLight = Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > maxBlock) {
                maxBlock = blockLight;
            }

            int skyLight = Minecraft.getInstance().level.getBrightness(LightLayer.SKY, pos);
            if (skyLight > maxSky) {
                maxSky = skyLight;
            }

            // Have max light for both, no need to continue light calculations!
            if (maxBlock == 15 && maxSky == 15) {
                break;
            }
        }
        return LightTexture.pack(maxBlock, maxSky);
    }
}
