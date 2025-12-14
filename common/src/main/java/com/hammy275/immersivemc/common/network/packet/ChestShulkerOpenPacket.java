package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ChestShulkerOpenPacket {

    public BlockPos pos;
    public boolean isOpen;

    public ChestShulkerOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
    }

    public static void encode(ChestShulkerOpenPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen);
    }

    public static ChestShulkerOpenPacket decode(RegistryFriendlyByteBuf buffer) {
        return new ChestShulkerOpenPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(final ChestShulkerOpenPacket message, ServerPlayer player) {
        if (player != null) {
            if (NetworkUtil.safeToRun(message.pos, player)) {
                BlockEntity tileEnt = player.level().getBlockEntity(message.pos);
                boolean maybeMarkOpen = true;
                if (tileEnt instanceof ChestBlockEntity) {
                    if (!ActiveConfig.FILE_SERVER.useChestImmersive) return;
                    ChestBlockEntity chest = (ChestBlockEntity) tileEnt;
                    ChestBlockEntity other = Util.getOtherChest(chest);
                    if (message.isOpen) {
                        chest.startOpen(player);
                        ChestToOpenSet.openChest(player, chest.getBlockPos());
                        if (other != null) {
                            other.startOpen(player);
                            ChestToOpenSet.openChest(player, other.getBlockPos());
                        }
                        PiglinAi.angerNearbyPiglins(player.level(), player, true);
                    } else {
                        chest.stopOpen(player);
                        ChestToOpenSet.closeChest(player, chest.getBlockPos());
                        if (other != null) {
                            other.stopOpen(player);
                            ChestToOpenSet.closeChest(player, other.getBlockPos());
                        }
                    }
                } else if (tileEnt instanceof EnderChestBlockEntity) {
                    if (!ActiveConfig.FILE_SERVER.useChestImmersive) return;
                    EnderChestBlockEntity chest = (EnderChestBlockEntity) tileEnt;
                    if (message.isOpen) {
                        chest.startOpen(player);
                        ChestToOpenSet.openChest(player, chest.getBlockPos());
                        PiglinAi.angerNearbyPiglins(player.level(), player, true);
                    } else {
                        chest.stopOpen(player);
                        ChestToOpenSet.closeChest(player, chest.getBlockPos());
                    }
                    maybeMarkOpen = false; // Never bother to attempt to mark ender chests as opened for Lootr
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
                        PiglinAi.angerNearbyPiglins(player.level(), player, true);
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
