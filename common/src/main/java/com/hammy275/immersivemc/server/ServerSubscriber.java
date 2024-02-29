package com.hammy275.immersivemc.server;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.storage.ImmersiveMCLevelStorage;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ServerSubscriber {

    public static EventResult blockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (level.isClientSide) return EventResult.pass(); // Only run server-side
        ServerLevel world = (ServerLevel) level;

        if (ImmersiveMCLevelStorage.usesWorldStorage(pos, state, world.getBlockEntity(pos), world)) {
            ImmersiveStorage storage = ImmersiveMCLevelStorage.getLevelStorage(world).remove(pos);
            if (storage != null) {
                for (int i = 0;
                     i <= GetStorage.getLastInputIndex(pos, state, world.getBlockEntity(pos), world);
                     i++) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ItemStack stack = storage.getItem(i);
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(level,
                                vecPos.x, vecPos.y, vecPos.z, stack);
                        level.addFreshEntity(itemEnt);
                    }
                }
            }
        }

        return EventResult.pass();
    }

    public static void onServerTick(MinecraftServer server) {
        for (AbstractTracker tracker : ServerTrackerInit.globalTrackers) {
            tracker.doTick(null);
        }
        TrackedImmersives.tick(server);
        DirtyTracker.unmarkAllDirty();
    }

    public static void onPlayerTick(Player playerIn) {
        if (playerIn.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) playerIn;
        for (AbstractTracker tracker : ServerTrackerInit.playerTrackers) {
            tracker.doTick(player);
        }
        if (VRPluginVerify.hasAPI) {
            ServerVRSubscriber.vrPlayerTick(player);
        }

        // Get looking at immersive
        HitResult hit = player.pick(20, 0, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            TrackedImmersives.maybeTrackImmersive(player, blockHit.getBlockPos());
        }
    }

    public static void onPlayerJoin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Network.INSTANCE.sendToPlayer(serverPlayer,
                    new ConfigSyncPacket(ActiveConfig.FILE));
        }


    }
}
