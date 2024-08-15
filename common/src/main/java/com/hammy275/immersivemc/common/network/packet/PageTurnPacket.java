package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.server.WorldStorages;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

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

    public static void handle(PageTurnPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(message.pos, player)) {
                BookData storage = (BookData) WorldStorages.instance().getOrCreate(message.pos, player.serverLevel());
                if (storage != null && !storage.book.isEmpty() && storage.pageTurner == null) {
                    if (message.forcedPageIndex == -1) {
                        if (!VRPluginVerify.playerInVR(player) &&
                                (message.clickedRight ? !storage.onLastPage() : !storage.onFirstPage())) {
                            // Turn to the next page and start a full animation if out of VR and it's valid to
                            // go to the next page
                            storage.pageTurner = player;
                            storage.pageChangeState = message.clickedRight ? PageChangeState.RIGHT_TO_LEFT_ANIM : PageChangeState.LEFT_TO_RIGHT_ANIM;
                            if (message.clickedRight) {
                                storage.nextPage();
                            } else {
                                storage.lastPage();
                            }
                            storage.setDirty();
                        } else if (VRPluginVerify.playerInVR(player)) {
                            // Let the VR player have control of page turning
                            storage.pageTurner = player;
                        }
                    } else {
                        storage.setPage(message.forcedPageIndex);
                    }
                }
            }
        });
    }
}
