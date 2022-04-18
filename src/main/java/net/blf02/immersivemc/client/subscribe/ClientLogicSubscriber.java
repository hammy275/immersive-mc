package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.ClientUtil;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.ImmersiveAnvil;
import net.blf02.immersivemc.client.immersive.ImmersiveBrewing;
import net.blf02.immersivemc.client.immersive.ImmersiveChest;
import net.blf02.immersivemc.client.immersive.ImmersiveCrafting;
import net.blf02.immersivemc.client.immersive.ImmersiveFurnace;
import net.blf02.immersivemc.client.immersive.ImmersiveJukebox;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AnvilInfo;
import net.blf02.immersivemc.client.immersive.info.BrewingInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.immersive.info.ImmersiveFurnaceInfo;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.client.tracker.ClientTrackerInit;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ChestOpenPacket;
import net.blf02.immersivemc.common.network.packet.DoCraftPacket;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClientLogicSubscriber {

    @SubscribeEvent
    public void onClientLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ActiveConfig.loadOffConfig(); // Load "disabled" config, so stuff is disabled if the server isn't running ImmersiveMC
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level instanceof ServerWorld) return;
        if (ClientUtil.immersiveLeftClickCooldown > 0) {
            ClientUtil.immersiveLeftClickCooldown--;
        }
        for (AbstractTracker tracker : ClientTrackerInit.trackers) {
            tracker.doTick(event.player);
        }
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            tickInfos(singleton);
        }
        if (Minecraft.getInstance().gameMode == null) return;

        PlayerEntity player = event.player;

        // Get block that we're looking at
        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return;

        BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        TileEntity tileEntity = player.level.getBlockEntity(pos);

        possiblyTrack(pos, state, tileEntity);

    }

    public static void possiblyTrack(BlockPos pos, BlockState state, TileEntity tileEntity) {
        if (tileEntity instanceof AbstractFurnaceTileEntity) {
            AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) tileEntity;
            ImmersiveFurnace.getSingleton().trackObject(furnace);
        } else if (tileEntity instanceof BrewingStandTileEntity) {
            BrewingStandTileEntity stand = (BrewingStandTileEntity) tileEntity;
            ImmersiveBrewing.getSingleton().trackObject(stand);
        } else if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            ImmersiveCrafting.singleton.trackObject(pos);
        } else if (tileEntity instanceof JukeboxTileEntity) {
            ImmersiveJukebox.getSingleton().trackObject((JukeboxTileEntity) tileEntity);
        } else if (tileEntity instanceof ChestTileEntity) {
            ImmersiveChest.singleton.trackObject((ChestTileEntity) tileEntity);
        } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
            ImmersiveAnvil.singleton.trackObject(pos);
        }
    }

    @SubscribeEvent
    public void onClick(InputEvent.ClickInputEvent event) {
        // Don't run code if we're on spectator mode
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator()) return;
        if (event.getHand() == Hand.MAIN_HAND && event.isUseItem()) {
            if (handleRightClick(Minecraft.getInstance().player)) {
                event.setCanceled(true);
                ClientUtil.setRightClickCooldown();
            }
        } else if (event.getHand() == Hand.MAIN_HAND && event.isAttack()
            && ClientUtil.immersiveLeftClickCooldown <= 0) {
            if (handleLeftClick(Minecraft.getInstance().player)) {
                event.setCanceled(true);
                ClientUtil.immersiveLeftClickCooldown = 8;
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            singleton.getTrackedObjects().clear();
        }

    }

    protected <I extends AbstractImmersiveInfo> void tickInfos(AbstractImmersive<I> singleton) {
        List<I> infos = singleton.getTrackedObjects();
        List<I> toRemove = new LinkedList<>();
        boolean hasTooManyImmersives = infos.size() > singleton.maxImmersives &&
                singleton.maxImmersives > -1; // Can't have too many immersives if we want a negative amount!
        int minIndex = -1;
        int minTicksLeft = Integer.MAX_VALUE;
        int i = 0;
        for (I info : infos) {
            singleton.tick(info, VRPluginVerify.clientInVR);
            if (info.getTicksLeft() <= 0) {
                toRemove.add(info);
            }
            if (hasTooManyImmersives) {
                if (info.getTicksLeft() < minTicksLeft) {
                    minTicksLeft = info.getTicksLeft();
                    minIndex = i;
                }
            }
            i++;
        }
        if (minIndex > -1) {
            I toRem = infos.get(minIndex);
            if (!toRemove.contains(toRem)) {
                toRemove.add(toRem);
            }
        }

        for (I info : toRemove) {
            infos.remove(info);
        }
    }

    public static boolean handleLeftClick(PlayerEntity player) {
        if (Minecraft.getInstance().player == null) return false;

        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            Network.INSTANCE.sendToServer(new DoCraftPacket(
                    ClientStorage.craftingStorage, pos
            ));
            ClientStorage.removeLackingIngredientsFromTable(player);
            return true;
        }

        TileEntity tileEnt = player.level.getBlockEntity(pos);
        if (tileEnt instanceof ChestTileEntity) {
            ChestTileEntity chest = (ChestTileEntity) tileEnt;
            ChestInfo info = ImmersiveChest.findImmersive(chest);
            if (info != null) {
                info.nextRow();
            }
            return true;
        }

        return false;
    }

    public static boolean handleRightClick(PlayerEntity player) {
        if (Minecraft.getInstance().gameMode == null) return false;
        double dist = Minecraft.getInstance().gameMode.getPickRange();
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
                if (closest.isPresent()) {
                    ClientSwap.craftingSwap(closest.get(), Hand.MAIN_HAND);
                    return true;
                }
            }
        }

        for (ChestInfo info : ImmersiveChest.singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                if (closest.isPresent()) {
                    Network.INSTANCE.sendToServer(new SwapPacket(
                            info.getBlockPosition(), closest.get(), Hand.MAIN_HAND
                    ));
                    return true;
                }
            }
        }

        for (AnvilInfo info : ImmersiveAnvil.singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                if (closest.isPresent()) {
                    ClientSwap.anvilSwap(closest.get(), Hand.MAIN_HAND, info.anvilPos);
                    return true;
                }
            }
        }

        // If we handle things in the block ray tracing part of right click, we return true
        if (handleRightClickBlockRayTrace(player)) {
            return true;
        }
        // Don't handle jukeboxes since those are VR only
        return false;
    }

    protected static boolean handleRightClickBlockRayTrace(PlayerEntity player) {
        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return false;

        if (VRPluginVerify.clientInVR || ClientConstants.vrInteractionsOutsideVR) {
            BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
            BlockState state = player.level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractChestBlock && player.level.getBlockEntity(pos) instanceof ChestTileEntity
                    && !player.isCrouching()) { // Crouch to still open chest
                ChestInfo info = ImmersiveChest.findImmersive((ChestTileEntity) player.level.getBlockEntity(pos));
                if (info != null) {
                    info.isOpen = !info.isOpen;
                    Network.INSTANCE.sendToServer(new ChestOpenPacket(info.getBlockPosition(), info.isOpen));
                    if (!info.isOpen) {
                        info.remove(); // Remove immersive if we're closing the chest
                    }
                    return true;
                }
            }
        }
        return false;
    }


}
