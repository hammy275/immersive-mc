package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class UpdateStoragePacket {

    public final BlockPos pos;
    public final ImmersiveStorage storage;
    public final String storageType;

    public UpdateStoragePacket(BlockPos pos, ImmersiveStorage storage, String storageType) {
        this.pos = pos;
        this.storage = storage;
        this.storageType = storageType;
    }

    public static void encode(UpdateStoragePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeNbt(packet.storage.save(new CompoundTag())).writeUtf(packet.storageType);
    }

    public static UpdateStoragePacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag nbt = buffer.readNbt();
        String type = buffer.readUtf();

        ImmersiveStorage storage = GetStorage.assembleStorage(nbt, type, null);
        return new UpdateStoragePacket(pos, storage, type);
    }

    public static void handle(final UpdateStoragePacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) {
                for (AbstractImmersive<? extends AbstractImmersiveInfo> immersive : Immersives.WS_IMMERSIVES) {
                    for (AbstractImmersiveInfo info : immersive.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(message.pos)) {
                            immersive.processStorageFromNetwork(info, message.storage);
                        }
                    }
                }
            }
        });
        
    }
}
