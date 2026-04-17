package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.storage.network.SelfHandlingNetworkStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
    private @Nullable UUID controllingPlayerUUID = null;
    public int cooldown = 0;
    private transient ServerLevel level = null;  // Only available on the server
    private transient LidBlockEntity chest = null;  // Only available on the server
    private boolean isDirty = true;
    public LidTargetState lidTargetState = LidTargetState.CLOSED; // Default to closing if a player in VR leaves it

    public ChestOpennessStorage() {
    }

    public ChestOpennessStorage(BlockEntity blockEntity) {
        this.openness = Util.getChestLidController(blockEntity).immersiveMC$getOpenness();
        this.pos = blockEntity.getBlockPos();
        this.level = (ServerLevel) blockEntity.getLevel();
        this.chest = (LidBlockEntity) blockEntity;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos)
                .writeFloat(openness)
                .writeInt(cooldown)
                .writeEnum(lidTargetState)
                .writeBoolean(controllingPlayerUUID != null);
        if (controllingPlayerUUID != null) {
            buffer.writeUUID(controllingPlayerUUID);
        }
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.openness = buffer.readFloat();
        this.cooldown = buffer.readInt();
        this.lidTargetState = buffer.readEnum(LidTargetState.class);
        if (buffer.readBoolean()) {
            this.controllingPlayerUUID = buffer.readUUID();
        }
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

    @Nullable
    public Player getControllingPlayer(Level level) {
        return controllingPlayerUUID != null ? level.getPlayerByUUID(controllingPlayerUUID) : null;
    }

    @Nullable
    public UUID getControllingPlayerUUID() {
        return controllingPlayerUUID;
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

    public void setDirty() {
        isDirty = true;
    }

    public void setNoLongerDirty() {
        isDirty = false;
    }

    public void serverTick() {
        Player controllingPlayer = getControllingPlayer(level);
        if (controllingPlayer == null) {
            if (controllingPlayerUUID != null) {
                controllingPlayerUUID = null;
                setDirty();
            }
        } else {
            if (VRVerify.playerInVR(controllingPlayer)) {

            } else {
                if (chest instanceof ChestBlockEntity cbe) {
                    ChestBlockEntity other = Util.getOtherChest(cbe);
                    if (lidTargetState == LidTargetState.OPEN) {
                        if (!ChestToOpenSet.hasChestOpen(controllingPlayer, pos)) {
                            cbe.startOpen(controllingPlayer);
                            ChestToOpenSet.openChest(controllingPlayer, pos);
                            if (other != null) {
                                other.startOpen(controllingPlayer);
                                ChestToOpenSet.openChest(controllingPlayer, other.getBlockPos());
                            }
                            PiglinAi.angerNearbyPiglins(level, controllingPlayer, true);
                            ChestShulkerOpenPacket.handle(new ChestShulkerOpenPacket(pos, true), (ServerPlayer) controllingPlayer);
                            Lootr.lootrImpl.markOpener(controllingPlayer, pos);
                        }
                    } else {
                        if (ChestToOpenSet.hasChestOpen(controllingPlayer, pos)) {
                            cbe.stopOpen(controllingPlayer);
                            ChestToOpenSet.closeChest(controllingPlayer, pos);
                            if (other != null) {
                                other.stopOpen(controllingPlayer);
                                ChestToOpenSet.closeChest(controllingPlayer, other.getBlockPos());
                            }
                        }
                    }
                } else if (chest instanceof EnderChestBlockEntity ecbe) {
                    if (lidTargetState == LidTargetState.OPEN) {
                        if (!ChestToOpenSet.hasChestOpen(controllingPlayer, pos)) {
                            ecbe.startOpen(controllingPlayer);
                            ChestToOpenSet.openChest(controllingPlayer, pos);
                            PiglinAi.angerNearbyPiglins(level, controllingPlayer, true);
                            ChestShulkerOpenPacket.handle(new ChestShulkerOpenPacket(pos, true), (ServerPlayer) controllingPlayer);
                        }
                    } else {
                        if (ChestToOpenSet.hasChestOpen(controllingPlayer, pos)) {
                            ecbe.stopOpen(controllingPlayer);
                            ChestToOpenSet.closeChest(controllingPlayer, pos);
                        }
                    }
                }
            }
        }
        if (cooldown > 0) {
            cooldown--;
        } else {
            controllingPlayerUUID = null;
        }
    }

    public boolean takeControl(UUID newController) {
        if (cooldown > 0 || (controllingPlayerUUID != null && !controllingPlayerUUID.equals(newController))) {
            return false;
        }
        controllingPlayerUUID = newController;
        return true;
    }

    @Override
    public void handleClient() {
        ChestInfo info = ClientUtil.findImmersive(Immersives.immersiveChest, pos);
        if (info != null) {
            info.setOpennessStorage(this);
        }
    }

    @Override
    public void handleServer(ServerPlayer player) {
        ChestOpennessStorage actual = SharedNetworkStorages.instance().get(player.level(), this.pos, ChestOpennessStorage.class);
        if (actual != null && VRVerify.playerInVR(player) && actual.takeControl(player.getUUID())) {
            actual.openness = Mth.clamp(this.openness, 0f, 1f);
            Network.INSTANCE.sendToPlayers(TrackedImmersives.getPlayersTrackingPos(player.server, player.level(), this.pos), actual);
        }
    }

    public enum LidTargetState {
        CLOSED,
        OPEN;
    }
}
