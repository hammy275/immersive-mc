package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.server.WorldStorages;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class PageTurnPacket {

    public final BlockPos pos;

    public PageTurnPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(PageTurnPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static PageTurnPacket decode(FriendlyByteBuf buffer) {
        return new PageTurnPacket(buffer.readBlockPos());
    }

    public static void handle(PageTurnPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(message.pos, player)) {
                BookData storage = (BookData) WorldStorages.instance().getOrCreate(message.pos, player.serverLevel());
                if (storage != null && !storage.book.isEmpty() && storage.pageTurner == null) {
                    storage.pageTurner = player;
                    storage.pageTurnerVR = VRPluginVerify.playerInVR(player);
                }
            }
        });
    }
}
