package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.SelfHandlingNetworkStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Storage for syncing chest contents. Only the openness and isDirty are kept on the server continuously, and the
 * isDirty only denotes if the lid handling is dirty, not the chest as a whole.
 * <br>
 * During syncing, there are generally two instances, one is the instance stored in the server, and the other
 * is the instance created with the items to be sent to clients.
 */
public class ChestOpennessStorage implements SelfHandlingNetworkStorage {

    private BlockPos pos = BlockPos.ZERO;
    private float openness = -1f;
    private transient Level level = null;  // Only available on the server
    private boolean isDirty = true;

    public ChestOpennessStorage() {
    }

    public ChestOpennessStorage(BlockEntity blockEntity) {
        this.openness = Util.getChestLidController(blockEntity).immersiveMC$getOpenness();
        this.pos = blockEntity.getBlockPos();
        this.level = blockEntity.getLevel();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeFloat(openness);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.openness = buffer.readFloat();
    }

    public float getOpenness() {
        return openness;
    }

    public void setOpenness(float openness) {
        this.openness = openness;
        isDirty = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Level getLevel() {
        if (level == null) {
            throw new IllegalStateException("Can only access ChestOpennessStorage's level on the server.");
        }
        return level;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setNoLongerDirty() {
        isDirty = false;
    }

    @Override
    public void handleClient() {
        ChestInfo info = ClientUtil.findImmersive(Immersives.immersiveChest, pos);
        if (info != null) {
            info.forcedOpenness = openness;
        }
    }
}
