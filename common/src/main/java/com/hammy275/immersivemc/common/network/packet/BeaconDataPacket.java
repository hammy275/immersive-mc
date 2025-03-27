package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconDataPacket {

    private static final int speedId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.SPEED.value());
    private static final int hasteId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.HASTE.value());
    private static final int resistId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.RESISTANCE.value());
    private static final int jumpId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.JUMP_BOOST.value());
    private static final int strengthId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.STRENGTH.value());
    private static final int regenId = BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION.value());

    public final BlockPos pos;
    public final int powerIndex;
    public final boolean useRegen;

    public static BeaconDataPacket fromBeacon(BeaconBlockEntity beacon) {
        BeaconBlockEntityMixin accessor = (BeaconBlockEntityMixin) beacon;
        Holder<MobEffect> primaryEffect = accessor.immersiveMC$getPrimaryPower();
        int primaryId = primaryEffect == null ? -1 : BuiltInRegistries.MOB_EFFECT.getId(primaryEffect.value());

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

        Holder<MobEffect> secondaryEffect = accessor.immersiveMC$getSecondaryPower();
        return new BeaconDataPacket(beacon.getBlockPos(),
                powerIndex, secondaryEffect != null && BuiltInRegistries.MOB_EFFECT.getId(secondaryEffect.value()) == regenId);
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

    public static void encode(BeaconDataPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.powerIndex).writeBoolean(packet.useRegen);
    }

    public static BeaconDataPacket decode(RegistryFriendlyByteBuf buffer) {
        return new BeaconDataPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(final BeaconDataPacket packet, ServerPlayer player) {
        if (player == null) { // S2C (got info from server)
            NetworkClientHandlers.setBeaconData(packet);
        } else { // C2S (asking for info)
            if (player.level().getBlockEntity(packet.pos) instanceof BeaconBlockEntity beacon) {
                Network.INSTANCE.sendToPlayer(player,
                        BeaconDataPacket.fromBeacon(beacon));
            }
        }
    }
}
