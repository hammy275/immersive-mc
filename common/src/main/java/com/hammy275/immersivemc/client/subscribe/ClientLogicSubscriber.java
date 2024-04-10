package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive.*;
import com.hammy275.immersivemc.client.immersive.info.*;
import com.hammy275.immersivemc.client.immersive_item.AbstractItemImmersive;
import com.hammy275.immersivemc.client.immersive_item.ItemImmersives;
import com.hammy275.immersivemc.client.tracker.ClientTrackerInit;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClientLogicSubscriber {

    public static boolean backpackPressed = false;
    private static boolean alreadyInServer = false;
    private static boolean lastVRState = VRPluginVerify.clientInVR();

    public static void onClientLogin(ClientLevel level) {
        if (!alreadyInServer) { // Only run if we're actually joining a new level, rather than changing dimensions
            ActiveConfig.loadDisabled(); // Load "disabled" config, so stuff is disabled if the server isn't running ImmersiveMC
            alreadyInServer = true;
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (Minecraft.getInstance().level == null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // Clear all immersives if switching out of VR and we disable ImmersiveMC outside of VR
        boolean currentVRState = VRPluginVerify.clientInVR();
        if (currentVRState != lastVRState) {
            lastVRState = currentVRState;
            if (!currentVRState && ActiveConfig.FILE.disableOutsideVR) {
                for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
                    immersive.clearImmersives();
                }
            }
        }

        if (ImmersiveMC.OPEN_SETTINGS.isDown() && Minecraft.getInstance().screen == null) {
            Minecraft.getInstance().setScreen(new ConfigScreen(null));
        }

        // Stop ticking operations if not in VR and we don't want to use ImmersiveMC outside VR
        if (!currentVRState && ActiveConfig.FILE.disableOutsideVR) return;

        if (ClientUtil.immersiveLeftClickCooldown > 0) {
            ClientUtil.immersiveLeftClickCooldown--;
        } else if (Minecraft.getInstance().options.keyAttack.isDown()) {
            if (handleLeftClick(Minecraft.getInstance().player)) {
                ClientUtil.immersiveLeftClickCooldown += 6;
            }
        }



        if (ImmersiveMC.SUMMON_BACKPACK.isDown()) {
            if (!backpackPressed) {
                backpackPressed = true;
                ClientUtil.openBag(player);
            }
        } else {
            backpackPressed = false;
        }

        Immersives.immersiveHitboxes.initImmersiveIfNeeded();

        for (AbstractTracker tracker : ClientTrackerInit.trackers) {
            tracker.doTick(player);
        }
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            tickInfos(singleton, player);
        }
        if (VRPluginVerify.clientInVR()) {
            for (AbstractItemImmersive<?> singleton : ItemImmersives.ITEM_IMMERSIVES) {
                singleton.registerAndTickAll(
                        Minecraft.getInstance().player.getMainHandItem(),
                        Minecraft.getInstance().player.getOffhandItem()
                );
            }
        }
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;

        if (VRPluginVerify.hasAPI) {
            ClientVRSubscriber.immersiveTickVR(player);
        }

        // Always run LastVRDataTracker tick at the end
        if (VRPluginVerify.clientInVR()) {
            ClientTrackerInit.lastVRDataTracker.doTick(Minecraft.getInstance().player);
        }

        // Get block that we're looking at
        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) looking).getBlockPos();
        BlockState state = player.level().getBlockState(pos);
        BlockEntity tileEntity = player.level().getBlockEntity(pos);

        possiblyTrack(pos, state, tileEntity, Minecraft.getInstance().level);

    }

    public static void possiblyTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> immersive : Immersives.IMMERSIVES) {
            if (immersive.shouldTrack(pos, level) && immersive.clientAuthoritative()) {
                immersive.refreshOrTrackObject(pos, level);
            }
        }
    }

    public static boolean onClick(int button) {
        // Don't run code if we're on spectator mode
        if ((Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator()) ||
                (!VRPluginVerify.clientInVR() && ActiveConfig.FILE.disableOutsideVR)) return false;
        if (button == 1) {
            int cooldown = handleRightClick(Minecraft.getInstance().player);
            if (cooldown > 0) {
                ClientUtil.setRightClickCooldown(cooldown);
                return true;
            }

            // Check for cancelling right click if interacting with immersive-enabled block
            HitResult looking = Minecraft.getInstance().hitResult;
            if (looking != null && looking.getType() == HitResult.Type.BLOCK && ActiveConfig.active().disableVanillaGUIs) {
                BlockPos pos = ((BlockHitResult) looking).getBlockPos();
                for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                    // Don't bother checking this immersive if not in VR and immersive is VR only. Never skip those!
                    if (singleton.isVROnly() && !VRPluginVerify.clientInVR()) {
                        continue;
                    }
                    for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(pos)) {
                            // This is our looked at block, and we have an info for it!
                            if (singleton.enabledInConfig() && singleton.shouldBlockClickIfEnabled(info)) {
                                // Cancel right click. We can use this immersive, it's enabled, and
                                // the immersive wants us to block it (jukebox may not want to so it can eject disc,
                                // for example).
                                return true;
                            }
                        }
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

    public static void onDisconnect(Player player) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            singleton.getTrackedObjects().clear();
        }
        ActiveConfig.FROM_SERVER = (ActiveConfig) ActiveConfig.DISABLED.clone();
        alreadyInServer = false;
        // Cleared so leaving and re-joining a singleplayer world doesn't keep the lid open
        ChestToOpenSet.clear();

    }

    protected static <I extends AbstractImmersiveInfo> void tickInfos(AbstractImmersive<I> singleton, Player player) {
        // Don't tick if VR only and not in VR
        if (singleton.isVROnly() && !VRPluginVerify.clientInVR()) {
            return;
        }
        singleton.globalTick();
        if (singleton.getTrackedObjects().size() == 0) {
            singleton.noInfosTick(); // Run onNoInfos() function if we don't have any infos right now
        } else {
            List<I> infos = singleton.getTrackedObjects();
            List<I> toRemove = new LinkedList<>();
            boolean hasTooManyImmersives = infos.size() > singleton.maxImmersives &&
                    singleton.maxImmersives > -1; // Can't have too many immersives if we want a negative amount!
            int minIndex = -1;
            int minTicksLeft = Integer.MAX_VALUE;
            int i = 0;

            for (I info : infos) {
                // Make sure we can safely use this immersion before ticking it.
                if (singleton.shouldTrack(info.getBlockPosition(), Minecraft.getInstance().level)
                    || singleton.forceTickEvenIfNoTrack) {
                    singleton.tick(info, VRPluginVerify.clientInVR());
                } else {
                    info.remove();
                }
                if (info.hasHitboxes()) {
                    if (VRPluginVerify.clientInVR()) {
                        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player);
                        info.slotHovered = Util.getFirstIntersect(vrPlayer.getController0().position(),
                                info.getAllHitboxes()).orElse(-1);
                        info.slotHovered2 = Util.getFirstIntersect(vrPlayer.getController1().position(),
                                info.getAllHitboxes()).orElse(-1);
                        if (!(singleton instanceof BuiltImmersive)) {
                            if (info instanceof InfoTriggerHitboxes tInfo) {
                                info.triggerHitboxSlotHovered = Util.getFirstIntersect(vrPlayer.getController(tInfo.getVRControllerNum()).position(),
                                        tInfo.getTriggerHitboxes()).orElse(-1);
                            } else {
                                info.triggerHitboxSlotHovered = -1;
                            }
                        }
                    } else {
                        Tuple<Vec3, Vec3> startAndEnd = ClientUtil.getStartAndEndOfLookTrace(player);
                        info.slotHovered = Util.rayTraceClosest(startAndEnd.getA(), startAndEnd.getB(),
                                info.getAllHitboxes()).orElse(-1);
                        info.slotHovered2 = -1;
                        if (!(singleton instanceof BuiltImmersive)) {
                            if (info.slotHovered == -1 && info instanceof InfoTriggerHitboxes tInfo) {
                                info.triggerHitboxSlotHovered = Util.rayTraceClosest(startAndEnd.getA(), startAndEnd.getB(),
                                        tInfo.getTriggerHitboxes()).orElse(-1);
                            } else {
                                info.triggerHitboxSlotHovered = -1;
                            }
                        }
                    }
                }
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
                singleton.onRemove(info);
                infos.remove(info);
            }
        }
    }

    public static boolean handleLeftClick(Player player) {
        if (Minecraft.getInstance().player == null || (!VRPluginVerify.clientInVR() && ActiveConfig.FILE.disableOutsideVR)) return false;

        boolean inVR = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR() && VRPlugin.API.apiActive(player);
        if (inVR) {
            for (AbstractItemImmersive<?> immersive : ItemImmersives.ITEM_IMMERSIVES) {
                boolean handledClick = immersive.attemptLeftClickAll();
                if (handledClick) {
                    return true;
                }
            }
        }

        BackpackInfo backpackInfo = Immersives.immersiveBackpack.getTrackedObjects().size() > 0 ?
                Immersives.immersiveBackpack.getTrackedObjects().get(0) : null;
        // Move to next row on left click if backpack is out
        if (backpackInfo != null && backpackInfo.slotHovered > -1) {
            ImmersiveBackpack.onHitboxInteract(player, backpackInfo, backpackInfo.slotHovered);
            return true;
        }

        if (inVR) {
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (!(info instanceof InfoTriggerHitboxes)) break;
                    InfoTriggerHitboxes triggerInfo = (InfoTriggerHitboxes) info;
                    IVRData data = VRPlugin.API.getVRPlayer(player).getController(triggerInfo.getVRControllerNum());
                    Optional<Integer> triggerHit = Util.getFirstIntersect(data.position(), triggerInfo.getTriggerHitboxes());
                    if (triggerHit.isPresent()) {
                        singleton.onAnyRightClick(info);
                        singleton.handleTriggerHitboxRightClick(triggerInfo, player, triggerHit.get());
                        return true;
                    }
                }
            }
        }

        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking != null && looking.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) looking).getBlockPos();
            BlockState state = player.level().getBlockState(pos);
            BlockEntity tileEnt = player.level().getBlockEntity(pos);

            if (tileEnt instanceof ChestBlockEntity || tileEnt instanceof EnderChestBlockEntity) {
                ChestInfo chestInfo = ImmersiveChest.findImmersive(tileEnt);
                if (chestInfo != null && chestInfo.isOpen) {
                    chestInfo.nextRow();
                    return true;
                }
            } else if (ImmersiveHandlers.shulkerBoxHandler.isValidBlock(pos, player.level())) {
                BuiltImmersiveInfo info = Immersives.immersiveShulker.findImmersive(pos);
                if (info != null) {
                    ChestLikeData data = (ChestLikeData) info.getExtraData();
                    if (data.isOpen) {
                        data.nextRow();
                        return true;
                    }

                }
            } else if (ImmersiveHandlers.barrelHandler.isValidBlock(pos, player.level())) {
                BuiltImmersiveInfo info = Immersives.immersiveBarrel.findImmersive(pos);
                if (info != null) {
                    ChestLikeData data = (ChestLikeData) info.getExtraData();
                    if (data.isOpen) {
                        data.nextRow();
                        return true;
                    }
                }
            }
        } else if (backpackInfo != null) {
            backpackInfo.gotoNextRow();
            return true;
        }

        // Just before returning false, see if we're in a hitbox, so we can do a full stack place and return true
        for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
            for (AbstractImmersiveInfo info : immersive.getTrackedObjects()) {
                if (info.slotHovered != -1 || info.slotHovered2 != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int handleRightClick(Player player) {
        if (Minecraft.getInstance().gameMode == null || (!VRPluginVerify.clientInVR() && ActiveConfig.FILE.disableOutsideVR)) return 0;
        boolean inVR = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR() && VRPlugin.API.apiActive(player);
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start = player.getEyePosition(1);
        Vec3 viewVec = player.getViewVector(1);
        Vec3 end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);

        HitResult looking = Minecraft.getInstance().hitResult;
        if (ActiveConfig.active().crouchBypassImmersion &&
                looking != null && looking.getType() == HitResult.Type.BLOCK &&
                Minecraft.getInstance().player.isSecondaryUseActive()) {
            if (Util.isHittingImmersive((BlockHitResult) looking, Minecraft.getInstance().level)) {
                return -1;
            }

        }

        if (!inVR || ActiveConfig.active().rightClickInVR) { // Don't handle right clicks for VR players, they have hands (unless they config to!)!
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                if (singleton.isVROnly() && !inVR) continue;
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (info.hasHitboxes() && singleton.hitboxesAvailable(info)) {
                        Optional<Integer> closest = Util.rayTraceClosest(start, end, info.getAllHitboxes());
                        if (closest.isPresent()) {
                            singleton.onAnyRightClick(info);
                            singleton.handleRightClick(info, player, closest.get(), InteractionHand.MAIN_HAND);
                            return singleton.getCooldownDesktop();
                        } else if (info instanceof InfoTriggerHitboxes) {
                            InfoTriggerHitboxes triggerInfo = (InfoTriggerHitboxes) info;
                            Optional<Integer> closestTrigger = Util.rayTraceClosest(start, end, triggerInfo.getTriggerHitboxes());
                            if (closestTrigger.isPresent()) {
                                singleton.onAnyRightClick(info);
                                singleton.handleTriggerHitboxRightClick(triggerInfo, player, closestTrigger.get());
                                return singleton.getCooldownDesktop();
                            }
                        }
                    }
                }
            }
        }

        // If we handle things in the block ray tracing part of right click, we return true
        int rayTraceCooldown = handleRightClickBlockRayTrace(player);
        if (rayTraceCooldown > 0) {
            return rayTraceCooldown;
        }
        return 0;
    }

    protected static int handleRightClickBlockRayTrace(Player player) {
        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != HitResult.Type.BLOCK) return 0;

        BlockPos pos = ((BlockHitResult) looking).getBlockPos();
        BlockState state = player.level().getBlockState(pos);
        if (ActiveConfig.active().rightClickChest && ActiveConfig.active().useChestImmersion) {
            boolean isChest = state.getBlock() instanceof AbstractChestBlock && player.level().getBlockEntity(pos) instanceof ChestBlockEntity;
            boolean isEnderChest = state.getBlock() instanceof EnderChestBlock && player.level().getBlockEntity(pos) instanceof EnderChestBlockEntity;
            if (isChest || isEnderChest) {
                ChestInfo info = ImmersiveChest.findImmersive(player.level().getBlockEntity(pos));
                if (info != null) {
                    ImmersiveChest.openChest(info);
                    return Immersives.immersiveChest.getCooldownDesktop();
                }
            }
        }
        if (ActiveConfig.active().useBarrelImmersion &&
                ImmersiveHandlers.barrelHandler.isValidBlock(pos, player.level())) {
            BuiltImmersiveInfo info = Immersives.immersiveBarrel.findImmersive(pos);
            if (info != null) {
                ((ChestLikeData) info.getExtraData()).toggleOpen(pos);
                return Immersives.immersiveBarrel.getCooldownDesktop();
            }
        }
        if (ActiveConfig.active().useShulkerImmersion &&
                ImmersiveHandlers.shulkerBoxHandler.isValidBlock(pos, player.level())) {
            for (BuiltImmersiveInfo info : Immersives.immersiveShulker.getTrackedObjects()) {
                if (info.getBlockPosition().equals(pos)) {
                    ((ChestLikeData) info.getExtraData()).toggleOpen(info.getBlockPosition());
                    return Immersives.immersiveShulker.getCooldownDesktop();
                }
            }
        }

        return 0; // Still here in case if we need it later
    }


}
