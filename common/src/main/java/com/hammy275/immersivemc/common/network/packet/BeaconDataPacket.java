package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.function.Supplier;

public class BeaconDataPacket {

    private static final int speedId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.MOVEMENT_SPEED);
    private static final int hasteId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DIG_SPEED);
    private static final int resistId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DAMAGE_RESISTANCE);
    private static final int jumpId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.JUMP);
    private static final int strengthId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DAMAGE_BOOST);
    private static final int regenId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION);

    public final BlockPos pos;
    public final int powerIndex;
    public final boolean useRegen;

    public static BeaconDataPacket fromBeacon(BeaconBlockEntity beacon) {
        BeaconBlockEntityMixin accessor = (BeaconBlockEntityMixin) beacon;
        int primaryId = BuiltInRegistries.MOB_EFFECT.getId(accessor.getPrimaryPower());

        int powerIndex = -1;
        if (primaryId == speedId) {
            powerIndex = 0;
        } else if (primaryId == hasteId) {
            powerIndex = 1;
        } else if (primaryId == resistId) {
            powerIndex = 2;
        } else if (primaryId == jumpId) {
            powerIndex = 3;
        } else if (primaryId == strengthId) {
            powerIndex = 4;
        }

        return new BeaconDataPacket(beacon.getBlockPos(),
                powerIndex, BuiltInRegistries.MOB_EFFECT.getId(accessor.getSecondaryPower()) == regenId);
    }

    public BeaconDataPacket(BlockPos pos, int powerIndex, boolean useRegen) {
        this.pos = pos;
        this.powerIndex = powerIndex;
        this.useRegen = useRegen;
    }

    public BeaconDataPacket(BlockPos pos) {
        // Write some junk to be decoded for decode() to be happy
        this.pos = pos;
        this.powerIndex = -2;
        this.useRegen = false;
    }

    public static void encode(BeaconDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.powerIndex).writeBoolean(packet.useRegen);
    }

    public static BeaconDataPacket decode(FriendlyByteBuf buffer) {
        return new BeaconDataPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(final BeaconDataPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // S2C (got info from server)
                NetworkClientHandlers.setBeaconData(packet);
            } else { // C2S (asking for info)
                if (player.getLevel().getBlockEntity(packet.pos) instanceof BeaconBlockEntity beacon) {
                    Network.INSTANCE.sendToPlayer(player,
                            BeaconDataPacket.fromBeacon(beacon));
                }
            }
        });
    }
}
