package net.blf02.immersivemc.server;

import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.immersive.CheckerFunction;
import net.blf02.immersivemc.common.immersive.ImmersiveCheckers;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.NetworkHandler;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.blf02.immersivemc.common.network.packet.ImmersiveBreakPacket;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.storage.ImmersiveMCLevelStorage;
import net.blf02.immersivemc.server.tracker.ServerTrackerInit;
import net.blf02.immersivemc.server.tracker.ServerVRSubscriber;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ServerSubscriber {

    public static EventResult blockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (level.isClientSide) return EventResult.pass(); // Only run server-side
        ServerLevel world = (ServerLevel) level;
        boolean sendBreakPacket = false;

        if (ImmersiveMCLevelStorage.usesWorldStorage(pos, state, world.getBlockEntity(pos), world)) {
            ImmersiveStorage storage = ImmersiveMCLevelStorage.getLevelStorage(world).remove(pos);
            if (storage != null) {
                for (int i = 0;
                     i <= GetStorage.getLastInputIndex(pos, state, world.getBlockEntity(pos), world);
                     i++) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ItemStack stack = storage.items[i];
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(level,
                                vecPos.x, vecPos.y, vecPos.z, stack);
                        level.addFreshEntity(itemEnt);
                    }
                }
            }
        }

        for (CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> checker : ImmersiveCheckers.CHECKERS) {
            if (checker.apply(pos, level.getBlockState(pos),
                    level.getBlockEntity(pos), level)) {
                sendBreakPacket = true;
                break;
            }
        }

        if (sendBreakPacket) {
            AABB nearbyBox = AABB.ofSize(Vec3.atCenterOf(pos), 20, 20, 20);
            List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, nearbyBox);
            Network.INSTANCE.sendToPlayers(nearby, new ImmersiveBreakPacket(pos));
            ChestToOpenCount.chestImmersiveOpenCount.remove(pos);
        }

        return EventResult.pass();
    }

    public static void onServerTick(MinecraftServer server) {
        for (AbstractTracker tracker : ServerTrackerInit.globalTrackers) {
            tracker.doTick(null);
        }
        NetworkHandler.INSTANCE.tick(false);
    }

    public static void onPlayerTick(Player player) {
        if (player.level.isClientSide) return;
        for (AbstractTracker tracker : ServerTrackerInit.playerTrackers) {
            tracker.doTick(player);
        }
        if (VRPluginVerify.hasAPI) {
            ServerVRSubscriber.vrPlayerTick(player);
        }
    }

    public static void onPlayerJoin(Player player) {
        if (!player.level.isClientSide && player instanceof ServerPlayer) {
            ActiveConfig.loadConfigFromFile(true);
            Network.INSTANCE.sendToPlayer((ServerPlayer) player,
                    new ConfigSyncPacket());
        }


    }
}
