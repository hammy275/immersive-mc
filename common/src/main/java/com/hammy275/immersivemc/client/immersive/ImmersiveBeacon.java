package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.immersive.info.HitboxItemPair;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.BeaconConfirmPacket;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.vr.VRRumble;
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

import java.time.Instant;

public class ImmersiveBeacon extends AbstractImmersive<BeaconInfo, BeaconStorage> {

    private static final double effectHitboxSize = 0.2;
    private static final double displayHitboxSize = 0.2;
    private static final double effectCircleRadius = 0.2;
    private static final ResourceLocation[] effectLocations = new ResourceLocation[]{
            new ResourceLocation("textures/mob_effect/speed.png"),
            new ResourceLocation("textures/mob_effect/haste.png"),
            new ResourceLocation("textures/mob_effect/resistance.png"),
            new ResourceLocation("textures/mob_effect/jump_boost.png"),
            new ResourceLocation("textures/mob_effect/strength.png")
    };
    private static final ResourceLocation regenerationLocation = new ResourceLocation("textures/mob_effect/regeneration.png");
    private static final ResourceLocation confirmLocation = new ResourceLocation(ImmersiveMC.MOD_ID, "confirm.png");
    private static final ResourceLocation addLocation = new ResourceLocation(ImmersiveMC.MOD_ID, "add.png");

    @Override
    public BeaconInfo buildInfo(BlockPos pos, Level level) {
        BeaconInfo info = new BeaconInfo(pos);
        info.startMillis = Instant.now().toEpochMilli();
        Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
        return info;
    }

    @Override
    public int handleHitboxInteract(BeaconInfo info, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        if (hitboxIndex <= 4) {
            info.effectSelected = hitboxIndex;
        } else if (hitboxIndex == 5) {
            info.regenSelected = true;
        } else if (hitboxIndex == 6) {
            info.regenSelected = false;
        } else if (hitboxIndex == 7) {
            Network.INSTANCE.sendToServer(new BeaconConfirmPacket(info.getBlockPosition(), info.getEffectId(),
                    info.regenSelected ? BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION) : -1));
            VRRumble.rumbleIfVR(Minecraft.getInstance().player, 0, CommonConstants.vibrationTimeWorldInteraction);
        } else {
            Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), 0, hand));
        }
        return ClientConstants.defaultCooldownTicks;
    }

    @Override
    public void tick(BeaconInfo info) {
        super.tick(info);

        // Run every tick since effects spin in a circle
        setHitboxesAndPositions(info);

        info.lastPlayerDir = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
    }

    @Override
    public boolean shouldRender(BeaconInfo info) {
        return info.lastPlayerDir != null && info.areaAboveIsAir && info.hasHitboxes();
    }

    @Override
    public void render(BeaconInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        helpers.renderItemWithInfo(info.hitboxes.get(8).item, stack, ClientConstants.itemScaleSizeBeacon,
                false, info.light, info, true, 8, null,
                info.lastPlayerDir.getOpposite(), null);

        float transitionMultiplier = helpers.getTransitionMultiplier(info.getTicksExisted());

        float effectSize = (float) effectHitboxSize * transitionMultiplier;
        for (int i = 0; i < info.hitboxes.size() - 1; i++) {
            HitboxItemPair hitbox = info.hitboxes.get(i);
            if (hitbox.box != null) {
                helpers.renderHitbox(stack, hitbox.box);
                if (i <= 4) {
                    helpers.renderImage(stack, effectLocations[i],
                            BoundingBox.getCenter(hitbox.box).add(0, -0.05, 0),
                            info.effectSelected == i ? effectSize * 1.5f : info.isSlotHovered(i) ? effectSize * 1.25f : effectSize,
                            info.light, info.lastPlayerDir);
                }
            }
        }

        float displaySize = (float) displayHitboxSize * transitionMultiplier;

        if (info.effectSelected != -1) {
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
            if (info.isReadyForConfirm()) {
                helpers.renderImage(stack, confirmLocation, BoundingBox.getCenter(hitbox7.box).add(0, -0.1, 0),
                        info.isSlotHovered(7) ? ClientConstants.itemScaleSizeBeacon * 1.25f : ClientConstants.itemScaleSizeBeacon,
                        info.light, info.lastPlayerDir);
            }

            Direction playerForward = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
            double xMult = 0;
            double zMult = 0;
            if (playerForward.getNormal().getX() != 0) {
                zMult = 1;
            } else {
                xMult = 1;
            }
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

    @Override
    public ImmersiveHandler<BeaconStorage> getHandler() {
        return ImmersiveHandlers.beaconHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo("beacon", () -> new ItemStack(Items.BEACON),
                config -> config.useBeaconImmersion,
                (config, newVal) -> config.useBeaconImmersion = newVal);
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(BeaconInfo info) {
        return false;
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
            Vec3 forwardFromBlockVec = new Vec3(beaconForward.getNormal().getX(), beaconForward.getNormal().getY(),
                    beaconForward.getNormal().getZ());
            Direction left = beaconForward.getClockWise();

            Vec3 leftVec = new Vec3(left.getNormal().getX(), left.getNormal().getY(), left.getNormal().getZ());

            // For item input
            double itemHitboxSize = ClientConstants.itemScaleSizeBeacon;
            info.hitboxes.get(8).box = AABB.ofSize(
                    Vec3.atBottomCenterOf(info.getBlockPosition()).add(forwardFromBlockVec.scale(0.25)
                            .add(forwardFromBlockVec.scale(itemHitboxSize / 2d)).add(0, itemHitboxSize / 2d + 0.01, 0)),
                    itemHitboxSize, itemHitboxSize, itemHitboxSize
            );
            info.hitboxes.get(7).box = AABB.ofSize(
                    BoundingBox.getCenter(info.hitboxes.get(8).box).add(0, itemHitboxSize / 2d + 0.25, 0),
                    itemHitboxSize, itemHitboxSize, itemHitboxSize
            );

            info.effectSelectedDisplayPos = center.add(0, 0.125, 0).add(leftVec.scale(-1d/3d));

            int beaconLevel = ((BeaconBlockEntityMixin) beacon).getLevels();
            if (info.lastLevel > beaconLevel) { // Beacon downgraded, potentially clear selected
                if (beaconLevel == 1 && info.effectSelected > 1) {
                    info.effectSelected = -1;
                } else if (beaconLevel == 2 && info.effectSelected == 4) {
                    info.effectSelected = -1;
                }
                Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
            }
            if (beaconLevel > 0) {
                info.levelWasNonzero = true;
                long timeSinceStartMilli = Instant.now().toEpochMilli() - info.startMillis;
                long millisPerRot = 9000;
                // Need to get the direction the player is facing, so opposite the forward (which is immersive's forward)
                Direction centerDir = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(Minecraft.getInstance().player, info.getBlockPosition()).getOpposite();
                Vec3 forwardPos = center.add(leftVec.scale(0.8)).add(0, effectCircleRadius, 0);
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
                        info.hitboxes.get(5).box = AABB.ofSize(info.effectSelectedDisplayPos.add(leftVec.scale(-0.25)),
                                displayHitboxSize, displayHitboxSize, displayHitboxSize);
                        info.hitboxes.get(6).box = AABB.ofSize(info.effectSelectedDisplayPos.add(0, -0.25, 0),
                                displayHitboxSize, displayHitboxSize, displayHitboxSize);
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
            info.lastLevel = beaconLevel;
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
                Vec3.atLowerCornerOf(Direction.DOWN.getNormal()).scale(effectCircleRadius)
        );

        Direction.Axis axisFacing = playerForwardDir.getAxis();
        double xz = axisFacing == Direction.Axis.Z ? circleCenter.x : circleCenter.z;

        rotRad = -rotRad;

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
}
