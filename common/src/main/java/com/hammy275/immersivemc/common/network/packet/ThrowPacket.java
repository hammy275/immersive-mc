package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.data.AboutToThrowData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

public class ThrowPacket {

    private final Vec3 velocity;
    private Vec3 dir;

    public ThrowPacket(Vec3 velocity, Vec3 dir) {
        this.velocity = velocity;
        this.dir = dir;
    }

    public static void encode(ThrowPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(packet.velocity.x).writeDouble(packet.velocity.y).writeDouble(packet.velocity.z)
                .writeDouble(packet.dir.x).writeDouble(packet.dir.y).writeDouble(packet.dir.z);
    }

    public static ThrowPacket decode(RegistryFriendlyByteBuf buffer) {
        return new ThrowPacket(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(final ThrowPacket packet, ServerPlayer player) {
        packet.dir = packet.dir.normalize(); // Just in case we get something non-normalized from the client
        if (player != null && ActiveConfig.FILE_SERVER.useThrowingImmersive) {
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (Util.isThrowableItem(itemInHand.getItem())) {
                if (itemInHand.getItem() instanceof FishingRodItem && player.fishing != null) return;
                AboutToThrowData.aboutToThrowMap.put(player.getUUID(),
                        new AboutToThrowData.ThrowRecord(packet.velocity, packet.dir));
                if (itemInHand.getItem() instanceof TridentItem) {
                    // 71000 just ensures we "held right click" long enough
                    itemInHand.getItem().releaseUsing(itemInHand, player.level(), player, 71000);
                } else {
                    itemInHand.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                }
            }
        }
    }
}
