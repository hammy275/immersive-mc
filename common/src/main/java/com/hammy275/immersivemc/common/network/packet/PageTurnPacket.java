package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.server.SharedNetworkStorages;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PageTurnPacket {

    public final BlockPos pos; // Position of lectern
    public final int forcedPageIndex; // Used for clickable text in books to force a page set
    public final boolean clickedRight; // Used for non-VR players to determine which direction to do a page turn

    public PageTurnPacket(BlockPos pos) {
        this(pos, -1);
    }

    public PageTurnPacket(BlockPos pos, int forcedPageIndex) {
        this(pos, forcedPageIndex, false);
    }

    public PageTurnPacket(BlockPos pos, boolean clickedRight) {
        this(pos, -1, clickedRight);
    }

    public PageTurnPacket(BlockPos pos, int forcedPageIndex, boolean clickedRight) {
        this.pos = pos;
        this.forcedPageIndex = forcedPageIndex;
        this.clickedRight = clickedRight;
    }

    public static void encode(PageTurnPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.forcedPageIndex).writeBoolean(packet.clickedRight);
    }

    public static PageTurnPacket decode(FriendlyByteBuf buffer) {
        return new PageTurnPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(PageTurnPacket message, ServerPlayer player) {
        if (NetworkUtil.safeToRun(message.pos, player)) {
            BookData storage = SharedNetworkStorages.instance().getOrCreate(player.level(), message.pos, ImmersiveHandlers.lecternHandler);
            if (storage != null && !storage.book.isEmpty() && storage.pageTurner == null) {
                if (message.forcedPageIndex == -1) {
                    if (!VRPluginVerify.playerInVR(player) &&
                            (message.clickedRight ? !storage.onLastPage() : !storage.onFirstPage())) {
                        storage.startPageTurnAnim(player, message.clickedRight);
                    } else if (VRPluginVerify.playerInVR(player)) {
                        // Let the VR player have control of page turning
                        storage.pageTurner = player;
                    }
                } else {
                    storage.setPage(message.forcedPageIndex);
                }
            }
        }
    }
}
