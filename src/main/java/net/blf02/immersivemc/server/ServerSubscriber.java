package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.immersive.CheckerFunction;
import net.blf02.immersivemc.common.immersive.ImmersiveCheckers;
import net.blf02.immersivemc.common.network.Distributors;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.blf02.immersivemc.common.network.packet.ImmersiveBreakPacket;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.storage.LevelStorage;
import net.blf02.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerSubscriber {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void blockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return; // Only run server-side
        ServerLevel world = (ServerLevel) event.getWorld();
        BlockState state = event.getState();
        boolean sendBreakPacket = false;

        if (LevelStorage.usesWorldStorage(event.getPos(), state, world.getBlockEntity(event.getPos()), world)) {
            ImmersiveStorage storage = LevelStorage.getStorage(world).remove(event.getPos());
            if (storage != null && event.getPlayer().level != null) {
                for (int i = 0;
                     i <= GetStorage.getLastInputIndex(event.getPos(), state, world.getBlockEntity(event.getPos()), world);
                     i++) {
                    Vec3 vecPos = Vec3.atCenterOf(event.getPos());
                    ItemStack stack = storage.items[i];
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(event.getPlayer().level,
                                vecPos.x, vecPos.y, vecPos.z, stack);
                        event.getWorld().addFreshEntity(itemEnt);
                    }
                }
            }
        }

        for (CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> checker : ImmersiveCheckers.CHECKERS) {
            if (checker.apply(event.getPos(), event.getWorld().getBlockState(event.getPos()),
                    event.getWorld().getBlockEntity(event.getPos()), event.getPlayer().level)) {
                sendBreakPacket = true;
                break;
            }
        }

        if (sendBreakPacket) {
            Network.INSTANCE.send(
                    Distributors.NEARBY_POSITION.with(() -> new Distributors.NearbyDistributorData(event.getPos(), 20)),
                    new ImmersiveBreakPacket(event.getPos()));
            ChestToOpenCount.chestImmersiveOpenCount.remove(event.getPos());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (AbstractTracker tracker : ServerTrackerInit.globalTrackers) {
            tracker.doTick(null);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        for (AbstractTracker tracker : ServerTrackerInit.playerTrackers) {
            tracker.doTick(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayer) {
            ActiveConfig.loadConfigFromFile(true);
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()),
                    new ConfigSyncPacket());
        }


    }
}
