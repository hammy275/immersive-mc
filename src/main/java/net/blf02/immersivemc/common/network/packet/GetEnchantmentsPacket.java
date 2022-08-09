package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.ImmersiveETable;
import net.blf02.immersivemc.client.immersive.info.EnchantingInfo;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class GetEnchantmentsPacket {

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

    public GetEnchantmentsPacket(BlockPos pos) {
        this.pos = pos;

        this.weakXPLevels = -999;
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
                                 int strongXPLevels, int strongEnchHint, int strongLevelHint,
                                 BlockPos pos) {
        this.weakXPLevels = weakXPLevels; // XP levels needed
        this.weakEnchHint = weakEnchHint; // Enchantment you'll get
        this.weakLevelHint = weakLevelHint; // Enchantment level you'll get
        this.midXPLevels = midXPLevels;
        this.midEnchHint = midEnchHint;
        this.midLevelHint = midLevelHint;
        this.strongXPLevels = strongXPLevels;
        this.strongEnchHint = strongEnchHint;
        this.strongLevelHint = strongLevelHint;

        this.pos = pos;
    }

    public static void encode(GetEnchantmentsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.weakXPLevels == -999); // Whether we're asking for enchs (true) or getting them (false)
        if (packet.weakXPLevels == -999) {
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
            buffer.writeBlockPos(packet.pos);
        }
    }

    public static GetEnchantmentsPacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return new GetEnchantmentsPacket(buffer.readBlockPos());
        } else {
            return new GetEnchantmentsPacket(buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readBlockPos());
        }
    }

    public static void handle(GetEnchantmentsPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                handleClient(message);
            } else if (NetworkUtil.safeToRun(message.pos, player)) {
                ImmersiveStorage enchantStorage = GetStorage.getEnchantingStorage(player, message.pos);
                if (enchantStorage.items[0] != null && !enchantStorage.items[0].isEmpty()) {
                    BlockEntity tileEnt = player.level.getBlockEntity(message.pos);
                    if (tileEnt instanceof EnchantmentTableBlockEntity) {
                        EnchantmentMenu container = new EnchantmentMenu(-1,
                                player.getInventory(), ContainerLevelAccess.create(player.level, message.pos));
                        container.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
                        container.setItem(0, 0, enchantStorage.items[0]);

                        int[] xpLevels = container.costs;
                        int[] descs = container.enchantClue;
                        int[] enchLevels = container.levelClue;


                        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                new GetEnchantmentsPacket(
                                        xpLevels[0], descs[0], enchLevels[0],
                                        xpLevels[1], descs[1], enchLevels[1],
                                        xpLevels[2], descs[2], enchLevels[2],
                                        message.pos
                                ));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected static void handleClient(GetEnchantmentsPacket message) {
        for (EnchantingInfo info : ImmersiveETable.singleton.getTrackedObjects()) {
            if (info.getBlockPosition().equals(message.pos)) {
                Enchantment ench = getEnch(message.weakEnchHint);
                if (ench != null) {
                    info.weakInfo.levelsNeeded = message.weakXPLevels;
                    info.weakInfo.textPreview = getDesc(ench, message.weakLevelHint);
                } else {
                    info.weakInfo.textPreview = null;
                }

                ench = getEnch(message.midEnchHint);
                if (ench != null) {
                    info.midInfo.levelsNeeded = message.midXPLevels;
                    info.midInfo.textPreview = getDesc(ench, message.midLevelHint);
                } else {
                    info.midInfo.textPreview = null;
                }

                ench = getEnch(message.strongEnchHint);
                if (ench != null) {
                    info.strongInfo.levelsNeeded = message.strongXPLevels;
                    info.strongInfo.textPreview = getDesc(ench, message.strongLevelHint);
                } else {
                    info.strongInfo.textPreview = null;
                }
            }
        }
    }

    protected static Enchantment getEnch(int id) {
        return Enchantment.byId(id);
    }

    protected static TextComponent getDesc(Enchantment ench, int enchLevel) {
        return new TextComponent(ench.getFullname(enchLevel).getString() + "...?");
    }
}
