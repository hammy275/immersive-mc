package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.storage.NullContainer;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.swap.Swap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DoCraftPacket {

    protected final CraftingInventory inv;
    protected final BlockPos tablePos;

    public DoCraftPacket(CraftingInventory inv, BlockPos tablePos) {
        this.inv = inv;
        this.tablePos = tablePos;
    }

    public static void encode(DoCraftPacket packet, PacketBuffer buffer) {
        for (int i = 0; i < 9; i++) {
            buffer.writeItem(packet.inv.getItem(i));
        }
        buffer.writeBlockPos(packet.tablePos);
    }

    public static DoCraftPacket decode(PacketBuffer buffer) {
        CraftingInventory inv = new CraftingInventory(new NullContainer(), 3, 3);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, buffer.readItem());
        }
        return new DoCraftPacket(inv, buffer.readBlockPos());

    }

    public static void handle(final DoCraftPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null && NetworkUtil.safeToRun(message.tablePos, player)) {
                Swap.handleCrafting(player, message.inv, message.tablePos);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
