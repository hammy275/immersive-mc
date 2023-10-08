package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.BeaconConfirmPacket;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.mixin.BeaconBlockEntityMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;

public class ImmersiveBeacon extends AbstractWorldStorageImmersive<BeaconInfo> {

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

    public ImmersiveBeacon() {
        super(1);
    }

    @Override
    protected void render(BeaconInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeBeacon / info.getItemTransitionCountdown();

        renderItem(info.items[0], stack, info.getPosition(0), info.slotHovered == 0 ? itemSize * 1.25f : itemSize,
                info.lastPlayerDir.getOpposite(), null, info.getHitbox(0),
                false, -1, info.light);
        for (int i = 0; i < info.triggerBoxes.length; i++) {
            if (info.triggerBoxes[i] != null) {
                renderHitbox(stack, info.triggerBoxes[i], info.triggerBoxes[i].getCenter());
            }
        }

        float effectSize = (float) effectHitboxSize / info.getItemTransitionCountdown();

        for (int i = 0; i <= 4; i++) {
            if (info.triggerBoxes[i] != null) {
                renderImage(stack, effectLocations[i], info.triggerBoxes[i].getCenter().add(0, -0.05, 0), info.lastPlayerDir,
                        info.effectSelected == i ? effectSize * 1.5f :
                                info.triggerHitboxSlotHovered == i ? effectSize * 1.25f : effectSize, info.light);
            }
        }

        float displaySize = (float) displayHitboxSize / info.getItemTransitionCountdown();

        if (info.effectSelected != -1) {
            renderImage(stack, effectLocations[info.effectSelected], info.effectSelectedDisplayPos.add(0, -0.05, 0),
                    info.lastPlayerDir, displaySize, info.light);
        }

        if (info.triggerBoxes[6] != null) { // Regen and plus
            renderImage(stack, regenerationLocation, info.triggerBoxes[5].getCenter().add(0, -0.05, 0),
                    info.lastPlayerDir, info.triggerHitboxSlotHovered == 5 ? displaySize * 1.25f : displaySize, info.light);
            renderImage(stack, addLocation, info.triggerBoxes[6].getCenter().add(0, -0.05, 0),
                    info.lastPlayerDir, info.triggerHitboxSlotHovered == 6 ? displaySize * 1.25f : displaySize, info.light);
        }

        if (info.triggerBoxes[7] != null && info.isReadyForConfirmExceptPayment()) {
            if (info.isReadyForConfirm()) {
                renderImage(stack, confirmLocation, info.triggerBoxes[7].getCenter().add(0, -0.1, 0),
                        info.lastPlayerDir, info.triggerHitboxSlotHovered == 7 ? itemSize * 1.25f : itemSize, info.light);
            }
            double xMult = 0;
            double zMult = 0;
            if (getForwardFromPlayer(Minecraft.getInstance().player).getOpposite().getNormal().getX() != 0) {
                zMult = 1;
            } else {
                xMult = 1;
            }
            renderHitbox(stack,
                    AABB.ofSize(info.effectSelectedDisplayPos, displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                    info.effectSelectedDisplayPos, true,
                    0f, 1f, 0f
                    );
            if (info.regenSelected) {
                renderHitbox(stack,
                        AABB.ofSize(info.triggerBoxes[5].getCenter(),
                                displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                        info.effectSelectedDisplayPos, true,
                        0f, 1f, 0f
                );
            } else {
                renderHitbox(stack,
                        AABB.ofSize(info.triggerBoxes[6].getCenter(),
                                displayHitboxSize * xMult, displayHitboxSize, displayHitboxSize * zMult),
                        info.effectSelectedDisplayPos, true,
                        0f, 1f, 0f
                );
            }
        }
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
            Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
            Vec3 forwardFromBlockVec = new Vec3(forward.getNormal().getX(), forward.getNormal().getY(),
                    forward.getNormal().getZ());
            Direction left = forward.getClockWise();

            Vec3 leftVec = new Vec3(left.getNormal().getX(), left.getNormal().getY(), left.getNormal().getZ());

            // For item input
            double itemHitboxSize = ClientConstants.itemScaleSizeBeacon;
            info.setPosition(0, Vec3.atBottomCenterOf(info.getBlockPosition()).add(forwardFromBlockVec.scale(0.25)
                    .add(forwardFromBlockVec.scale(itemHitboxSize / 2d)).add(0, itemHitboxSize / 2d + 0.01, 0)));
            info.setHitbox(0, AABB.ofSize(info.getPosition(0),
                    itemHitboxSize, itemHitboxSize, itemHitboxSize));
            info.triggerBoxes[7] = AABB.ofSize(
                    info.getPosition(0).add(0, itemHitboxSize / 2d + 0.25, 0),
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
                Direction centerDir = getForwardFromPlayer(Minecraft.getInstance().player).getOpposite();
                Vec3 forwardPos = center.add(leftVec.scale(0.8)).add(0, effectCircleRadius, 0);
                double rot0 = ((double) (timeSinceStartMilli % millisPerRot) / millisPerRot) * 2 * Math.PI;
                if (beaconLevel == 1) {
                    double rot1 = rot0 + Math.PI;
                    info.triggerBoxes[0] = AABB.ofSize(posToRotatedPos(forwardPos,
                            rot0, centerDir), effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[1] = AABB.ofSize(posToRotatedPos(forwardPos,
                            rot1, centerDir), effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    // Use .length - 1 as checkmark needs to not be overwritten
                    for (int i = 2; i < info.triggerBoxes.length - 1; i++) {
                        info.triggerBoxes[i] = null;
                    }
                } else if (beaconLevel == 2) {
                    double rotDiff = 2d * Math.PI / 4d;
                    double rot1 = rot0 + rotDiff;
                    double rot2 = rot0 + 2 * rotDiff;
                    double rot3 = rot0 + 3 * rotDiff;
                    info.triggerBoxes[0] = AABB.ofSize(posToRotatedPos(forwardPos, rot0, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[1] = AABB.ofSize(posToRotatedPos(forwardPos, rot1, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[2] = AABB.ofSize(posToRotatedPos(forwardPos, rot2, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[3] = AABB.ofSize(posToRotatedPos(forwardPos, rot3, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    for (int i = 4; i < info.triggerBoxes.length - 1; i++) {
                        info.triggerBoxes[i] = null;
                    }
                } else {
                    double rotDiff = 2d * Math.PI / 5d;
                    double rot1 = rot0 + rotDiff;
                    double rot2 = rot0 + 2 * rotDiff;
                    double rot3 = rot0 + 3 * rotDiff;
                    double rot4 = rot0 + 4 * rotDiff;
                    info.triggerBoxes[0] = AABB.ofSize(posToRotatedPos(forwardPos, rot0, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[1] = AABB.ofSize(posToRotatedPos(forwardPos, rot1, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[2] = AABB.ofSize(posToRotatedPos(forwardPos, rot2, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[3] = AABB.ofSize(posToRotatedPos(forwardPos, rot3, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    info.triggerBoxes[4] = AABB.ofSize(posToRotatedPos(forwardPos, rot4, centerDir),
                            effectHitboxSize, effectHitboxSize, effectHitboxSize);
                    if (beaconLevel == 4) {
                        info.triggerBoxes[5] = AABB.ofSize(info.effectSelectedDisplayPos.add(leftVec.scale(-0.25)),
                                displayHitboxSize, displayHitboxSize, displayHitboxSize);
                        info.triggerBoxes[6] = AABB.ofSize(info.effectSelectedDisplayPos.add(0, -0.25, 0),
                                displayHitboxSize, displayHitboxSize, displayHitboxSize);
                    } else {
                        for (int i = 5; i < info.triggerBoxes.length - 1; i++) {
                            info.triggerBoxes[i] = null;
                        }
                        info.regenSelected = false;
                    }
                }
            } else if (info.levelWasNonzero) { // Beacon level is 0 and it wasn't 0 before! (have this check for initial beacon info from server)
                for (int i = 0; i < info.triggerBoxes.length; i++) {
                    info.triggerBoxes[i] = null;
                }
                info.effectSelected = -1;
                info.regenSelected = false;
                Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
            }
            info.lastLevel = beaconLevel;
        }
    }

    @Override
    protected void doTick(BeaconInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        // Run every tick since effects spin in a circle
        setHitboxesAndPositions(info);

        info.lastPlayerDir = getForwardFromPlayer(Minecraft.getInstance().player).getOpposite();
    }

    @Override
    public BlockPos getLightPos(BeaconInfo info) {
        return info.getBlockPosition().above();
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useBeaconImmersion;
    }

    @Override
    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes tInfo, Player player, int hitboxNum) {
        BeaconInfo info = (BeaconInfo) tInfo;
        if (hitboxNum <= 4) {
            info.effectSelected = hitboxNum;
        } else if (hitboxNum == 7) {
            Network.INSTANCE.sendToServer(new BeaconConfirmPacket(info.getBlockPosition(), info.getEffectId(),
                    info.regenSelected ? BuiltInRegistries.MOB_EFFECT.getId(MobEffects.REGENERATION) : -1));
            VRRumble.rumbleIfVR(null, tInfo.getVRControllerNum(), CommonConstants.vibrationTimeWorldInteraction);
        } else {
            info.regenSelected = hitboxNum == 5;
        }
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isBeacon(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    protected void initInfo(BeaconInfo info) {
        info.startMillis = Instant.now().toEpochMilli();
        setHitboxesAndPositions(info);
        Network.INSTANCE.sendToServer(new BeaconDataPacket(info.getBlockPosition()));
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public void processStorageFromNetwork(AbstractWorldStorageInfo info, ImmersiveStorage storage) {
        info.items[0] = storage.getItem(0);
    }

    @Override
    public BeaconInfo getNewInfo(BlockPos pos) {
        return new BeaconInfo(pos); // Beacon data is only known server-side :(
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderBeacon;
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
