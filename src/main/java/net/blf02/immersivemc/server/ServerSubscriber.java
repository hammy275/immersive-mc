package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Distributors;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ConfigSyncPacket;
import net.blf02.immersivemc.common.network.packet.ImmersiveBreakPacket;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.storage.WorldStorage;
import net.blf02.immersivemc.server.storage.info.ImmersiveStorage;
import net.blf02.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.block.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerSubscriber {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void blockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isClientSide()) return; // Only run server-side
        ServerWorld world = (ServerWorld) event.getWorld();
        BlockState state = event.getState();
        boolean sendBreakPacket = false;

        if (WorldStorage.usesWorldStorage(state)) {
            ImmersiveStorage storage = WorldStorage.getStorage(world).remove(event.getPos());
            if (storage != null && event.getPlayer().level != null) {
                for (int i = 0; i <= GetStorage.getLastInputIndex(state); i++) {
                    Vector3d vecPos = Vector3d.atCenterOf(event.getPos());
                    ItemStack stack = storage.items[i];
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(event.getPlayer().level,
                                vecPos.x, vecPos.y, vecPos.z, stack);
                        event.getWorld().addFreshEntity(itemEnt);
                    }
                }
            }
        }

        if (state.hasTileEntity()) {
            TileEntity tileEntity = event.getWorld().getBlockEntity(event.getPos());
            sendBreakPacket = tileEntity instanceof AbstractFurnaceTileEntity ||
                    tileEntity instanceof JukeboxTileEntity ||
                    tileEntity instanceof BrewingStandTileEntity ||
                    tileEntity instanceof ChestTileEntity || tileEntity instanceof EnderChestTileEntity;
        } else {
            sendBreakPacket = state.getBlock() == Blocks.CRAFTING_TABLE ||
            state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock
            || state.getBlock() instanceof EnchantingTableBlock || state.getBlock() instanceof RepeaterBlock;
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
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayerEntity) {
            ActiveConfig.loadConfigFromFile(true);
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                    new ConfigSyncPacket());
        }


    }
}
