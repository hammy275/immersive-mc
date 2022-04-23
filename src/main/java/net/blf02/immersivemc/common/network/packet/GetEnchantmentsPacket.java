package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class GetEnchantmentsPacket {

    public final ItemStack stack;
    public final BlockPos pos;

    public final int weakXPLevels;
    public final int weakEnchHint;
    public final int weakLevelHint;

    public final int midXPLevels;
    public final int midEnchHint;
    public final int midLevelHint;

    public final int strongXPLevels;
    public final int strongEnchHint;
    public final int strongLevelHint;

    public GetEnchantmentsPacket(ItemStack stack, BlockPos pos) {
        this.stack = stack;
        this.pos = pos;

        this.weakXPLevels = -1;
        this.weakEnchHint = -1;
        this.weakLevelHint = -1;
        this.midXPLevels = -1;
        this.midEnchHint = -1;
        this.midLevelHint = -1;
        this.strongXPLevels = -1;
        this.strongEnchHint = -1;
        this.strongLevelHint = -1;
    }

    public GetEnchantmentsPacket(int weakXPLevels, int weakEnchHint, int weakLevelHint,
                                 int midXPLevels, int midEnchHint, int midLevelHint,
                                 int strongXPLevels, int strongEnchHint, int strongLevelHint) {
        this.weakXPLevels = weakXPLevels; // XP levels needed
        this.weakEnchHint = weakEnchHint; // Enchantment you'll get
        this.weakLevelHint = weakLevelHint; // Enchantment level you'll get
        this.midXPLevels = midXPLevels;
        this.midEnchHint = midEnchHint;
        this.midLevelHint = midLevelHint;
        this.strongXPLevels = strongXPLevels;
        this.strongEnchHint = strongEnchHint;
        this.strongLevelHint = strongLevelHint;

        this.stack = null;
        this.pos = null;
    }

    public static void encode(GetEnchantmentsPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.stack != null); // Whether we're asking for enchs (true) or getting them (false)
        if (packet.stack != null) {
            buffer.writeItem(packet.stack);
            buffer.writeBlockPos(packet.pos);
        } else {
            buffer.writeInt(packet.weakXPLevels);
            buffer.writeInt(packet.weakEnchHint);
            buffer.writeInt(packet.weakLevelHint);
            buffer.writeInt(packet.midXPLevels);
            buffer.writeInt(packet.midEnchHint);
            buffer.writeInt(packet.midLevelHint);
            buffer.writeInt(packet.strongXPLevels);
            buffer.writeInt(packet.strongEnchHint);
            buffer.writeInt(packet.strongLevelHint);
        }
    }

    public static GetEnchantmentsPacket decode(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return new GetEnchantmentsPacket(buffer.readItem(), buffer.readBlockPos());
        } else {
            return new GetEnchantmentsPacket(buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    }

    public static void handle(GetEnchantmentsPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) {
                handleClient(message);
            } else if (NetworkUtil.safeToRun(message.pos, player)){
                TileEntity tileEnt = player.level.getBlockEntity(message.pos);
                if (tileEnt instanceof EnchantingTableTileEntity) {
                    EnchantmentContainer container = new EnchantmentContainer(-1,
                            player.inventory, IWorldPosCallable.create(player.level, message.pos));
                    container.setItem(1, new ItemStack(Items.LAPIS_LAZULI, 64));
                    container.setItem(0, message.stack);

                    int[] xpLevels = container.costs;
                    int[] descs = container.enchantClue;
                    int[] enchLevels = container.levelClue;


                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new GetEnchantmentsPacket(
                                    xpLevels[0], descs[0], enchLevels[0],
                                    xpLevels[1], descs[1], enchLevels[1],
                                    xpLevels[2], descs[2], enchLevels[2]
                            ));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected static void handleClient(GetEnchantmentsPacket message) {
        Enchantment ench = getEnch(message.weakEnchHint);
        if (ench != null) {
            ClientStorage.weakInfo.levelsNeeded = message.weakXPLevels;
            ClientStorage.weakInfo.textPreview = getDesc(ench, message.weakLevelHint, message.weakXPLevels);
        } else {
            ClientStorage.weakInfo.textPreview = null;
        }

        ench = getEnch(message.midEnchHint);
        if (ench != null) {
            ClientStorage.midInfo.levelsNeeded = message.midXPLevels;
            ClientStorage.midInfo.textPreview = getDesc(ench, message.midLevelHint, message.midXPLevels);
        } else {
            ClientStorage.midInfo.textPreview = null;
        }

        ench = getEnch(message.strongEnchHint);
        if (ench != null) {
            ClientStorage.strongInfo.levelsNeeded = message.strongXPLevels;
            ClientStorage.strongInfo.textPreview = getDesc(ench, message.strongLevelHint, message.strongXPLevels);
        } else {
            ClientStorage.strongInfo.textPreview = null;
        }


    }

    protected static Enchantment getEnch(int id) {
        return Enchantment.byId(id);
    }

    protected static StringTextComponent getDesc(Enchantment ench, int enchLevel, int xpLevels) {
        String levelStr = enchLevel == 1 ? " level for " : " levels for ";
        return new StringTextComponent(xpLevels + levelStr + ench.getFullname(enchLevel).getString() + "...?");
    }
}
