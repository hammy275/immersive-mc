package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Distributors;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.blf02.immersivemc.common.network.packet.ImmersiveBreakPacket;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerSubscriber {

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return; // Only run server-side
        BlockState state = event.getState();
        boolean sendBreakPacket = false;

        if (state.hasTileEntity()) {
            TileEntity tileEntity = event.getWorld().getBlockEntity(event.getPos());
            sendBreakPacket = tileEntity instanceof AbstractFurnaceTileEntity ||
                    tileEntity instanceof JukeboxTileEntity ||
                    tileEntity instanceof BrewingStandTileEntity ||
                    tileEntity instanceof ChestTileEntity;
        } else {
            sendBreakPacket = state.getBlock() == Blocks.CRAFTING_TABLE;
        }

        if (sendBreakPacket) {
            Network.INSTANCE.send(
                    Distributors.NEARBY_POSITION.with(() -> new Distributors.NearbyDistributorData(event.getPos(), 20)),
                    new ImmersiveBreakPacket(event.getPos()));
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
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayerEntity) {
            ActiveConfig.loadConfigFromFile();
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                    new ConfigSyncPacket());
        }


    }
}
