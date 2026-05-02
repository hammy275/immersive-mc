package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.level.block.entity.*;

public class ChestShulkerOpenPacket {

    public BlockPos pos;
    public boolean isOpen;
    public float startingOpenness;

    public ChestShulkerOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this(pos, isOpenPacket, -1f);
    }

    public ChestShulkerOpenPacket(BlockPos pos, boolean isOpenPacket, float startingOpenness) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
        this.startingOpenness = startingOpenness;
    }

    public static void encode(ChestShulkerOpenPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen).writeFloat(packet.startingOpenness);
    }

    public static ChestShulkerOpenPacket decode(RegistryFriendlyByteBuf buffer) {
        return new ChestShulkerOpenPacket(buffer.readBlockPos(), buffer.readBoolean(), buffer.readFloat());
    }

    public static void handle(final ChestShulkerOpenPacket message, ServerPlayer player) {
        if (player != null) {
            if (NetworkUtil.safeToRun(message.pos, player)) {
                BlockEntity tileEnt = player.level().getBlockEntity(message.pos);
                boolean maybeMarkOpen = true;
                if (tileEnt instanceof ChestBlockEntity || tileEnt instanceof EnderChestBlockEntity) {
                    ChestOpennessStorage storage = SharedNetworkStorages.instance().getOrCreate(player.level(),
                            message.pos, ChestOpennessStorage.class, () -> new ChestOpennessStorage(tileEnt));
                    if (storage.takeControl(player.getUUID(), ChestOpennessStorage.AnimationState.ANIMATED)) {
                        if (message.startingOpenness >= 0f) {
                            storage.setOpenness(Mth.clamp(message.startingOpenness, 0f, 1f));
                        }
                        storage.startAnimating(player, message.isOpen ? ChestOpennessStorage.LidTarget.OPEN : ChestOpennessStorage.LidTarget.CLOSED);
                    }
                } else if (tileEnt instanceof ShulkerBoxBlockEntity shulkerBox) {
                    if (!ActiveConfig.FILE_SERVER.useShulkerImmersive) return;
                    if (message.isOpen) {
                        shulkerBox.startOpen(player);
                    } else {
                        shulkerBox.stopOpen(player);
                    }
                } else if (tileEnt instanceof BarrelBlockEntity barrel) {
                    if (!ActiveConfig.FILE_SERVER.useBarrelImmersive) return;
                    if (message.isOpen) {
                        barrel.startOpen(player);
                        ChestToOpenSet.openChest(player, barrel.getBlockPos());
                        PiglinAi.angerNearbyPiglins(player, true);
                    } else {
                        barrel.stopOpen(player);
                        ChestToOpenSet.closeChest(player, barrel.getBlockPos());
                    }
                } else if (Lootr.lootrImpl.openLootrBarrel(message.pos, player, message.isOpen) || Lootr.lootrImpl.openLootrShulkerBox(message.pos, player, message.isOpen)) {
                    // Intentional NO-OP. All the useful work is done in the if statement itself.
                } else {
                    maybeMarkOpen = false;
                }
                if (maybeMarkOpen) {
                    Lootr.lootrImpl.markOpener(player, message.pos);
                }
            }
        }
    }
}
