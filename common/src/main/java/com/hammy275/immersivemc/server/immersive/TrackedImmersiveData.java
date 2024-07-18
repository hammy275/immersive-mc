package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public class TrackedImmersiveData<S extends NetworkStorage> {

    public static final double maxDist = CommonConstants.distanceToRemoveImmersive;

    public final UUID playerUUID;
    private final Set<BlockPos> pos;
    private final ImmersiveHandler<S> handler;
    private final Level level;
    private final Vec3 center;

    public TrackedImmersiveData(UUID playerUUID, Set<BlockPos> pos, ImmersiveHandler<S> handler, Level level) {
        this.playerUUID = playerUUID;
        this.pos = pos;
        this.handler = handler;
        this.level = level;
        this.center = Util.average(pos);
    }

    public boolean shouldSync(ServerPlayer player) {
        return this.handler.isDirtyForClientSync(player, this.pos.iterator().next());
    }

    public FetchInventoryPacket<S> getSyncPacket(ServerPlayer player) {
        return new FetchInventoryPacket<>(handler, handler.makeInventoryContents(player, pos.iterator().next()), pos.iterator().next());
    }

    public boolean validForPlayer(ServerPlayer player) {
        return blockMatches() &&
                player.distanceToSqr(center) <= maxDist*maxDist &&
                this.handler.enabledInConfig(player);
    }

    public Set<BlockPos> getPos() {
        return this.pos;
    }

    public Level getLevel() {
        return this.level;
    }

    public ImmersiveHandler<S> getHandler() {
        return this.handler;
    }

    public boolean blockMatches() {
        return Util.isValidBlocks(handler, pos, level);
    }
}
