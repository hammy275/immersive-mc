package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.server.ChestToOpenCount;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChestOpenPacket {

    public BlockPos pos;
    public boolean isOpen;

    public ChestOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
    }

    public static void encode(ChestOpenPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen);
    }

    public static ChestOpenPacket decode(PacketBuffer buffer) {
        return new ChestOpenPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(final ChestOpenPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (NetworkUtil.safeToRun(message.pos, player)) {
                    TileEntity tileEnt = player.level.getBlockEntity(message.pos);
                    if (tileEnt instanceof ChestTileEntity) {
                        if (!ActiveConfig.useChestImmersion) return;
                        ChestTileEntity chest = (ChestTileEntity) tileEnt;
                        ChestTileEntity other = Util.getOtherChest(chest);
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
                    } else if (tileEnt instanceof EnderChestTileEntity) {
                        if (!ActiveConfig.useChestImmersion) return;
                        EnderChestTileEntity chest = (EnderChestTileEntity) tileEnt;
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
