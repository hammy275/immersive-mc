package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;

public class ReelFishPacket {
    public ReelFishPacket() {

    }

    public static void encode(ReelFishPacket message, RegistryFriendlyByteBuf buffer) {

    }

    public static ReelFishPacket decode(RegistryFriendlyByteBuf buffer) {
        return new ReelFishPacket();
    }

    public static void handle(final ReelFishPacket packet, ServerPlayer player) {
        // No need to VR check here. We're just doing the same thing as a right click, so this is safe as-is.
        if (ActiveConfig.FILE_SERVER.useThrowingImmersive && player != null && player.fishing != null &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem rod) {
            rod.use(player.level(), player, InteractionHand.MAIN_HAND);
        }
    }
}
