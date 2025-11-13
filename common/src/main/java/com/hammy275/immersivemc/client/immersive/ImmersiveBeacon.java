package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.immersive.info.HitboxItemPair;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.client.immersive_item.info.HeldImageImmersiveInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.BeaconConfirmPacket;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

public class ImmersiveBeacon extends AbstractImmersive<BeaconInfo, BeaconStorage> {

    private static final double effectHitboxSize = 0.2;
    private static final double displayHitboxSize = 0.2;
    private static final double effectCircleRadius = 0.2;
    private static final ResourceLocation[] effectLocations = new ResourceLocation[]{
            Util.mcId("textures/mob_effect/speed.png"),
            Util.mcId("textures/mob_effect/haste.png"),
            Util.mcId("textures/mob_effect/resistance.png"),
            Util.mcId("textures/mob_effect/jump_boost.png"),
            Util.mcId("textures/mob_effect/strength.png")
    };
    private static final ResourceLocation regenerationLocation = Util.mcId("textures/mob_effect/regeneration.png");
    private static final ResourceLocation confirmLocation = Util.id("immersive/beacon/confirm.png");
    private static final ResourceLocation addLocation = Util.id("immersive/beacon/add.png");

    @Override
    public BeaconInfo buildInfo(BlockPos pos, Level level) {
        BeaconInfo info = new BeaconInfo(pos);
        info.startMillis = Instant.now().toEpochMilli();
        Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
        return info;
    }

    @Override
    public int handleHitboxInteract(BeaconInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        ResourceLocation id = getHandler().getID();
        BiConsumer<HeldImageImmersiveInfo<Integer>, VRBodyPartData> heldItemTicker = !useGrabBeacon() ? null : (imageInfo, handData) -> {
            if (!Immersives.immersiveBeacon.getTrackedObjects().contains(info)) {
                imageInfo.shouldRemove = true;
            }
        };
        int hitboxIndex = hitboxIndices.get(0);
        if (hitboxIndex <= 4) {
            info.effectSelected = hitboxIndex;
            if (useGrabBeacon()) {
                HandImmersives.heldImageImmersive.setHeldImage(hand, effectLocations[hitboxIndex], id, hitboxIndex, 1f/3f, heldItemTicker);
            }
        } else if (hitboxIndex == 5) {
            info.regenSelected = true;
            if (useGrabBeacon()) {
                HandImmersives.heldImageImmersive.setHeldImage(hand, regenerationLocation, id, 5, 1f/3f, heldItemTicker);
            }
        } else if (hitboxIndex == 6) {
            info.regenSelected = false;
            if (useGrabBeacon()) {
                HandImmersives.heldImageImmersive.setHeldImage(hand, addLocation, id, -1, 1f/3f, heldItemTicker);
            }
        } else if (hitboxIndex == 7) {
            int effectId = -2;
            int secondaryId = -2;
            if (useGrabBeacon()) {
                List<HeldImageImmersiveInfo<?>> heldImages = HandImmersives.heldImageImmersive.getHeldImages(id);
                if (!heldImages.isEmpty()) {
                    if (info.lastLevel < 4) {
                        secondaryId = -1;
                    } else {
                        secondaryId = heldImages.stream().filter(imageInfo -> {
                            int held = (int) imageInfo.heldData;
                            return (held == 5 || held == -1) && info.hitboxes.get(7).box != null
                                    && BoundingBox.contains(info.hitboxes.get(7).box, VRClientAPI.instance().getPreTickWorldPose().getHand(imageInfo.hand).getPos());
                        }).findFirst().map(heldImageImmersiveInfo -> (int) heldImageImmersiveInfo.heldData).orElse(-2);
                        if (secondaryId != -2) {
                            secondaryId = info.regenSelected ? BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION.value()) : -1;
                        }
                    }
                    effectId = heldImages.stream().filter(imageInfo -> {
                        int held = (int) imageInfo.heldData;
                        return held >= 0 && held <= 4 && info.hitboxes.get(7).box != null
                                && BoundingBox.contains(info.hitboxes.get(7).box, VRClientAPI.instance().getPreTickWorldPose().getHand(imageInfo.hand).getPos());
                    }).findFirst().map(heldImageImmersiveInfo -> (int) heldImageImmersiveInfo.heldData).orElse(-2);
                    if (effectId >= 0) {
                        info.effectSelected = effectId;
                    }
                }
            } else {
                effectId = info.effectSelected;
                secondaryId = info.regenSelected ? BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION.value()) : -1;
            }
            if (effectId >= 0 && secondaryId != -2 && !info.hitboxes.get(8).item.isEmpty()) {
                effectId = info.getEffectId();
                Network.INSTANCE.sendToServer(new BeaconConfirmPacket(info.getBlockPosition(), effectId, secondaryId));
                VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimeWorldInteraction);
                HandImmersives.heldImageImmersive.removeImages(id);
            } else {
                return -1;
            }
        } else {
            ImmersiveClientLogicHelpers.instance().sendSwapPacket(info.getBlockPosition(), List.of(0), hand, false);
        }
        return ImmersiveClientConstants.instance().defaultCooldown();
    }

    @Override
    public void tick(BeaconInfo info) {
        super.tick(info);

        // Run every tick since effects spin in a circle
        for (HitboxItemPair pair : info.hitboxes) {
            pair.lastPos = pair.box == null ? null : BoundingBox.getCenter(pair.box);
        }
        setHitboxesAndPositions(info);

        info.lastPlayerDir = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
    }

    @Override
    @Nullable
    public AABB getDragHitbox(BeaconInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(BeaconInfo info, int hitboxIndex) {
        return true;
    }

    @Override
    public boolean shouldRender(BeaconInfo info) {
        return info.lastPlayerDir != null && info.areaAboveIsAir && info.hasHitboxes();
    }

    @Override
    public void render(BeaconInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        helpers.renderItemWithInfo(info.hitboxes.get(8).item, stack, ClientConstants.itemScaleSizeBeacon,
                false, info.light, info, true, 8, null,
                info.lastPlayerDir.getOpposite(), null);

        float transitionMultiplier = helpers.getTransitionMultiplier(info.getTicksExisted());

        float effectSize = (float) effectHitboxSize * transitionMultiplier;
        for (int i = 0; i < info.hitboxes.size() - 1; i++) {
            HitboxItemPair hitbox = info.hitboxes.get(i);
            if (hitbox.box != null) {
                BoundingBox renderHitbox = hitbox.getRenderHitbox(partialTick);
                helpers.renderHitbox(stack, renderHitbox);
                if (i <= 4) {
                    helpers.renderImage(stack, effectLocations[i],
                            BoundingBox.getCenter(renderHitbox).add(0, -0.05, 0),
                            info.effectSelected == i && !useGrabBeacon() ? effectSize * 1.5f : info.isSlotHovered(i) ? effectSize * 1.25f : effectSize,
                            info.light, info.lastPlayerDir);
                }
            }
        }

        float displaySize = (float) displayHitboxSize * transitionMultiplier;

        if (info.effectSelected != -1 && !useGrabBeacon()) {
            helpers.renderImage(stack, effectLocations[info.effectSelected], info.effectSelectedDisplayPos.add(0, -0.05, 0),
                    displaySize, info.light, info.lastPlayerDir);
        }

        for (int i = 5; i <= 6; i++) {
            HitboxItemPair hitbox = info.hitboxes.get(i);
            if (hitbox.box != null) {
                helpers.renderImage(stack, i == 5 ? regenerationLocation : addLocation,
                        BoundingBox.getCenter(hitbox.box).add(0, -0.05, 0),
                        info.isSlotHovered(i) ? displaySize * 1.25f : displaySize,
                        info.light, info.lastPlayerDir);
            }
        }

        HitboxItemPair hitbox7 = info.hitboxes.get(7);
        if (hitbox7.box != null && info.isEffectSelected()) {
            if (useGrabBeacon()) {
                helpers.renderHitbox(stack, hitbox7.box);
            } else if (info.isReadyForConfirm()) {
                helpers.renderImage(stack, confirmLocation, BoundingBox.getCenter(hitbox7.box).add(0, -0.1, 0),
                        info.isSlotHovered(7) ? ClientConstants.itemScaleSizeBeacon * 1.25f : ClientConstants.itemScaleSizeBeacon,
                        info.light, info.lastPlayerDir);
            }

            Direction playerForward = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
            double xMult = 0;
            double zMult = 0;
            if (playerForward.getUnitVec3i().getX() != 0) {
                zMult = 1;
            } else {
                xMult = 1;
            }
            if (!useGrabBeacon()) {
                helpers.renderHitbox(stack,
                        AABB.ofSize(info.effectSelectedDisplayPos, displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                        true, 0f, 1f, 0f);
                if (info.regenSelected && info.hitboxes.get(5).box != null) {
                    helpers.renderHitbox(stack,
                            AABB.ofSize(BoundingBox.getCenter(info.hitboxes.get(5).box),
                                    displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                            true, 0f, 1f, 0f);
                } else if (!info.regenSelected && info.hitboxes.get(6).box != null) {
                    helpers.renderHitbox(stack,
                            AABB.ofSize(BoundingBox.getCenter(info.hitboxes.get(6).box),
                                    displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                            true, 0f, 1f, 0f);
                }
            }
        }
    }

    @Override
    public ImmersiveHandler<BeaconStorage> getHandler() {
        return ImmersiveHandlers.beaconHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("beacon", () -> new ItemStack(Items.BEACON),
                config -> config.useBeaconImmersive,
                (config, newVal) -> config.useBeaconImmersive = newVal);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(BeaconInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(BeaconInfo info, BeaconStorage storage) {
        info.hitboxes.get(8).item = storage.getItem(0);
    }

    @Override
    public boolean isVROnly() {
        return false;
    }

    protected void setHitboxesAndPositions(BeaconInfo info) {
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof BeaconBlockEntity beacon) {
            for (int x = -1; x <= 1; x++) { // 3x3 area one block and two blocks above must all be air to look nice
                for (int y = 1; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (!Minecraft.getInstance().level.getBlockState(info.getBlockPosition().offset(x, y, z)).canBeReplaced()) {
                            info.areaAboveIsAir = false;
                            return;
                        }
                    }
                }
            }
            info.areaAboveIsAir = true;

            // NOTE: Unlike most other places in ImmersiveMC, left refers to left from the player's
            // perspective, not the block's!
            Vec3 center = Vec3.atCenterOf(info.getBlockPosition()).add(0, 1, 0);
            Direction beaconForward = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition());
            Vec3 forwardFromBlockVec = new Vec3(beaconForward.getUnitVec3i().getX(), beaconForward.getUnitVec3i().getY(),
                    beaconForward.getUnitVec3i().getZ());
            Direction left = beaconForward.getClockWise();

            Vec3 leftVec = left.getUnitVec3();

            // For item input
            double itemHitboxSize = ClientConstants.itemScaleSizeBeacon;
            info.hitboxes.get(8).box = AABB.ofSize(
                    Vec3.atBottomCenterOf(info.getBlockPosition()).add(forwardFromBlockVec.scale(0.25)
                            .add(forwardFromBlockVec.scale(itemHitboxSize / 2d)).add(0, itemHitboxSize / 2d + 0.01, 0)),
                    itemHitboxSize, itemHitboxSize, itemHitboxSize
            );
            if (useGrabBeacon()) {
                info.hitboxes.get(7).box = new AABB(info.getBlockPosition()).inflate(0.001);
            } else {
                info.hitboxes.get(7).box = AABB.ofSize(
                        BoundingBox.getCenter(info.hitboxes.get(8).box).add(0, itemHitboxSize / 2d + 0.25, 0),
                        itemHitboxSize, itemHitboxSize, itemHitboxSize
                );
            }

            info.effectSelectedDisplayPos = center.add(0, 0.125, 0).add(leftVec.scale(-1d/3d)).add(forwardFromBlockVec.scale(0.45));

            int beaconLevel = ((BeaconBlockEntityMixin) beacon).immersiveMC$getLevels();
            if (info.lastLevel > beaconLevel) { // Beacon downgraded, potentially clear selected
                if (beaconLevel == 1 && info.effectSelected > 1) {
                    info.effectSelected = -1;
                } else if (beaconLevel == 2 && info.effectSelected == 4) {
                    info.effectSelected = -1;
                }
                Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
            }
            // Need to get the direction the player is facing, so opposite the forward (which is immersive's forward)
            Direction centerDir = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
            if (beaconLevel > 0) {
                info.levelWasNonzero = true;
                long timeSinceStartMilli = Instant.now().toEpochMilli() - info.startMillis;
                long millisPerRot = 9000;
                Vec3 forwardPos = center.add(leftVec.scale(0.8)).add(0, effectCircleRadius, 0).add(forwardFromBlockVec.scale(0.45));
                double rot0 = ((double) (timeSinceStartMilli % millisPerRot) / millisPerRot) * 2 * Math.PI;
                if (beaconLevel == 1) {
                    double rot1 = rot0 + Math.PI;
                    info.hitboxes.get(0).box = AABB.ofSize(posToRotatedPos(forwardPos,
                            rot0, centerDir), effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(1).box = AABB.ofSize(posToRotatedPos(forwardPos,
                            rot1, centerDir), effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    // Use .length - 1 as checkmark needs to not be overwritten
                    for (int i = 2; i < info.hitboxes.size() - 2; i++) {
                        info.hitboxes.get(i).box = null;
                    }
                } else if (beaconLevel == 2) {
                    double rotDiff = 2d * Math.PI / 4d;
                    double rot1 = rot0 + rotDiff;
                    double rot2 = rot0 + 2 * rotDiff;
                    double rot3 = rot0 + 3 * rotDiff;
                    info.hitboxes.get(0).box = AABB.ofSize(posToRotatedPos(forwardPos, rot0, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(1).box = AABB.ofSize(posToRotatedPos(forwardPos, rot1, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(2).box = AABB.ofSize(posToRotatedPos(forwardPos, rot2, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(3).box = AABB.ofSize(posToRotatedPos(forwardPos, rot3, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    for (int i = 4; i < info.hitboxes.size() - 2; i++) {
                        info.hitboxes.get(i).box = null;
                    }
                } else {
                    double rotDiff = 2d * Math.PI / 5d;
                    double rot1 = rot0 + rotDiff;
                    double rot2 = rot0 + 2 * rotDiff;
                    double rot3 = rot0 + 3 * rotDiff;
                    double rot4 = rot0 + 4 * rotDiff;
                    info.hitboxes.get(0).box = AABB.ofSize(posToRotatedPos(forwardPos, rot0, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(1).box = AABB.ofSize(posToRotatedPos(forwardPos, rot1, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(2).box = AABB.ofSize(posToRotatedPos(forwardPos, rot2, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(3).box = AABB.ofSize(posToRotatedPos(forwardPos, rot3, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.hitboxes.get(4).box = AABB.ofSize(posToRotatedPos(forwardPos, rot4, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    if (beaconLevel == 4) {
                        if (useGrabBeacon()) {
                            Vec3 effectGrabPos = info.effectSelectedDisplayPos.add(leftVec.scale(-0.25)).add(0, -0.1, 0);
                            info.hitboxes.get(5).box = AABB.ofSize(effectGrabPos.add(leftVec.scale(-0.11)),
                                    displayHitboxSize, displayHitboxSize, displayHitboxSize);
                            info.hitboxes.get(6).box = AABB.ofSize(effectGrabPos.add(leftVec.scale(0.11)),
                                    displayHitboxSize, displayHitboxSize, displayHitboxSize);
                        } else {
                            info.hitboxes.get(5).box = AABB.ofSize(info.effectSelectedDisplayPos.add(leftVec.scale(-0.25)),
                                    displayHitboxSize, displayHitboxSize, displayHitboxSize);
                            info.hitboxes.get(6).box = AABB.ofSize(info.effectSelectedDisplayPos.add(0, -0.25, 0),
                                    displayHitboxSize, displayHitboxSize, displayHitboxSize);
                        }
                    } else {
                        for (int i = 5; i < info.hitboxes.size() - 2; i++) {
                            info.hitboxes.get(i).box = null;
                        }
                        info.regenSelected = false;
                    }
                }
            } else if (info.levelWasNonzero) { // Beacon level is 0 and it wasn't 0 before! (have this check for initial beacon info from server)
                for (int i = 0; i < info.hitboxes.size(); i++) {
                    info.hitboxes.get(i).box = null;
                }
                info.effectSelected = -1;
                info.regenSelected = false;
                Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
            }
            if (centerDir != info.lastPlayerDir) {
                for (HitboxItemPair hitbox : info.hitboxes) {
                    hitbox.lastPos = null;
                }
            }
            info.lastLevel = beaconLevel;
        }

        for (int i = 0; i < info.hitboxes.size(); i++) {
            info.hitboxes.get(i).isTriggerHitbox = !useGrabBeacon();
        }
    }

    /**
     *
     * forwardPos represents 12 o'clock for a circle the items are traveling on.
     * Credit to https://stackoverflow.com/questions/12161277/how-to-rotate-a-vertex-around-a-certain-point
     * for help here.
     *
     * @param forwardPos Three o'clock position, which is closest to the player
     * @param rotRad Rotation in radians. Multiplied by -1 to make clockwise.
     * @param playerForwardDir Direction player is facing
     * @return Position for object after rotation
     */
    private Vec3 posToRotatedPos(Vec3 forwardPos, double rotRad, Direction playerForwardDir) {

        Vec3 circleCenter = forwardPos.add(
                Direction.DOWN.getUnitVec3().scale(effectCircleRadius)
        );

        Direction.Axis axisFacing = playerForwardDir.getAxis();
        double xz = axisFacing == Direction.Axis.Z ? circleCenter.x : circleCenter.z;

        if (playerForwardDir == Direction.NORTH || playerForwardDir == Direction.EAST) {
            rotRad = -rotRad;
        }

        double newXZ = xz
                + effectCircleRadius * Math.cos(rotRad)
                - effectCircleRadius * Math.sin(rotRad);
        double newY = circleCenter.y
                + effectCircleRadius * Math.sin(rotRad)
                + effectCircleRadius * Math.cos(rotRad);

        if (axisFacing == Direction.Axis.Z) {
            return new Vec3(newXZ, newY, forwardPos.z);
        } else {
            return new Vec3(forwardPos.x, newY, newXZ);
        }
    }

    public static boolean useGrabBeacon() {
        return VRVerify.clientInVR() && ActiveConfig.active().useGrabBeaconInVR;
    }
}
