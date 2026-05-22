package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.storage.network.SelfHandlingNetworkStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SelfHandlingNetworkStorageSyncPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.mixin.ChestBlockEntityAccessor;
import com.hammy275.immersivemc.mixin.ContainerOpenersCounterAccessor;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.hammy275.immersivemc.server.ServerMixinProxy;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Storage for syncing chest contents. Only the openness and isDirty are kept on the server continuously, and the
 * isDirty only denotes if the lid handling is dirty, not the chest as a whole.
 * <br>
 * During syncing, there are generally two instances, one is the instance stored in the server, and the other
 * is the instance created with the items to be sent to clients.
 */
public class ChestOpennessStorage implements SelfHandlingNetworkStorage {

    public static final float CHEST_OPEN_THRESHOLD = 0.1f;

    private BlockPos pos = BlockPos.ZERO;
    private float openness = -1f;
    private transient float oldOpenness = 0f;
    private @Nullable UUID controllingPlayerUUID = null;
    private transient AnimationState animationState = AnimationState.PLAYER_CONTROLLED;
    private transient ServerLevel level = null;  // Only available on the server
    private transient LidBlockEntity chest = null;  // Only available on the server
    private boolean isDirty = true;
    private LidTarget lidTarget = LidTarget.CLOSED; // Default to closing if a player in VR leaves it

    public ChestOpennessStorage() {
    }

    public ChestOpennessStorage(BlockEntity blockEntity) {
        this.pos = blockEntity.getBlockPos();
        this.level = (ServerLevel) blockEntity.getLevel();
        this.chest = (LidBlockEntity) blockEntity;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos)
                .writeFloat(openness)
                .writeInt(lidTarget.ordinal())
                .writeBoolean(controllingPlayerUUID != null);
        if (controllingPlayerUUID != null) {
            buffer.writeUUID(controllingPlayerUUID);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.openness = buffer.readFloat();
        this.lidTarget = LidTarget.values()[buffer.readInt()];
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
    public ServerPlayer getControllingPlayer(Level level) {
        return controllingPlayerUUID != null ? (ServerPlayer) level.getPlayerByUUID(controllingPlayerUUID) : null;
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
        ServerPlayer controllingPlayer = getControllingPlayer(level);
        if (controllingPlayer == null) {
            if (controllingPlayerUUID != null) {
                controllingPlayerUUID = null;
                setDirty();
            }
        } else {
            if (animationState == AnimationState.PLAYER_CONTROLLED) {
                lidTarget = openness >= CHEST_OPEN_THRESHOLD ? LidTarget.OPEN : LidTarget.CLOSED;
                setDirty();
            }
            if (isAnimating()) {
                ChestBlockEntity other = null;
                if (chest instanceof ChestBlockEntity cbe) {
                    other = Util.getOtherChest(cbe);
                }

                if (animationState == AnimationState.ANIMATED) {
                    if (lidTarget == LidTarget.OPEN) {
                        openness = Mth.clamp(Math.max(openness, 0f) + 0.1f, 0, 1);
                    } else {
                        openness = Mth.clamp(openness - 0.1f, 0, 1);
                        if (openness == 0) {
                            openness = -1f; // Give vanilla control back when the chest is closed again
                        }
                    }
                    if (openness != oldOpenness) {
                        setDirty();
                    }
                }

                if (animationState == AnimationState.PLAYER_CONTROLLED) {
                    if (openness < CHEST_OPEN_THRESHOLD && oldOpenness >= CHEST_OPEN_THRESHOLD) {
                        doChestClose(controllingPlayer, other);
                    } else if (openness >= CHEST_OPEN_THRESHOLD && oldOpenness < CHEST_OPEN_THRESHOLD) {
                        doChestOpen(controllingPlayer, other);
                    }
                }
            }

            if (animationState != AnimationState.PLAYER_CONTROLLED && !isAnimating()) {
                controllingPlayerUUID = null;
                setDirty();
            }
        }

        if (isDirty()) {
            ServerLevel level = (ServerLevel) ((BlockEntity) chest).getLevel();
            List<ServerPlayer> toSendTo = TrackedImmersives.getPlayersTrackingPos(level.getServer(), level, this.pos);
            if (animationState == AnimationState.PLAYER_CONTROLLED && controllingPlayer != null) {
                toSendTo = toSendTo.stream().filter(player -> player != controllingPlayer).toList();
            }
            Network.INSTANCE.sendToPlayers(toSendTo, new SelfHandlingNetworkStorageSyncPacket(this));
            isDirty = false;
        }
        oldOpenness = Math.max(openness, 0f);
    }

    private void doChestOpen(ServerPlayer controllingPlayer, @Nullable ChestBlockEntity other) {
        ServerMixinProxy.skipIncrementDecrementChests = true;
        try {
            if (chest instanceof ChestBlockEntity cbe) {
                ChestToOpenSet.openChest(controllingPlayer, pos);
                cbe.startOpen(controllingPlayer);
                if (other != null) {
                    ChestToOpenSet.openChest(controllingPlayer, other.getBlockPos());
                    other.startOpen(controllingPlayer);
                }
                PiglinAi.angerNearbyPiglins(controllingPlayer, true);
                Lootr.lootrImpl.markOpener(controllingPlayer, pos);
            } else if (chest instanceof EnderChestBlockEntity ecbe) {
                ChestToOpenSet.openChest(controllingPlayer, pos);
                ecbe.startOpen(controllingPlayer);
                PiglinAi.angerNearbyPiglins(controllingPlayer, true);
            }
        } finally {
            ServerMixinProxy.skipIncrementDecrementChests = false;
        }

    }

    private void doChestClose(ServerPlayer controllingPlayer, @Nullable ChestBlockEntity other) {
        ServerMixinProxy.skipIncrementDecrementChests = true;
        // When closing, immediately recheckOpeners to sync the "closed" state to the client, which otherwise
        // thinks it's open.
        try {
            if (chest instanceof ChestBlockEntity cbe) {
                ChestToOpenSet.closeForAll(controllingPlayer.level, pos);
                ContainerOpenersCounter openersCounter = ((ChestBlockEntityAccessor) cbe).immersiveMC$openersCounter();
                Util.getPlayersWithContainerOpen(openersCounter, level).forEach(cbe::stopOpen);
                openersCounter.recheckOpeners(cbe.getLevel(), cbe.getBlockPos(), cbe.getLevel().getBlockState(cbe.getBlockPos()));
                if (other != null) {
                    ChestToOpenSet.closeForAll(controllingPlayer.level, other.getBlockPos());
                    openersCounter = ((ChestBlockEntityAccessor) other).immersiveMC$openersCounter();
                    Util.getPlayersWithContainerOpen(openersCounter, level).forEach(other::stopOpen);
                    openersCounter.recheckOpeners(other.getLevel(), other.getBlockPos(), other.getLevel().getBlockState(other.getBlockPos()));
                }
            } else if (chest instanceof EnderChestBlockEntity ecbe) {
                ChestToOpenSet.closeForAll(controllingPlayer.level, pos);
                ContainerOpenersCounter openersCounter = ((ChestBlockEntityAccessor) ecbe).immersiveMC$openersCounter();
                Util.getPlayersWithContainerOpen(openersCounter, level).forEach(ecbe::stopOpen);
                openersCounter.recheckOpeners(ecbe.getLevel(), ecbe.getBlockPos(), ecbe.getLevel().getBlockState(ecbe.getBlockPos()));
            }
        } finally {
            ServerMixinProxy.skipIncrementDecrementChests = false;
        }
    }

    public boolean takeControl(UUID newController, AnimationState newAnimationState) {
        // No control if you aren't the one controlling it
        if (controllingPlayerUUID != null && !controllingPlayerUUID.equals(newController)) {
            return false;
        }
        // Chests in an animation cannot be "unanimated"
        if (this.animationState == AnimationState.ANIMATED && isAnimating()) {
            return false;
        }
        // Actually take control of the chest
        controllingPlayerUUID = newController;
        this.animationState = newAnimationState;
        return true;
    }

    public void startAnimating(ServerPlayer controllingPlayer, LidTarget newTarget) {
        this.lidTarget = newTarget;
        this.animationState = AnimationState.ANIMATED;
        if (newTarget == LidTarget.OPEN) {
            doChestOpen(controllingPlayer, Util.getOtherChest((ChestBlockEntity) chest));
        } else {
            doChestClose(controllingPlayer, Util.getOtherChest((ChestBlockEntity) chest));
        }
    }

    public boolean isAnimating() {
        return !((lidTarget == LidTarget.CLOSED && openness == -1f) || (lidTarget == LidTarget.OPEN && openness == 1f));
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
        ChestOpennessStorage actual = SharedNetworkStorages.instance().get(player.level, this.pos, ChestOpennessStorage.class);
        if (actual != null && VRVerify.playerInVR(player) && actual.takeControl(player.getUUID(), AnimationState.PLAYER_CONTROLLED)) {
            actual.openness = Mth.clamp(this.openness, 0f, 1f);
            actual.animationState = this.animationState;
        }
    }

    public enum LidTarget {
        CLOSED,
        OPEN;
    }

    public enum AnimationState {
        ANIMATED,
        PLAYER_CONTROLLED
    }
}
