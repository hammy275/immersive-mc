package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;

import java.util.function.Supplier;

public class ReelFishPacket {
    public ReelFishPacket() {

    }

    public static void encode(ReelFishPacket message, FriendlyByteBuf buffer) {

    }

    public static ReelFishPacket decode(FriendlyByteBuf buffer) {
        return new ReelFishPacket();
    }

    public static void handle(final ReelFishPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            // No need to VR check here. We're just doing the same thing as a right click, so this is safe as-is.
            if (ActiveConfig.FILE_SERVER.useThrowingImmersive && player != null && player.fishing != null &&
                    player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem rod) {
                rod.use(player.level, player, InteractionHand.MAIN_HAND);
            }
        });
    }
}
