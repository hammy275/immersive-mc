package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.common.immersive.ImmersiveCheckers;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

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

    public static void handle(final GetRecipePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) {
                handleClient(packet);
            } else if (NetworkUtil.safeToRun(packet.pos, sender)) {
                if (sender.level.getBlockEntity(packet.pos) instanceof Container table
                && ImmersiveCheckers.isCraftingTable(packet.pos, sender.level.getBlockState(packet.pos),
                        sender.level.getBlockEntity(packet.pos), sender.level)) {
                    ItemStack[] items = new ItemStack[10];
                    for (int i = 0; i <= 8; i++) {
                        items[i] = table.getItem(i);
                    }
                    items[9] = ItemStack.EMPTY;
                    CraftingRecipe recipe = Swap.getRecipe(sender, items);
                    ItemStack output = recipe != null ? recipe.getResultItem() : ItemStack.EMPTY;
                    GetStorage.getCraftingStorage(sender, packet.pos).items[9] = output;
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
                            new GetRecipePacket(packet.pos, output));
                }

            }
        });
        ctx.get().setPacketHandled(true);

    }

    public static void handleClient(GetRecipePacket packet) {
        for (CraftingInfo info : Immersives.immersiveCrafting.getTrackedObjects()) {
            if (info.getBlockPosition().equals(packet.pos)) {
                info.outputItem = packet.stack;
            }
        }
    }

}
