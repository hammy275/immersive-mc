package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.ChestToOpenCount;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.util.function.Supplier;

public class ChestShulkerOpenPacket {

    public BlockPos pos;
    public boolean isOpen;

    public ChestShulkerOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
    }

    public static void encode(ChestShulkerOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen);
    }

    public static ChestShulkerOpenPacket decode(FriendlyByteBuf buffer) {
        return new ChestShulkerOpenPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(final ChestShulkerOpenPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) {
                if (NetworkUtil.safeToRun(message.pos, player)) {
                    BlockEntity tileEnt = player.level.getBlockEntity(message.pos);
                    if (tileEnt instanceof ChestBlockEntity) {
                        if (!ActiveConfig.FILE.useChestImmersion) return;
                        ChestBlockEntity chest = (ChestBlockEntity) tileEnt;
                        ChestBlockEntity other = Util.getOtherChest(chest);
                        if (message.isOpen) {
                            chest.startOpen(player);
                            changeChestCount(chest.getBlockPos(), 1);
                            if (other != null) {
                                other.startOpen(player);
                                changeChestCount(other.getBlockPos(), 1);
                            }
                            PiglinAi.angerNearbyPiglins(player, true);
                        } else {
                            chest.stopOpen(player);
                            changeChestCount(chest.getBlockPos(), -1);
                            if (other != null) {
                                other.stopOpen(player);
                                changeChestCount(other.getBlockPos(), -1);
                            }
                        }
                    } else if (tileEnt instanceof EnderChestBlockEntity) {
                        if (!ActiveConfig.FILE.useChestImmersion) return;
                        EnderChestBlockEntity chest = (EnderChestBlockEntity) tileEnt;
                        if (message.isOpen) {
                            chest.startOpen(player);
                            changeChestCount(chest.getBlockPos(), 1);
                            PiglinAi.angerNearbyPiglins(player, true);
                        } else {
                            chest.stopOpen(player);
                            changeChestCount(chest.getBlockPos(), -1);
                        }
                    } else if (tileEnt instanceof ShulkerBoxBlockEntity shulkerBox) {
                        if (!ActiveConfig.FILE.useShulkerImmersion) return;
                        if (message.isOpen) {
                            shulkerBox.startOpen(player);
                        } else {
                            shulkerBox.stopOpen(player);
                        }
                    } else if (tileEnt instanceof BarrelBlockEntity barrel) {
                        if (!ActiveConfig.FILE.useBarrelImmersion) return;
                        if (message.isOpen) {
                            barrel.startOpen(player);
                            changeChestCount(barrel.getBlockPos(), 1);
                            PiglinAi.angerNearbyPiglins(player, true);
                        } else {
                            barrel.stopOpen(player);
                            changeChestCount(barrel.getBlockPos(), -1);
                        }
                    }
                }
            }
        });
        
    }

    protected static void changeChestCount(BlockPos pos, int amount) {
        Integer currentVal = ChestToOpenCount.chestImmersiveOpenCount.get(pos);
        int newVal;
        if (currentVal == null || currentVal == 0) {
            newVal = amount;
        } else {
            newVal = amount + currentVal;
        }
        if (newVal <= 0) {
            ChestToOpenCount.chestImmersiveOpenCount.remove(pos);
        } else {
            ChestToOpenCount.chestImmersiveOpenCount.put(pos, newVal);
        }
    }


}
