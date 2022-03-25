package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.ClientUtil;
import net.blf02.immersivemc.client.immersive.ImmersiveBrewing;
import net.blf02.immersivemc.client.immersive.ImmersiveCrafting;
import net.blf02.immersivemc.client.immersive.ImmersiveFurnace;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.DoCraftPacket;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

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
            ImmersiveFurnace.getSingleton().trackObject(furnace);
        } else if (tileEntity instanceof BrewingStandTileEntity) {
            BrewingStandTileEntity stand = (BrewingStandTileEntity) tileEntity;
            ImmersiveBrewing.getSingleton().trackObject(stand);
        } else if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            ImmersiveCrafting.singleton.trackObject(pos);
        }
    }

    @SubscribeEvent
    public void onClick(InputEvent.ClickInputEvent event) {
        if (event.getHand() == Hand.MAIN_HAND && event.isUseItem()) {
            if (handleRightClick(Minecraft.getInstance().player)) {
                event.setCanceled(true);
                ClientUtil.setRightClickCooldown();
            }
        } else if (event.getHand() == Hand.MAIN_HAND && event.isAttack()) {
            if (handleLeftClick(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        }
    }

    public static boolean handleLeftClick(PlayerEntity player) {
        if (Minecraft.getInstance().gameMode == null) return false;

        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            Network.INSTANCE.sendToServer(new DoCraftPacket(
                    ClientStorage.craftingStorage, pos
            ));
            return true;
        }

        return false;
    }

    public static boolean handleRightClick(PlayerEntity player) {
        double dist;
        try {
            dist = Minecraft.getInstance().gameMode.getPickRange();
        } catch (NullPointerException e) {
            return false;
        }
        Vector3d start = player.getEyePosition(1);
        Vector3d viewVec = player.getViewVector(1);
        Vector3d end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);


        for (ImmersiveFurnaceInfo info : ImmersiveFurnace.getSingleton().getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                if (closest.isPresent()) {
                    Network.INSTANCE.sendToServer(new SwapPacket(
                            info.getTileEntity().getBlockPos(), closest.get(), Hand.MAIN_HAND
                    ));
                    return true;
                }
            }
        }

        for (BrewingInfo info : ImmersiveBrewing.getSingleton().getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                if (closest.isPresent()) {
                    Network.INSTANCE.sendToServer(new SwapPacket(
                            info.getTileEntity().getBlockPos(), closest.get(), Hand.MAIN_HAND
                    ));
                    return true;
                }
            }
        }

        for (CraftingInfo info : ImmersiveCrafting.singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                closest.ifPresent(integer -> ClientSwap.craftingSwap(integer, Hand.MAIN_HAND));
                return true;
            }
        }
        return false;
    }


}
