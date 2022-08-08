package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.server.ChestToOpenCount;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChestOpenPacket {

    public BlockPos pos;
    public boolean isOpen;

    public ChestOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
    }

    public static void encode(ChestOpenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen);
    }

    public static ChestOpenPacket decode(FriendlyByteBuf buffer) {
        return new ChestOpenPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(final ChestOpenPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (NetworkUtil.safeToRun(message.pos, player)) {
                    BlockEntity tileEnt = player.level.getBlockEntity(message.pos);
                    if (tileEnt instanceof ChestBlockEntity) {
                        if (!ActiveConfig.useChestImmersion) return;
                        ChestBlockEntity chest = (ChestBlockEntity) tileEnt;
                        ChestBlockEntity other = Util.getOtherChest(chest);
                        if (message.isOpen) {
                            chest.startOpen(player);
                            changeChestCount(chest.getBlockPos(), 1);
                            if (other != null) {
                                other.startOpen(player);
                                changeChestCount(other.getBlockPos(), 1);
                            }
                            PiglinTasks.angerNearbyPiglins(player, true);
                        } else {
                            chest.stopOpen(player);
                            changeChestCount(chest.getBlockPos(), -1);
                            if (other != null) {
                                other.stopOpen(player);
                                changeChestCount(other.getBlockPos(), -1);
                            }
                        }
                    } else if (tileEnt instanceof EnderChestBlockEntity) {
                        if (!ActiveConfig.useChestImmersion) return;
                        EnderChestBlockEntity chest = (EnderChestBlockEntity) tileEnt;
                        if (message.isOpen) {
                            chest.startOpen();
                            changeChestCount(chest.getBlockPos(), 1);
                            PiglinTasks.angerNearbyPiglins(player, true);
                        } else {
                            chest.stopOpen();
                            changeChestCount(chest.getBlockPos(), -1);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
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
