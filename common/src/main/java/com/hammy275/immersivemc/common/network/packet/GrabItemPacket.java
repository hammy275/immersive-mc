package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ServerPlayerConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.PlayerConfigs;
import com.hammy275.immersivemc.server.tracker.RangedGrabTrackerServer;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import dev.architectury.networking.NetworkManager;

import java.util.function.Supplier;

public class GrabItemPacket {

    public final int entityId;

    public GrabItemPacket(ItemEntity item) {
        this.entityId = item.getId();
    }

    public GrabItemPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(GrabItemPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
    }

    public static GrabItemPacket decode(FriendlyByteBuf buffer) {
        return new GrabItemPacket(buffer.readInt());
    }

    public static void handle(final GrabItemPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            if (!ActiveConfig.useRangedGrab) return;
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) {
                Entity ent = player.level.getEntity(packet.entityId);
                ServerPlayerConfig config = PlayerConfigs.getConfig(player);
                int range;
                if (config.rangedGrabRange == -1) {
                    range = 5;
                } else {
                    range = (config.rangedGrabRange + 1) * 2;
                }
                if (ent instanceof ItemEntity && player.distanceToSqr(ent) <= range * range &&
                        Util.canPickUpItem((ItemEntity) ent, player)) {
                    ItemEntity item = (ItemEntity) ent;
                    ServerTrackerInit.rangedGrabTracker.infos.add(new RangedGrabTrackerServer.RangedGrabInfo(item, player));
                }
            }
        });
        
    }


}
