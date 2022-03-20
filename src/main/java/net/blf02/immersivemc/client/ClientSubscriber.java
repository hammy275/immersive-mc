package net.blf02.immersivemc.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ClientSubscriber {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (Minecraft.getInstance().gameMode == null) return;

        PlayerEntity player = event.player;

        // Get block that we're looking at
        RayTraceResult lookingAtRay = player.pick(Minecraft.getInstance().gameMode.getPickRange(), 1, false);

        BlockPos pos = new BlockPos(lookingAtRay.getLocation());
        BlockState state = player.level.getBlockState(pos);
        TileEntity tileEntity = player.level.getBlockEntity(pos);

        if (tileEntity instanceof AbstractFurnaceTileEntity) {
            AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) tileEntity;

        }
    }

}
