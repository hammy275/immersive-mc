package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TrackedImmersiveData {

    public static final double maxDist = CommonConstants.distanceToRemoveImmersive;

    public final UUID playerUUID;
    private final BlockPos pos;
    private final ImmersiveHandler handler;
    private final Level level;

    public TrackedImmersiveData(UUID playerUUID, BlockPos pos, ImmersiveHandler handler, Level level) {
        this.playerUUID = playerUUID;
        this.pos = pos;
        this.handler = handler;
        this.level = level;
    }

    public boolean shouldSync(ServerPlayer player) {
        return this.handler.isDirtyForClientSync(player, this.pos);
    }

    public void didSync(ServerPlayer player) {
        this.handler.clearDirtyForClientSync(player, this.pos);
    }

    public FetchInventoryPacket getSyncPacket(ServerPlayer player) {
        return new FetchInventoryPacket(handler, handler.makeInventoryContents(player, pos), pos);
    }

    public boolean validForPlayer(ServerPlayer player) {
        return blockMatches() &&
                player.distanceToSqr(Vec3.atCenterOf(pos)) <= maxDist*maxDist &&
                this.handler.enabledInConfig(ActiveConfig.getConfigForPlayer(player));
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Level getLevel() {
        return this.level;
    }

    private boolean blockMatches() {
        return handler.isValidBlock(pos, level.getBlockState(pos), level.getBlockEntity(pos), level);
    }
}
