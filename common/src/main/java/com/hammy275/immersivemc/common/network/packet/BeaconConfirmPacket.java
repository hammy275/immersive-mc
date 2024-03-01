package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import com.hammy275.immersivemc.server.storage.GetStorage;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.function.Supplier;

public class BeaconConfirmPacket {

    public final BlockPos pos;
    public final int primaryId;
    public final int secondaryId;

    public BeaconConfirmPacket(BlockPos beaconPos, int primaryId, int secondaryId) {
        this.pos = beaconPos;
        this.primaryId = primaryId;
        this.secondaryId = secondaryId;
    }

    public static void encode(BeaconConfirmPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.primaryId).writeInt(packet.secondaryId);
    }

    public static BeaconConfirmPacket decode(FriendlyByteBuf buffer) {
        return new BeaconConfirmPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(final BeaconConfirmPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(message.pos, player)) {
                if (player.level().getBlockEntity(message.pos) instanceof BeaconBlockEntity beacon) {
                    ContainerData data = ((BeaconBlockEntityMixin) beacon).getBeaconData();
                    ImmersiveStorage beaconStorage = GetStorage.getBeaconStorage(player, message.pos);
                    int secondId = message.secondaryId;
                    if (data.get(0) == 4 && message.secondaryId == -1) {
                        secondId = message.primaryId;
                    }
                    if (!beaconStorage.getItem(0).isEmpty() &&
                            isValidForBeacon(data.get(0), message.primaryId, secondId)) {
                        beaconStorage.setItem(0, ItemStack.EMPTY);
                        data.set(1, message.primaryId + 1);
                        data.set(2, secondId + 1);
                        beaconStorage.setDirty();
                        player.level().blockEntityChanged(beacon.getBlockPos());
                    }
                }
            }
        });
    }
    private static boolean isValidForBeacon(int beaconLevel, int primaryId, int secondaryId) {
        if (beaconLevel < 1 || (beaconLevel < 4 && secondaryId != -1)) {
            return false;
        }

        MobEffect primary = primaryId == -1 ? null : BuiltInRegistries.MOB_EFFECT.byId(primaryId);
        MobEffect secondary = secondaryId == -1 ? null : BuiltInRegistries.MOB_EFFECT.byId(secondaryId);

        if (beaconLevel == 1) {
            return primary == MobEffects.MOVEMENT_SPEED || primary == MobEffects.DIG_SPEED;
        } else if (beaconLevel == 2) {
            return primary == MobEffects.DAMAGE_RESISTANCE || primary == MobEffects.JUMP
                    || isValidForBeacon(1, primaryId, secondaryId);
        } else if (beaconLevel == 3) {
            return primary == MobEffects.DAMAGE_BOOST
                    || isValidForBeacon(2, primaryId, secondaryId);
        } else {
            return (secondary == MobEffects.REGENERATION || isValidForBeacon(3, secondaryId, -1)) &&
                    isValidForBeacon(3, primaryId, -1);
        }
    }
}
