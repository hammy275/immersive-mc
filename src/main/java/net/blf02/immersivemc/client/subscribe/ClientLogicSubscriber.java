package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.client.ClientUtil;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.*;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.BackpackInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.blf02.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import net.blf02.immersivemc.client.tracker.ClientTrackerInit;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.ChestOpenPacket;
import net.blf02.immersivemc.common.network.packet.InventorySwapPacket;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.server.swap.Swap;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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

    public boolean backpackPressed = false;

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

        if (ImmersiveMC.SUMMON_BACKPACK.isDown()) {
            if (!backpackPressed) {
                backpackPressed = true;
                BackpackImmersive.singleton.doTrack();
            }
        } else {
            backpackPressed = false;
        }

        for (AbstractTracker tracker : ClientTrackerInit.trackers) {
            tracker.doTick(event.player);
        }
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            tickInfos(singleton);
        }
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;

        PlayerEntity player = event.player;

        // Get block that we're looking at
        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != RayTraceResult.Type.BLOCK) return;

        BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        TileEntity tileEntity = player.level.getBlockEntity(pos);

        possiblyTrack(pos, state, tileEntity, Minecraft.getInstance().level);

    }

    public static void possiblyTrack(BlockPos pos, BlockState state, TileEntity tileEntity, World level) {
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
        } else if (tileEntity instanceof ChestTileEntity || tileEntity instanceof EnderChestTileEntity) {
            ImmersiveChest.singleton.trackObject(tileEntity);
        } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
            ImmersiveAnvil.singleton.trackObject(pos);
        } else if (state.getBlock() instanceof EnchantingTableBlock) {
            ImmersiveETable.singleton.trackObject(pos);
            return;
        }

        // Extra special tracker additions
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.getBlock() instanceof EnchantingTableBlock) {
            ImmersiveETable.singleton.trackObject(pos);
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
        } else if (event.getHand() == Hand.MAIN_HAND && event.isAttack()) {
            if (ClientUtil.immersiveLeftClickCooldown > 0) {
                event.setCanceled(true);
                return; // Cancel left click if we're on cooldown
            }
            if (handleLeftClick(Minecraft.getInstance().player)) {
                event.setCanceled(true);
                ClientUtil.immersiveLeftClickCooldown += 6;
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

        BackpackInfo backpackInfo = BackpackImmersive.singleton.getTrackedObjects().size() > 0 ?
                BackpackImmersive.singleton.getTrackedObjects().get(0) : null;
        // Move to next row on left click if backpack is out
        if (backpackInfo != null && backpackInfo.slotHovered > -1) {
            Network.INSTANCE.sendToServer(new InventorySwapPacket(backpackInfo.slotHovered + 9));
            Swap.handleInventorySwap(player, backpackInfo.slotHovered + 9, Hand.MAIN_HAND); // Do swap on both sides
            return true;
        }

        boolean inVR = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR && VRPlugin.API.apiActive(player);
        if (inVR) {
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (!(info instanceof InfoTriggerHitboxes)) break;
                    IVRData data = VRPlugin.API.getVRPlayer(player).getController0();
                    InfoTriggerHitboxes triggerInfo = (InfoTriggerHitboxes) info;
                    Optional<Integer> triggerHit = Util.getFirstIntersect(data.position(), triggerInfo.getTriggerHitboxes());
                    if (triggerHit.isPresent()) {
                        singleton.handleTriggerHitboxRightClick(triggerInfo, player, triggerHit.get());
                        return true;
                    }
                }
            }
        }

        RayTraceResult looking = Minecraft.getInstance().hitResult;
        if (looking != null && looking.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) looking).getBlockPos();
            BlockState state = player.level.getBlockState(pos);
            TileEntity tileEnt = player.level.getBlockEntity(pos);

            if (tileEnt instanceof ChestTileEntity || tileEnt instanceof EnderChestTileEntity) {
                ChestInfo chestInfo = ImmersiveChest.findImmersive(tileEnt);
                if (chestInfo != null && chestInfo.isOpen) {
                    chestInfo.nextRow();
                    return true;
                }
            }
        } else if (backpackInfo != null) {
            backpackInfo.gotoNextRow();
            return true;
        }

        return false;
    }

    public static boolean handleRightClick(PlayerEntity player) {
        if (Minecraft.getInstance().gameMode == null) return false;
        boolean inVR = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR && VRPlugin.API.apiActive(player);
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vector3d start = player.getEyePosition(1);
        Vector3d viewVec = player.getViewVector(1);
        Vector3d end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);

        if (!inVR) { // Don't handle right clicks for VR players, they have hands!
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (info.hasHitboxes()) {
                        Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                        if (closest.isPresent()) {
                            singleton.handleRightClick(info, player, closest.get(), Hand.MAIN_HAND);
                            return true;
                        } else if (info instanceof InfoTriggerHitboxes) {
                            InfoTriggerHitboxes triggerInfo = (InfoTriggerHitboxes) info;
                            Optional<Integer> closestTrigger = Util.rayTraceClosest(start, end, triggerInfo.getTriggerHitboxes());
                            if (closestTrigger.isPresent()) {
                                singleton.handleTriggerHitboxRightClick(triggerInfo, player, closestTrigger.get());
                                return true;
                            }
                        }
                    }
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
            boolean isChest = state.getBlock() instanceof AbstractChestBlock && player.level.getBlockEntity(pos) instanceof ChestTileEntity;
            boolean isEnderChest = state.getBlock() instanceof EnderChestBlock && player.level.getBlockEntity(pos) instanceof EnderChestTileEntity;
            if ((isChest || isEnderChest) && !player.isCrouching()) { // Crouch to still open chest
                ChestInfo info = ImmersiveChest.findImmersive(player.level.getBlockEntity(pos));
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
