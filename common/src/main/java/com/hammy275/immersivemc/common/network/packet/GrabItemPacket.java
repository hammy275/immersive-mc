package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.tracker.RangedGrabTrackerServer;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class GrabItemPacket {

    public final int entityId;

    public GrabItemPacket(ItemEntity item) {
        this.entityId = item.getId();
    }

    public GrabItemPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(GrabItemPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
    }

    public static GrabItemPacket decode(RegistryFriendlyByteBuf buffer) {
        return new GrabItemPacket(buffer.readInt());
    }

    public static void handle(final GrabItemPacket packet, ServerPlayer player) {
        if (!ActiveConfig.FILE_SERVER.useRangedGrabImmersive) return;
        if (player != null) {
            Entity ent = player.level().getEntity(packet.entityId);
            ActiveConfig playerConfig = ActiveConfig.getConfigForPlayer(player);
            int range;
            if (playerConfig.rangedGrabRange == -1) {
                range = 5;
            } else {
                range = (playerConfig.rangedGrabRange + 1) * 2;
            }
            if (ent instanceof ItemEntity && player.distanceToSqr(ent) <= range * range &&
                    Util.canPickUpItem((ItemEntity) ent, player)) {
                ItemEntity item = (ItemEntity) ent;
                ServerTrackerInit.rangedGrabTracker.infos.add(new RangedGrabTrackerServer.RangedGrabInfo(item, player));
            }
        }
    }


}
