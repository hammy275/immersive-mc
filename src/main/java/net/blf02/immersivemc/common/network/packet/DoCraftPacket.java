package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.storage.NullContainer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
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
            if (player != null) {
                Optional<ICraftingRecipe> res = player.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING,
                        message.inv, player.level);
                if (res.isPresent()) {
                    // TODO: Remove all items needed for the recipe.
                    // TODO: If at any point there's a failure, give the items back, and fail the craft
                    // Give our item to us
                    ItemStack stackOut = res.get().assemble(message.inv);
                    BlockPos pos = message.tablePos.above();
                    ItemEntity entOut = new ItemEntity(player.level, pos.getX(), pos.getY(), pos.getZ());
                    entOut.setItem(stackOut);
                    entOut.setDeltaMovement(0, 0, 0);
                    player.level.addFreshEntity(entOut);

                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
