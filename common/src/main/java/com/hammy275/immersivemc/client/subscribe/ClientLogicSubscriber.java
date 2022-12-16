package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.ImmersiveBackpack;
import com.hammy275.immersivemc.client.immersive.ImmersiveChest;
import com.hammy275.immersivemc.client.immersive.ImmersiveShulker;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.client.immersive.info.ShulkerInfo;
import com.hammy275.immersivemc.client.tracker.ClientTrackerInit;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
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
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClientLogicSubscriber {

    public static boolean backpackPressed = false;

    public static void onClientLogin(ClientLevel level) {
        ActiveConfig.loadOffConfig(); // Load "disabled" config, so stuff is disabled if the server isn't running ImmersiveMC
    }

    public static void onClientTick(Minecraft minecraft) {
        if (Minecraft.getInstance().level == null) return;
        Player player = Minecraft.getInstance().player;
        if (ImmersiveMC.OPEN_SETTINGS.isDown() && Minecraft.getInstance().screen == null) {
            Minecraft.getInstance().setScreen(new ConfigScreen(null));
        }

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
                Immersives.immersiveBackpack.doTrack();
            }
        } else {
            backpackPressed = false;
        }

        for (AbstractTracker tracker : ClientTrackerInit.trackers) {
            tracker.doTick(player);
        }
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            tickInfos(singleton, player);
        }
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;

        // Get block that we're looking at
        HitResult looking = Minecraft.getInstance().hitResult;
        if (looking == null || looking.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) looking).getBlockPos();
        BlockState state = player.level.getBlockState(pos);
        BlockEntity tileEntity = player.level.getBlockEntity(pos);

        possiblyTrack(pos, state, tileEntity, Minecraft.getInstance().level);

        if (VRPluginVerify.hasAPI) {
            ClientVRSubscriber.immersiveTickVR(player);
        }

    }

    public static void possiblyTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (AbstractImmersive<? extends AbstractImmersiveInfo> immersive : Immersives.IMMERSIVES) {
            if (immersive.shouldTrack(pos, state, tileEntity, level)) {
                immersive.trackObject(pos, state, tileEntity, level);
            }
        }

        // Extra special tracker additions
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockEntity belowEntity = level.getBlockEntity(belowPos);
        if (Immersives.immersiveETable.shouldTrack(belowPos, belowState, belowEntity, level)) {
            Immersives.immersiveETable.trackObject(belowPos, belowState, belowEntity, level);
        } else if (Immersives.immersiveCrafting.shouldTrack(belowPos, belowState, belowEntity, level)) {
            Immersives.immersiveCrafting.trackObject(belowPos, belowState, belowEntity, level);
        }
    }

    public static boolean onClick(int button) {
        // Don't run code if we're on spectator mode
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator()) return false;
        if (button == 1) {
            int cooldown = handleRightClick(Minecraft.getInstance().player);
            if (cooldown > 0) {
                ClientUtil.setRightClickCooldown(cooldown);
                return true;
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
        ActiveConfig.serverCopy = null; // Reset server copy of config

    }

    protected static <I extends AbstractImmersiveInfo> void tickInfos(AbstractImmersive<I> singleton, Player player) {
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
                singleton.tick(info, VRPluginVerify.clientInVR);
                if (info.hasHitboxes()) {
                    Tuple<Vec3, Vec3> startAndEnd = ClientUtil.getStartAndEndOfLookTrace(player);
                    Optional<Integer> closest = Util.rayTraceClosest(startAndEnd.getA(), startAndEnd.getB(),
                            info.getAllHitboxes());
                    info.slotHovered = closest.orElse(-1);
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
        if (Minecraft.getInstance().player == null) return false;

        BackpackInfo backpackInfo = Immersives.immersiveBackpack.getTrackedObjects().size() > 0 ?
                Immersives.immersiveBackpack.getTrackedObjects().get(0) : null;
        // Move to next row on left click if backpack is out
        if (backpackInfo != null && backpackInfo.slotHovered > -1) {
            ImmersiveBackpack.onHitboxInteract(player, backpackInfo, backpackInfo.slotHovered);
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
            BlockState state = player.level.getBlockState(pos);
            BlockEntity tileEnt = player.level.getBlockEntity(pos);

            if (tileEnt instanceof ChestBlockEntity || tileEnt instanceof EnderChestBlockEntity) {
                ChestInfo chestInfo = ImmersiveChest.findImmersive(tileEnt);
                if (chestInfo != null && chestInfo.isOpen) {
                    chestInfo.nextRow();
                    return true;
                }
            } else if (tileEnt instanceof ShulkerBoxBlockEntity shulkerBox) {
                for (ShulkerInfo info : Immersives.immersiveShulker.getTrackedObjects()) {
                    if (info.getBlockPosition().equals(shulkerBox.getBlockPos())) {
                        info.nextRow();
                        Immersives.immersiveShulker.setHitboxes(info);
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
                if (info.slotHovered != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int handleRightClick(Player player) {
        if (Minecraft.getInstance().gameMode == null) return 0;
        boolean inVR = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR && VRPlugin.API.apiActive(player);
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start = player.getEyePosition(1);
        Vec3 viewVec = player.getViewVector(1);
        Vec3 end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                viewVec.z * dist);

        if (!inVR) { // Don't handle right clicks for VR players, they have hands!
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
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

        if (ActiveConfig.rightClickChest && ActiveConfig.useChestImmersion) {
            BlockPos pos = ((BlockHitResult) looking).getBlockPos();
            BlockState state = player.level.getBlockState(pos);
            boolean isChest = state.getBlock() instanceof AbstractChestBlock && player.level.getBlockEntity(pos) instanceof ChestBlockEntity;
            boolean isEnderChest = state.getBlock() instanceof EnderChestBlock && player.level.getBlockEntity(pos) instanceof EnderChestBlockEntity;
            if ((isChest || isEnderChest) && !player.isCrouching()) { // Crouch to still open chest
                ChestInfo info = ImmersiveChest.findImmersive(player.level.getBlockEntity(pos));
                if (info != null) {
                    ImmersiveChest.openChest(info);
                    return Immersives.immersiveChest.getCooldownDesktop();
                }
            }
        }
        if (ActiveConfig.useShulkerImmersion) {
            BlockPos pos = ((BlockHitResult) looking).getBlockPos();
            BlockEntity blockEnt = player.level.getBlockEntity(pos);
            if (blockEnt instanceof ShulkerBoxBlockEntity) {
                for (ShulkerInfo info : Immersives.immersiveShulker.getTrackedObjects()) {
                    if (info.getBlockPosition().equals(pos)) {
                        ImmersiveShulker.openShulkerBox(info);
                        return Immersives.immersiveShulker.getCooldownDesktop();
                    }
                }
            }
        }

        return 0; // Still here in case if we need it later
    }


}
