package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import net.minecraft.client.Minecraft;
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
        if (isOpen) {
            Lootr.lootrImpl.markOpener(Minecraft.getInstance().player, pos);
        }
    }

    public void nextRow() {
        if (++currentRow > MAX_ROW) {
            currentRow = MIN_ROW;
        }
    }

    private int getNextRow(int current) {
        int newRow = current + 1;
        if (newRow > MAX_ROW) {
            newRow = 0;
        }
        return newRow;
    }

    public int offsetIn(int rowCheck) {
        // Gets the number of rows needed until rowCheck is the active row.
        int offset = 0;
        int current = currentRow;
        while (current != rowCheck) {
            current = getNextRow(current);
            offset++;
        }
        return offset;
    }
}
