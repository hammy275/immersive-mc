package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.render.ImmersiveFurnace;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ClientLogicSubscriber {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (Minecraft.getInstance().gameMode == null) return;

        PlayerEntity player = event.player;

        // Get block that we're looking at
        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return;

        BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        TileEntity tileEntity = player.level.getBlockEntity(pos);

        if (tileEntity instanceof AbstractFurnaceTileEntity) {
            AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) tileEntity;
            ImmersiveFurnace.trackFurnace(furnace);
        }
    }

}
