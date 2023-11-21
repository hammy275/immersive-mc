package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import net.minecraft.core.BlockPos;

public class ChestLikeData {

    public static final int MIN_ROW = 0;
    public static final int MAX_ROW = 2;

    public int currentRow = MIN_ROW;
    public boolean isOpen = false;

    public ChestLikeData() {

    }

    public void toggleOpen(BlockPos pos) {
        isOpen = !isOpen;
        sendOpenPacket(pos);
    }

    public void forceOpen(BlockPos pos) {
        if (!isOpen) {
            isOpen = true;
            sendOpenPacket(pos);
        }
    }

    public void forceClose(BlockPos pos) {
        if (isOpen) {
            isOpen = false;
            sendOpenPacket(pos);
        }
    }

    private void sendOpenPacket(BlockPos pos) {
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(pos, isOpen));
    }

    public void nextRow() {
        if (++currentRow > MAX_ROW) {
            currentRow = MIN_ROW;
        }
    }
}
