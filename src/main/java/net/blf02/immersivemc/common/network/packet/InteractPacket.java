package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.SafeClientUtil;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayer;
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
    public final HumanoidArm hand;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public InteractPacket(BlockPos pos, int slot, HumanoidArm hand) {
        this.pos = pos;
        this.slot = slot;
        this.hand = hand;

        this.storageType = null;
    }

    public InteractPacket(String type, int slot, HumanoidArm hand) {
        this.storageType = type;
        this.slot = slot;
        this.hand = hand;

        this.pos = null;

        if (type.equals("backpack")) {
            placementMode = SafeClientUtil.getPlacementMode(true);
        }
    }

    protected InteractPacket setPlacementMode(PlacementMode mode) {
        this.placementMode = mode;
        return this;
    }

    public boolean isPlayerStorageInteract() {
        return this.storageType != null;
    }

    public static void encode(InteractPacket packet, PacketBuffer buffer) {
        buffer.writeEnum(packet.placementMode);
        buffer.writeBoolean(packet.isPlayerStorageInteract());
        if (packet.isPlayerStorageInteract()) {
            buffer.writeUtf(packet.storageType);
        } else {
            buffer.writeBlockPos(packet.pos);
        }
        buffer.writeInt(packet.slot).writeInt(packet.hand == Hand.MAIN_HAND ? 0 : 1);
    }

    public static InteractPacket decode(PacketBuffer buffer) {
        PlacementMode mode = buffer.readEnum(PlacementMode.class);
        if (buffer.readBoolean()) {
            return new InteractPacket(buffer.readUtf(), buffer.readInt(),
                    buffer.readInt() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND).setPlacementMode(mode);
        } else {
            return new InteractPacket(buffer.readBlockPos(), buffer.readInt(),
                    buffer.readInt() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND).setPlacementMode(mode);
        }
    }

    public static void handle(final InteractPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (message.isPlayerStorageInteract()) {
                if (message.storageType.equals("backpack")) {
                    ImmersiveStorage storage = GetStorage.getPlayerStorage(player, "backpack");
                    // -27 below since 0-26 are inventory slots
                    Swap.handleBackpackCraftingSwap(message.slot - 27, message.hand, storage, player, message.placementMode);
                }
            } else if (NetworkUtil.safeToRun(message.pos, player)) {
                BlockState state = player.level.getBlockState(message.pos);
                if (state.getBlock() == Blocks.CRAFTING_TABLE) {
                    Swap.handleCraftingSwap(player, message.slot, message.hand, message.pos, message.placementMode);
                } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
                    Swap.anvilSwap(message.slot, message.hand, message.pos, player, message.placementMode);
                } else if (state.getBlock() instanceof EnchantmentTableBlock) {
                    Swap.enchantingTableSwap(player, message.slot, message.hand, message.pos);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
