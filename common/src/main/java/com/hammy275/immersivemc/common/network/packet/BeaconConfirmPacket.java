package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconConfirmPacket {

    public final BlockPos pos;
    public final int primaryId;
    public final int secondaryId;

    public BeaconConfirmPacket(BlockPos beaconPos, int primaryId, int secondaryId) {
        this.pos = beaconPos;
        this.primaryId = primaryId;
        this.secondaryId = secondaryId;
    }

    public static void encode(BeaconConfirmPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.primaryId).writeInt(packet.secondaryId);
    }

    public static BeaconConfirmPacket decode(RegistryFriendlyByteBuf buffer) {
        return new BeaconConfirmPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(final BeaconConfirmPacket message, ServerPlayer player) {
        if (NetworkUtil.safeToRun(message.pos, player)) {
            if (player.level().getBlockEntity(message.pos) instanceof BeaconBlockEntity beacon) {
                ContainerData data = ((BeaconBlockEntityMixin) beacon).immersiveMC$getBeaconData();
                BeaconStorage beaconStorage = (BeaconStorage) WorldStoragesImpl.getOrCreateS(message.pos, player.level());
                int secondId = message.secondaryId;
                if (data.get(0) == 4 && message.secondaryId == -1) {
                    secondId = message.primaryId;
                }
                if (!beaconStorage.getItem(0).isEmpty() &&
                        isValidForBeacon(data.get(0), message.primaryId, secondId)) {
                    beaconStorage.setItem(0, ItemStack.EMPTY);
                    data.set(1, message.primaryId + 1);
                    data.set(2, secondId + 1);
                    beaconStorage.setDirty(player.level());
                    player.level().blockEntityChanged(beacon.getBlockPos());
                }
            }
        }
    }
    private static boolean isValidForBeacon(int beaconLevel, int primaryId, int secondaryId) {
        if (beaconLevel < 1 || (beaconLevel < 4 && secondaryId != -1)) {
            return false;
        }

        MobEffect primary = primaryId == -1 ? null : BuiltInRegistries.MOB_EFFECT.byId(primaryId);
        MobEffect secondary = secondaryId == -1 ? null : BuiltInRegistries.MOB_EFFECT.byId(secondaryId);

        if (beaconLevel == 1) {
            return primary == MobEffects.SPEED.value() || primary == MobEffects.HASTE.value();
        } else if (beaconLevel == 2) {
            return primary == MobEffects.RESISTANCE.value() || primary == MobEffects.JUMP_BOOST.value()
                    || isValidForBeacon(1, primaryId, secondaryId);
        } else if (beaconLevel == 3) {
            return primary == MobEffects.STRENGTH.value()
                    || isValidForBeacon(2, primaryId, secondaryId);
        } else {
            return (secondary == MobEffects.REGENERATION.value() || isValidForBeacon(3, secondaryId, -1)) &&
                    isValidForBeacon(3, primaryId, -1);
        }
    }
}
