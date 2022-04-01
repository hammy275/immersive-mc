package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ImmersiveBreakPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
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
                    tileEntity instanceof BrewingStandTileEntity;
        } else {
            sendBreakPacket = state.getBlock() == Blocks.CRAFTING_TABLE;
        }

        if (sendBreakPacket) {
            IChunk chunk = event.getWorld().getChunk(event.getPos());
            if (chunk instanceof Chunk) {
                Network.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) chunk),
                        new ImmersiveBreakPacket(event.getPos()));
            }
        }

    }
}
