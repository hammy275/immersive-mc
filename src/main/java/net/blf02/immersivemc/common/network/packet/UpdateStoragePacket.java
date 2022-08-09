package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.AbstractWorldStorageImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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

    public static void handle(final UpdateStoragePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                for (AbstractWorldStorageImmersive<? extends AbstractWorldStorageInfo> immersive : Immersives.WS_IMMERSIVES) {
                    for (AbstractWorldStorageInfo info : immersive.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(message.pos)) {
                            immersive.processStorageFromNetwork(info, message.storage);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
