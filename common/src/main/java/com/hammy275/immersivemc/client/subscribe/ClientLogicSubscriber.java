package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.immersive.*;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive.ImmersiveChest;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import com.hammy275.immersivemc.client.immersive.info.BagInfo;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.client.immersive.info.ChestLikeData;
import com.hammy275.immersivemc.client.immersive_item.AbstractHandImmersive;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.client.interact_module.ChestLidInteractModule;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.ticker.TickerInit;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.common.vr.dev.DevVRState;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.hammy275.immersivemc.server.api_impl.SharedNetworkStoragesImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRPose;

import java.util.*;

public class ClientLogicSubscriber {

    public static boolean backpackPressed = false;
    private static boolean alreadyInServer = false;
    private static boolean lastVRState = VRVerify.clientInVR();
    private static boolean didLowVivecraftVersionCheck = false;

    public static void onClientLogin(Minecraft minecraft) {
        if (!alreadyInServer) { // Only run if we're actually joining a new level, rather than changing dimensions
            ActiveConfig.loadDisabled(); // Load "disabled" config, so stuff is disabled if the server isn't running ImmersiveMC
            alreadyInServer = true;
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (Minecraft.getInstance().level == null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        minecraft.getProfiler().push(ImmersiveMC.MOD_ID);

        // Tell the user if they're running a Vivecraft version too low
        if (!didLowVivecraftVersionCheck) {
            didLowVivecraftVersionCheck = true;
            if (Util.hasTooLowVivecraftVersion()) {
                player.displayClientMessage(new TranslatableComponent("message.immersivemc.vivecraft_low_version"), false);
            }
        }

        if (!VRVerify.clientInVR()) {
            SwapTracker.c0.maybeIdleTick();
        }

        // Clear all immersives if switching out of VR and we disable ImmersiveMC outside of VR
        boolean currentVRState = VRVerify.clientInVR();
        if (currentVRState != lastVRState) {
            lastVRState = currentVRState;
            if (!currentVRState && ActiveConfig.FILE_CLIENT.disableImmersiveMCOutsideVR) {
                for (Immersive<?, ?, ?> immersive : Immersives.ALL_IMMERSIVES) {
                    immersive.getTrackedObjects().clear();
                }
            }
        }

        if (ImmersiveMC.OPEN_SETTINGS.isDown() && Minecraft.getInstance().screen == null) {
            Minecraft.getInstance().setScreen(new ConfigScreen(null));
        }

        // Stop ticking operations if not in VR and we don't want to use ImmersiveMC outside VR
        if (!currentVRState && ActiveConfig.FILE_CLIENT.disableImmersiveMCOutsideVR) return;

        if (ClientUtil.immersiveLeftClickCooldown > 0) {
            ClientUtil.immersiveLeftClickCooldown--;
        }
        // Separate check on the cooldown after the above so it doesn't go below 0 and so once it hits 0, we still
        // run ImmersiveMC logic.
        if (Minecraft.getInstance().options.keyAttack.isDown() && ClientUtil.immersiveLeftClickCooldown == 0) {
            if (handleLeftClick(Minecraft.getInstance().player)) {
                ClientUtil.immersiveLeftClickCooldown += 6;
            }
        }



        if (ImmersiveMC.SUMMON_BACKPACK.isDown()) {
            if (!backpackPressed) {
                backpackPressed = true;
                ClientUtil.openBag(player, false);
            }
        } else {
            backpackPressed = false;
        }

        Immersives.immersiveHitboxes.initImmersiveIfNeeded();

        TickerInit.tickClient(player);

        for (Immersive<?, ?, ?> singleton : Immersives.ALL_IMMERSIVES) {
            tickInfos(singleton, player);
        }
        if (VRVerify.clientInVR()) {
            for (AbstractHandImmersive<?> singleton : HandImmersives.HAND_IMMERSIVES) {
                singleton.registerAndTickAll();
            }
        }
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;

        if (VRVerify.hasAPI) {
            ClientVRSubscriber.immersiveTickVR(player);
        }

        ChestLidInteractModule.INSTANCE.removeInvalid();

        if (CommonConstants.devFakeVRMode) {
            DevVRState.clientTick();
        }

        // Get block that we're looking at
        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        BlockEntity tileEntity = player.level.getBlockEntity(pos);

        possiblyTrack(pos, state, tileEntity, Minecraft.getInstance().level);

        // Pop profiler push from above. Not using a popPush() so we're part of tick in the profiler.
        minecraft.getProfiler().pop();
    }

    public static void possiblyTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        // No similar loop for AbstractPlayerAttachmentImmersive since those don't run from blocks
        for (BlockBasedImmersive<?, ?, ?> immersive : Immersives.BLOCK_IMMERSIVES) {
            if (immersive.getHandler().clientAuthoritative() &&
                    immersive.getHandler().enabledInConfig(Minecraft.getInstance().player) &&
                    Util.isValidBlocks(immersive.getHandler(), pos, level)) {
                doTrackIfNotTrackingAlready(immersive, pos, level);
            }
        }
    }

    public static <I extends BlockBasedImmersiveInfo> I doTrackIfNotTrackingAlready(BlockBasedImmersive<I, ?, ?> immersive, BlockPos pos, Level level) {
        I info = ClientUtil.findImmersive(immersive, pos);
        if (info != null) {
            return info;
        }
        info = immersive.buildInfo(pos, level);
        immersive.getTrackedObjects().add(info);
        return info;
    }

    public static boolean onClick(int button) {
        // Don't run code if we're on spectator mode
        if ((Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator()) ||
                (!VRVerify.clientInVR() && ActiveConfig.FILE_CLIENT.disableImmersiveMCOutsideVR)) return false;
        if (button == 1) {
            int cooldown = handleRightClick(Minecraft.getInstance().player);
            if (cooldown >= 0) {
                ImmersiveClientLogicHelpers.instance().setCooldown(cooldown);
                return true;
            }

            // Check for cancelling right click if interacting with immersive-enabled block
            HitResult looking = Minecraft.getInstance().hitResult;
            if (looking != null && looking.getType() == HitResult.Type.BLOCK && ActiveConfig.active().disableVanillaInteractionsForSupportedImmersives) {
                BlockPos pos = ((BlockHitResult) looking).getBlockPos();
                // No similar check for AbstractPlayerAttachmentImmersive, since those aren't tied to blocks
                for (BlockBasedImmersive<? extends BlockBasedImmersiveInfo, ?, ?> singleton : Immersives.BLOCK_IMMERSIVES) {
                    // Don't bother checking this immersive if not in VR and immersive is VR only. Never skip those!
                    if (singleton.isVROnly() && !VRVerify.clientInVR()) {
                        continue;
                    }
                    if (skipRightClick(singleton, pos)) {
                        return true;
                    }
                }
            }


        } else if (button == 0 &&
                (ClientUtil.immersiveLeftClickCooldown > 0)) {
            return true;
        } else if (button == 0 && ClientUtil.immersiveLeftClickCooldown <= 0 && handleLeftClick(Minecraft.getInstance().player)) {
            ClientUtil.immersiveLeftClickCooldown += 6;
            return true;
        }
        return false;
    }

    private static <I extends BlockBasedImmersiveInfo> boolean skipRightClick(BlockBasedImmersive<I, ?, ?> immersive, BlockPos clickPos) {
        I info = ClientUtil.findImmersive(immersive, clickPos);
        // Cancel right click. We can use this immersive, it's enabled, and
        // the immersive wants us to block it (jukebox may not want to so it can eject disc,
        // for example).
        return info != null && immersive.shouldDisableRightClicksWhenVanillaInteractionsDisabled(info);
    }

    public static void onDisconnect(Player player) {
        // LAN hosts call this whenever any player disconnects, so make sure the host is the one leaving (#448).
        // Check if it's either null or the player leaving is us. Have to do a check against UUID though, since the
        // incoming player is a ServerPlayer, while Minecraft.getInstance().player is a local player (of course).
        if (Minecraft.getInstance().player == null ||
                Minecraft.getInstance().player.getGameProfile().getId().equals(player.getGameProfile().getId())) {
            for (Immersive<?, ?, ?> singleton : Immersives.ALL_IMMERSIVES) {
                singleton.getTrackedObjects().clear();
            }
            ActiveConfig.FROM_SERVER = (ClientActiveConfig) ClientActiveConfig.DISABLED.clone();
            alreadyInServer = false;
            // Cleared so leaving and re-joining a singleplayer world doesn't keep the lid open
            ChestToOpenSet.clear();
            SharedNetworkStoragesImpl.INSTANCE.clear(); // Clear so these don't persist between singleplayer worlds
            TickerInit.onPlayerDisconnectClient(player);
        }
    }

    protected static <I extends ImmersiveInfo> void tickInfos(Immersive<I, ?, ?> singleton, Player player) {
        if (singleton instanceof BlockBasedImmersive<?, ?, ?> blockBased) {
            // Don't tick if VR only and not in VR
            if (blockBased.isVROnly() && !VRVerify.clientInVR()) {
                return;
            }
            // For block-based ones, attempt to remove if the blocks no longer match or if too far away
            blockBased.getTrackedObjects().removeIf((info) -> {
                Set<BlockPos> positions = Util.getValidBlocks(blockBased.getHandler(), info.getBlockPosition(), Minecraft.getInstance().level);
                return positions.isEmpty() || player.distanceToSqr(Util.average(positions)) > CommonConstants.distanceSquaredToRemoveImmersive;
            });
        } else if (singleton instanceof PlayerAttachmentImmersive<?, ?, ?> attachment) {
            // For player attachment ones, attempt to remove if simply too far away, if the level no longer matches,
            // or if the owner is no longer in VR (which also covers disconnecting).
            attachment.getTrackedObjects().removeIf(info ->
                    !VRVerify.playerInVR(info.getOwner()) ||
                    player.distanceToSqr(info.getOwner().position()) > CommonConstants.distanceSquaredToRemoveAttachmentImmersive
                            || info.getOwner().level != player.level);
        }

        singleton.globalTick();
        Collection<I> infos = singleton.getTrackedObjects();

        try {
            for (I info : infos) {
                singleton.tick(info);
                if (info.hasHitboxes()) {
                    if (VRVerify.clientInVR()) {
                        VRPose vrPose = VR.API.getVRPose(player);
                        for (InteractionHand hand : InteractionHand.values()) {
                            info.setSlotHovered(Util.getFirstIntersect(vrPose.getHand(hand).getPos(),
                                    info.getAllHitboxes().stream().map((box) -> box != null ? box.getHitbox() : null).toList()).orElse(-1), hand.ordinal());
                        }
                    }
                    if (!VRVerify.clientInVR() || ActiveConfig.active().rightClickImmersiveInteractionsInVR) {
                        Tuple<Vec3, Vec3> startAndEnd = ClientUtil.getStartAndEndOfLookTrace(player);
                        info.setSlotHovered(Util.rayTraceClosest(startAndEnd.getA(), startAndEnd.getB(), info.getAllHitboxes()).orElse(-1), 0);
                        info.setSlotHovered(-1, 1);
                    }
                }
            }
        // Happens when clearing Immersives due to a mod compatibility issue on the client. Only skips this singleton
        // (which just got disabled), so nothing lasting happens because of this.
        } catch (ConcurrentModificationException ignored) {}

    }

    public static boolean handleLeftClick(Player player) {
        if (Minecraft.getInstance().player == null || (!VRVerify.clientInVR() && ActiveConfig.FILE_CLIENT.disableImmersiveMCOutsideVR)) return false;

        boolean inVR = VRVerify.playerInVR(player);
        if (inVR) {
            for (AbstractHandImmersive<?> immersive : HandImmersives.HAND_IMMERSIVES) {
                boolean handledClick = immersive.attemptLeftClickAll();
                if (handledClick) {
                    return true;
                }
            }
        }

        HitResult looking = Minecraft.getInstance().hitResult;
        BagInfo bagInfo = Immersives.immersiveBag.getLocalPlayerInfo();
        if (looking != null && looking.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) looking).getBlockPos();
            BlockState state = player.level.getBlockState(pos);
            BlockEntity tileEnt = player.level.getBlockEntity(pos);

            if (tileEnt instanceof ChestBlockEntity || tileEnt instanceof EnderChestBlockEntity) {
                ChestInfo chestInfo = ImmersiveChest.findImmersive(tileEnt);
                if (chestInfo != null && chestInfo.isOpen()) {
                    chestInfo.nextRow();
                    return true;
                }
            } else if (ImmersiveHandlers.shulkerBoxHandler.isValidBlock(pos, player.level)) {
                BuiltBlockBasedImmersiveInfo<ChestLikeData> info = ClientUtil.findImmersive(Immersives.immersiveShulker, pos);
                if (info != null) {
                    ChestLikeData data = info.getExtraData();
                    if (data.isOpen) {
                        data.nextRow();
                        return true;
                    }

                }
            } else if (ImmersiveHandlers.barrelHandler.isValidBlock(pos, player.level)) {
                BuiltBlockBasedImmersiveInfo<ChestLikeData> info = ClientUtil.findImmersive(Immersives.immersiveBarrel, pos);
                if (info != null) {
                    ChestLikeData data = info.getExtraData();
                    if (data.isOpen) {
                        data.nextRow();
                        return true;
                    }
                }
            } else if (ImmersiveHandlers.apothSalvagingTableHandler.isValidBlock(pos, player.level)) {
                BuiltBlockBasedImmersiveInfo<?> info = ClientUtil.findImmersive(Immersives.immersiveApothSalvagingTable, pos);
                if (info != null) {
                    int numHitboxes = info.getAllHitboxes().size();
                    for (int i = 0; i < numHitboxes; i++) {
                        if (!info.getItem(i).isEmpty()) {
                            ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), List.of(9), InteractionHand.MAIN_HAND, false);
                            return true;
                        }
                    }
                }
            }
        } else if (bagInfo != null && !bagInfo.hasSlotHovered()) {
            bagInfo.gotoNextRow();
            return true;
        }

        // Just before returning false, see if we're in a hitbox, so we can do a full stack place and return true
        for (Immersive<?, ?, ?> immersive : Immersives.ALL_IMMERSIVES) {
            for (ImmersiveInfo info : immersive.getTrackedObjects()) {
                if (info.getSlotHovered(0) != -1 || info.getSlotHovered(1) != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int handleRightClick(Player player) {
        if (Minecraft.getInstance().gameMode == null || (!VRVerify.clientInVR() && ActiveConfig.FILE_CLIENT.disableImmersiveMCOutsideVR)) return -1;
        if (ActiveConfig.active().crouchMode.bypassImmersive() && Minecraft.getInstance().player.isCrouching()) return -1;
        boolean inVR = VRVerify.playerInVR(player);

        Tuple<Vec3, Vec3> startAndEnd = ClientUtil.getStartAndEndOfLookTrace(Minecraft.getInstance().player);
        Vec3 start = startAndEnd.getA();
        Vec3 end = startAndEnd.getB();

        if (!inVR || ActiveConfig.active().rightClickImmersiveInteractionsInVR) { // Don't handle right clicks for VR players, they have hands (unless they config to!)!
            for (Immersive<?, ?, ?> singleton : Immersives.ALL_IMMERSIVES) {
                if (ClientUtil.isVROnly(singleton) && !inVR) continue;
                Integer fromInfos = handleRightClickInfos(singleton, start, end);
                if (fromInfos != null && fromInfos >= 0) {
                    return fromInfos;
                }
            }
            if (!inVR) {
                // This is done in ClientVRSubscriber for VR players
                SwapTracker.c0.tick(null, null, -1, false);
            }
            if (SwapTracker.c0.getCooldown() > 0) return SwapTracker.c0.getCooldown();
        }

        // Can get here from a right-click chest interaction while in VR. VR ticks the swap tracker.
        if (inVR && SwapTracker.c0.getCooldown() > 0) return SwapTracker.c0.getCooldown();
        // If we handle things in the block ray tracing part of right click, we return true
        int rayTraceCooldown = handleRightClickBlockRayTrace(player);
        if (rayTraceCooldown > 0) {
            return rayTraceCooldown;
        }
        return -1;
    }

    private static <I extends ImmersiveInfo> Integer handleRightClickInfos(Immersive<I, ?, ?> singleton, Vec3 start, Vec3 end) {
        Integer cooldownOut = null;
        I infoToSwapTick = null;
        for (I info : singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                if (closest.isPresent()) {
                    if (singleton.isInputHitbox(info, closest.get())) {
                        SwapTracker.c0.tick(singleton, info, closest.get(), true);
                        return SwapTracker.c0.getCooldown();
                    } else {
                        SwapTracker.c0.tick(null, null, -1, inDragHitbox(singleton, info, start, end));
                        if (SwapTracker.c0.getCooldown() <= 0) {
                            int res = singleton.handleHitboxInteract(info, Minecraft.getInstance().player, List.of(closest.get()), InteractionHand.MAIN_HAND, Minecraft.getInstance().options.keyAttack.isDown());
                            return res >= 0 ? res : null;
                        } else {
                            return SwapTracker.c0.getCooldown(); // Return cooldown instead of null so we don't do another SwapTracker tick
                        }
                    }
                } else if (inDragHitbox(singleton, info, start, end)) {
                    infoToSwapTick = info;
                    cooldownOut = 1;
                }
            }
        }
        if (infoToSwapTick != null) {
            SwapTracker.c0.tick(singleton, infoToSwapTick, -1, true);
        }
        return cooldownOut;
    }

    protected static int handleRightClickBlockRayTrace(Player player) {
        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != HitResult.Type.BLOCK) return -1;

        BlockPos pos = ((BlockHitResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        if (ActiveConfig.active().useChestImmersive) {
            boolean isChest = state.getBlock() instanceof AbstractChestBlock && player.level.getBlockEntity(pos) instanceof ChestBlockEntity;
            boolean isEnderChest = state.getBlock() instanceof EnderChestBlock && player.level.getBlockEntity(pos) instanceof EnderChestBlockEntity;
            if (isChest || isEnderChest) {
                ChestInfo info = ImmersiveChest.findImmersive(player.level.getBlockEntity(pos));
                if (info != null && (ActiveConfig.active().rightClickChestInteractions
                        || (!VRVerify.clientInVR() && (((BlockHitResult) looking).getDirection() == Direction.UP) || info.isOpen())
                        || ActiveConfig.active().disableVanillaInteractionsForSupportedImmersives)) {
                    ImmersiveChest.openChest(info);
                    return ImmersiveClientConstants.instance().defaultCooldown();
                }
            }
        }
        // Direction check is so we only open when right-clicking the front of the barrel
        if (ActiveConfig.active().useBarrelImmersive &&
                ImmersiveHandlers.barrelHandler.isValidBlock(pos, player.level)) {
            BuiltBlockBasedImmersiveInfo<ChestLikeData> info = ClientUtil.findImmersive(Immersives.immersiveBarrel, pos);
            if (info != null && ((((BlockHitResult) looking).getDirection() == state.getValue(BlockStateProperties.FACING))
                    || ActiveConfig.active().disableVanillaInteractionsForSupportedImmersives
                    || info.getExtraData().isOpen)) {
                info.getExtraData().toggleOpen(pos);
                return 6;
            }
        }
        if (ActiveConfig.active().useShulkerImmersive &&
                ImmersiveHandlers.shulkerBoxHandler.isValidBlock(pos, player.level)) {
            for (BuiltBlockBasedImmersiveInfo<ChestLikeData> info : Immersives.immersiveShulker.getTrackedObjects()) {
                if (info.getBlockPosition().equals(pos)) {
                    info.getExtraData().toggleOpen(info.getBlockPosition());
                    return 6;
                }
            }
        }

        return -1; // Still here in case if we need it later
    }

    private static <I extends ImmersiveInfo> boolean inDragHitbox(Immersive<I, ?, ?> singleton, I info, Vec3 rayStart, Vec3 rayEnd) {
        BoundingBox dragHitbox = singleton.getDragHitbox(info);
        return dragHitbox != null && Util.rayTraceClosest(rayStart, rayEnd, dragHitbox).isPresent();
    }

}
