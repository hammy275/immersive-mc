package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.server.tracker.RangedGrabTrackerServer;
import net.blf02.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class GrabItemPacket {

    public final int entityId;

    public GrabItemPacket(ItemEntity item) {
        this.entityId = item.getId();
    }

    public GrabItemPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(GrabItemPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.entityId);
    }

    public static GrabItemPacket decode(PacketBuffer buffer) {
        return new GrabItemPacket(buffer.readInt());
    }

    public static void handle(final GrabItemPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useRangedGrab) return;
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                Entity ent = player.level.getEntity(packet.entityId);
                if (ent instanceof ItemEntity && player.distanceToSqr(ent) <= 144 &&
                        Util.canPickUpItem((ItemEntity) ent, player)) {
                    ItemEntity item = (ItemEntity) ent;
                    ServerTrackerInit.rangedGrabTracker.infos.add(new RangedGrabTrackerServer.RangedGrabInfo(item, player));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
