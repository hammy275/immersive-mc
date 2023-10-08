package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class GetRecipePacket {

    public final BlockPos pos;
    public final ItemStack stack;

    public GetRecipePacket(BlockPos pos) {
        this(pos, null);
    }

    public GetRecipePacket(BlockPos pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
    }

    public boolean isClientToServer() {
        return this.stack == null;
    }

    public static void encode(GetRecipePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isClientToServer());
        if (!packet.isClientToServer()) {
            buffer.writeItem(packet.stack);
        }
    }

    public static GetRecipePacket decode(FriendlyByteBuf buffer) {
        return new GetRecipePacket(buffer.readBlockPos(), buffer.readBoolean() ? null : buffer.readItem());
    }

    public static void handle(final GetRecipePacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer sender = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (sender == null) {
                handleClient(packet);
            } else if (NetworkUtil.safeToRun(packet.pos, sender)) {
                if (sender.level().getBlockEntity(packet.pos) instanceof Container table
                && ImmersiveCheckers.isCraftingTable(packet.pos, sender.level().getBlockState(packet.pos),
                        sender.level().getBlockEntity(packet.pos), sender.level())) {
                    ItemStack[] items = new ItemStack[10];
                    for (int i = 0; i <= 8; i++) {
                        items[i] = table.getItem(i);
                    }
                    items[9] = ItemStack.EMPTY;
                    ItemStack output = Swap.getRecipeOutput(sender, items);
                    GetStorage.getCraftingStorage(sender, packet.pos).setItem(9, output);
                    Network.INSTANCE.sendToPlayer(sender,
                            new GetRecipePacket(packet.pos, output));
                }

            }
        });
        

    }

    public static void handleClient(GetRecipePacket packet) {
        for (BuiltImmersiveInfo info : Immersives.immersiveCrafting.getTrackedObjects()) {
            if (info.getBlockPosition().equals(packet.pos)) {
                info.itemHitboxes.get(9).item = packet.stack;
            }
        }
    }

}
