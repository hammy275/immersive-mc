package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.storage.ImmersiveMCLevelStorage;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ItemBackPacket {

    private final BlockPos pos;

    public ItemBackPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ItemBackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static ItemBackPacket decode(FriendlyByteBuf buffer) {
        return new ItemBackPacket(buffer.readBlockPos());
    }

    public static void handle(ItemBackPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(packet.pos, player) && ImmersiveMCLevelStorage.usesWorldStorage(packet.pos, player.level)) {
                ImmersiveStorage storage = GetStorage.getStorage(player, packet.pos);
                if (storage != null) {
                    storage.returnItems(player);
                    GetStorage.updateStorageOutputAfterItemReturn(player, packet.pos);
                }
            }
        });
    }
}
