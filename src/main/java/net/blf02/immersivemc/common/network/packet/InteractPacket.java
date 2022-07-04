package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Used when interacting with something that uses WorldStorage
 */
public class InteractPacket {

    public final BlockPos pos;
    public final String storageType;
    public final int slot;
    public final Hand hand;

    public InteractPacket(BlockPos pos, int slot, Hand hand) {
        this.pos = pos;
        this.slot = slot;
        this.hand = hand;

        this.storageType = null;
    }

    public InteractPacket(String type, int slot, Hand hand) {
        this.storageType = type;
        this.slot = slot;
        this.hand = hand;

        this.pos = null;
    }

    public boolean isPlayerStorageInteract() {
        return this.storageType != null;
    }

    public static void encode(InteractPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.isPlayerStorageInteract());
        if (packet.isPlayerStorageInteract()) {
            buffer.writeUtf(packet.storageType);
        } else {
            buffer.writeBlockPos(packet.pos);
        }
        buffer.writeInt(packet.slot).writeInt(packet.hand == Hand.MAIN_HAND ? 0 : 1);
    }

    public static InteractPacket decode(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return new InteractPacket(buffer.readUtf(), buffer.readInt(),
                    buffer.readInt() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
        } else {
            return new InteractPacket(buffer.readBlockPos(), buffer.readInt(),
                    buffer.readInt() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
        }
    }

    public static void handle(final InteractPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (message.isPlayerStorageInteract()) {
                if (message.storageType.equals("backpack")) {
                    ImmersiveStorage storage = GetStorage.getPlayerStorage(player, "backpack");
                    Swap.handleBackpackCraftingSwap(message.slot, message.hand, storage, player);
                }
            } else if (NetworkUtil.safeToRun(message.pos, player)) {
                BlockState state = player.level.getBlockState(message.pos);
                if (state.getBlock() == Blocks.CRAFTING_TABLE) {
                    Swap.handleCraftingSwap(player, message.slot, message.hand, message.pos);
                } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
                    Swap.anvilSwap(message.slot, message.hand, message.pos, player);
                } else if (state.getBlock() instanceof EnchantingTableBlock) {
                    Swap.enchantingTableSwap(player, message.slot, message.hand, message.pos);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
