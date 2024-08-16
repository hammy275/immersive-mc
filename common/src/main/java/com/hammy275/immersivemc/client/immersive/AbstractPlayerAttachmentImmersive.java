package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Abstract immersive anything
 * @param <I> Info type
 */
public abstract class AbstractPlayerAttachmentImmersive<I extends AbstractPlayerAttachmentInfo, S extends NetworkStorage> {
    public static final int maxLight = LightTexture.pack(15, 15);

    protected final List<I> infos;
    public final int maxImmersives;
    protected boolean forceDisableItemGuide = false;
    public boolean forceTickEvenIfNoTrack = false;

    public AbstractPlayerAttachmentImmersive(int maxImmersives) {
        Immersives.IMMERSIVE_ATTACHMENTS.add(this);
        this.maxImmersives = maxImmersives;
        this.infos = new ArrayList<>(maxImmersives > 0 ? maxImmersives + 1 : 16);
    }

    /**
     * Gets player position while accounting for partial ticks (getFrameTime())
     * @return Player position while accounting for partial ticks
     */
    public Vec3 playerPos() {
        return ClientUtil.playerPos();
    }

    /**
     * Called just before render() for data that needs to be updated just before render time, rather than
     * on a tick-based interval.
     *
     * WARNING: This is called per render-pass, and will completely mess with the player position!
     */
    protected void renderTick(I info, boolean isInVR) {

    }

    /**
     * Tick method that will always run exactly once per client tick
     */
    public void globalTick() {

    }

    /**
     * @return Whether this immersive should have tracking initiated by the client. If true, no data should ever be
     * sent from the server to the client for this immersive.
     */
    public boolean clientAuthoritative() {
        return false;
    }

    /**
     * @return ImmersiveHandler that this immersive uses. Can return null if this
     * immersive doesn't receive item data from the server through FetchInventoryPacket.
     */
    @Nullable
    public abstract ImmersiveHandler<S> getHandler();

    public boolean hitboxesAvailable(AbstractPlayerAttachmentInfo info) {
        return true;
    }

    public abstract boolean shouldRender(I info, boolean isInVR);

    protected abstract void render(I info, PoseStack stack, boolean isInVR);

    public abstract boolean enabledInConfig();

    protected abstract boolean inputSlotShouldRenderHelpHitbox(I info, int slotNum);

    public abstract boolean shouldTrack(BlockPos pos, Level level);

    @Nullable
    public abstract I refreshOrTrackObject(BlockPos pos, Level level);

    // Whether to block a right-click if the option to block right clicks to open GUIs is enabled
    public abstract boolean shouldBlockClickIfEnabled(AbstractPlayerAttachmentInfo info);

    /**
     * Initializes an `info` instance after it's constructed.
     * Useful for immersives that have non-changing hitboxes/positions.
     *
     * @param info Info instance that should be initialized
     */
    protected abstract void initInfo(I info);

    /**
     * Called just before handleRightClick() and handleTriggerHitboxRightClick()
     * @param info Info instance that had a hitbox click
     */
    public void onAnyRightClick(AbstractPlayerAttachmentInfo info) {

    }

    public boolean isVROnly() {
        return false;
    }

    public abstract void handleRightClick(AbstractPlayerAttachmentInfo info, Player player, int closest,
                                          InteractionHand hand);

    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        // No-op by default. Only needed realistically if the `info` implements InfoTriggerHitboxes
    }

    public void onRemove(I info) {}

    protected boolean slotHelpBoxIsSelected(I info, int slotNum) {
        return info.slotHovered(slotNum);
    }

    public abstract void processStorageFromNetwork(AbstractPlayerAttachmentInfo info, S storage);

    public void tick(I info, boolean isInVR) {
        if (enabledInConfig()) {
            if (!info.initCompleted) {
                initInfo(info);
                info.initCompleted = true;
            }
            if (Minecraft.getInstance().level != null && (shouldTrack(info.getBlockPosition(),
                    Minecraft.getInstance().level) || forceTickEvenIfNoTrack)) {
                doTick(info, isInVR);
                info.setInputSlots();
                if (this.hasMultipleLightPositions(info)) {
                    info.light = getLight(getLightPositions(info));
                } else {
                    info.light = getLight(getLightPos(info));
                }
            } else {
                info.remove();
            }
        }
    }

    public int getCooldownVR() {
        return 12;
    }

    public int getCooldownDesktop() {
        return 8;
    }

    protected void doTick(I info, boolean isInVR) {
        // Set the cooldown (transition time) based on how long we've existed or until we stop existing
        if (info.getItemTransitionCountdown() > 1 && info.getTicksLeft() > 20) {
            info.changeItemTransitionCountdown(-1);
        } else if (info.getItemTransitionCountdown() < ClientConstants.transitionTime && info.getTicksLeft() <= 20) {
            info.changeItemTransitionCountdown(1);
        }

        if (info.getTicksLeft() > 0) {
            info.changeTicksLeft(-1);
        }
        info.ticksActive++;

        if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.distanceToSqr(Vec3.atCenterOf(info.getBlockPosition())) >
                        CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }
    }

    /**
     * Gets the BlockPos used for lighting calculations. Called immediately after doTick(), so anything set
     * up then, it will be in info to use.
     *
     * Note that this should not be inside another block (even the immmersive!). The best option is to set this
     * to be one of the blocks that must be unoccupied for the immersive to render (the block above the crafting
     * table, for example).
     * @param info The immersive info instance.
     * @return The BlockPos for lighting.
     */
    public abstract BlockPos getLightPos(I info);

    /**
     * Override to use getLightPositions instead of getLightPos
     * @param info Info to determine if to use multiple light positions
     * @return Whether to use getLightPositions() or getLightPos()
     */
    public boolean hasMultipleLightPositions(I info) {
        return false;
    }

    /**
     * Override to return multiple light positions instead of one for light calculations if hasMultipleLightPositions()
     * return true.
     *
     * @param info Info to get positions from
     * @return All positions for lighting
     */
    public BlockPos[] getLightPositions(I info) {
        return new BlockPos[0];
    }

    // Below this line are utility functions. Everything above MUST be overwritten, and have super() called!

    /**
     * Do rendering
     *
     * This is the render method that should be called by outside functions
     */
    public void doRender(I info, PoseStack stack, boolean isInVR) {
        if (shouldRender(info, isInVR)) {
            try {
                renderTick(info, isInVR);
                render(info, stack, isInVR);
                if (ActiveConfig.active().placementGuideMode != PlacementGuideMode.OFF && !forceDisableItemGuide
                    && nearbyItemGuideRenderCheck(info)) {
                    // Add from -1 because we're adding lengths, so we subtract one to have valid indexes
                    for (int i = 0; i < info.getInputSlots().length; i++) {
                        if (inputSlotShouldRenderHelpHitbox(info, i)) {
                            BoundingBox itemBox = info.getInputSlots()[i];
                            enqueueItemGuideRender(stack, itemBox, 0.2f, slotHelpBoxIsSelected(info, i), info.light);
                        }
                    }
                }
            } catch (NullPointerException | ConcurrentModificationException ignored) {}
            // Until we have some sort of lock (if we ever do), we need to try-catch NPEs and CMEs during rendering
            // in case if the other thread modifies things while we render

        }
    }

    private boolean nearbyItemGuideRenderCheck(I info) {
        HitResult hit = Minecraft.getInstance().hitResult;
        Player player = Minecraft.getInstance().player;
        boolean inVR = VRPluginVerify.clientInVR();
        Vec3 vrHitStart = inVR ? VRPlugin.API.getVRPlayer(player).getHMD().position() : null;
        Vec3 vrLook = inVR ? VRPlugin.API.getVRPlayer(player).getHMD().getLookAngle() : null;
        Vec3 vrHitEnd = inVR ? vrHitStart.add(vrLook.scale(Minecraft.getInstance().gameMode.getPickRange())) : null;
        HitResult vrHit = inVR ? player.level().clip(new ClipContext(vrHitStart, vrHitEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)) : null;
        return (hit != null && hit.getType() == HitResult.Type.BLOCK &&
                ((BlockHitResult) hit).getBlockPos().equals(info.getBlockPosition()))
                || playerPos().distanceTo(Vec3.atCenterOf(info.getBlockPosition())) <= 4
                || (vrHit != null && vrHit.getType() == HitResult.Type.BLOCK && ((BlockHitResult) vrHit).getBlockPos().equals(info.getBlockPosition()));
    }

    public List<I> getTrackedObjects() {
        return infos;
    }

    /**
     * Run when there are no infos
     */
    public void noInfosTick() {

    }

    public void renderItem(ItemStack item, PoseStack stack, Vec3 pos, float size, Direction facing,
                           BoundingBox hitbox, boolean renderItemCounts, int light) {
        renderItem(item, stack, pos, size, facing, null, hitbox, renderItemCounts, -1, light);
    }

    /**
     * Renders an item at the specified position
     * @param item Item to render
     * @param stack PoseStack in
     * @param pos Position to render at
     * @param size Size to render at
     * @param facing Direction to face (should be the direction of the block). Can be null to look at camera.
     * @param upDown Direction upwards or downwards. Can be null if not facing up or down.
     * @param hitbox Hitbox for debug rendering
     * @param renderItemCounts Whether to render an item count with the item
     * @param spinDegrees Degress to spin on x/z. Overwritten if facing is nonnull.
     */
    public void renderItem(ItemStack item, PoseStack stack, Vec3 pos, float size, Direction facing, Direction upDown,
                           BoundingBox hitbox, boolean renderItemCounts, int spinDegrees, int light) {
        ImmersiveRenderHelpersImpl.INSTANCE.renderItem(item, stack, size, hitbox, renderItemCounts, light, spinDegrees < 0 ? null : (float) spinDegrees, facing, upDown);
    }

    protected void enqueueItemGuideRender(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {
        ClientRenderSubscriber.itemGuideRenderData.add(
                new ClientRenderSubscriber.ItemGuideRenderData(stack, hitbox, alpha, isSelected, light));
    }

    protected void renderHitbox(PoseStack stack, BoundingBox hitbox) {
        renderHitbox(stack, hitbox, false);
    }

    protected void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender) {
        renderHitbox(stack, hitbox, alwaysRender, 1, 1, 1);
    }

    public static void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue) {
        renderHitbox(stack, hitbox, alwaysRender, red, green, blue, 1);
    }

    public static void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue, float alpha) {
        ImmersiveRenderHelpersImpl.INSTANCE.renderHitbox(stack, hitbox, alwaysRender, red, green, blue, alpha);
    }

    /**
     * Gets the direction to the left from the Direction's perspective, assuming the Direction is
     * looking at the player. This makes it to the right for the player.
     * @param forward Forward direction of the block
     * @return The aforementioned left
     */
    public static Direction getLeftOfDirection(Direction forward) {
        if (forward == Direction.UP || forward == Direction.DOWN) {
            throw new IllegalArgumentException("Direction cannot be up or down!");
        }
        if (forward == Direction.NORTH) {
            return Direction.WEST;
        } else if (forward == Direction.WEST) {
            return Direction.SOUTH;
        } else if (forward == Direction.SOUTH) {
            return Direction.EAST;
        }
        return Direction.NORTH;
    }

    public static Vec3 getTopCenterOfBlock(BlockPos pos) {
        // Only add 0.5 to y since atCenterOf moves it up 0.5 for us
        return Vec3.upFromBottomCenterOf(pos, 1);
    }

    /**
     * Gets the forward direction of the block based on the player
     *
     * Put simply, this returns the best direction such that the block is "facing" the play by looking that direction.
     * @param player Player to get forward from
     * @param pos Blosck position of the
     * @return The forward direction of a block to use.
     */
    public static Direction getForwardFromPlayer(Player player, BlockPos pos) {
        Vec3 blockPos = Vec3.atBottomCenterOf(pos);
        Vec3 playerPos = player.position();
        Vec3 diff = playerPos.subtract(blockPos);
        Direction.Axis axis = Math.abs(diff.x) > Math.abs(diff.z) ? Direction.Axis.X : Direction.Axis.Z;
        if (axis == Direction.Axis.X) {
            return diff.x < 0 ? Direction.WEST : Direction.EAST;
        } else {
            return diff.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    public static Vec3[] get3x3HorizontalGrid(BlockPos blockPos, double spacing, Direction blockForward,
                                       boolean use3DCompat) {
        Vec3 pos = getTopCenterOfBlock(blockPos);
        if (use3DCompat) {
            pos = pos.add(0, 1d/16d, 0);
        }
        Direction left = getLeftOfDirection(blockForward);

        Vec3 leftOffset = new Vec3(
                left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);
        Vec3 rightOffset = new Vec3(
                left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);

        Vec3 topOffset = new Vec3(
                blockForward.getNormal().getX() * -spacing, 0, blockForward.getNormal().getZ() * -spacing);
        Vec3 botOffset = new Vec3(
                blockForward.getNormal().getX() * spacing, 0, blockForward.getNormal().getZ() * spacing);


        return new Vec3[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
        };
    }

    public int getLight(BlockPos pos) {
        return ImmersiveClientLogicHelpers.instance().getLight(pos);
    }

    public int getLight(BlockPos[] positions) {
        return ImmersiveClientLogicHelpers.instance().getLight(Arrays.stream(positions).toList());
    }

    public void clearImmersives() {
        this.infos.clear();
    }

    public static Direction getForwardFromPlayerUpAndDown(Player player, BlockPos pos) {
        return getForwardFromPlayerUpAndDownFilterBlockFacing(player, pos, false);
    }

    /**
     * Same as getForwardFromPlayer, but can return the block facing up or down, alongside any of the four
     * directions of N/E/S/W.
     * @param player Player.
     * @param pos Position of block.
     * @param filterOnBlockFacing If true, the axis of the block's DirectionalBlock.FACING will not be returned from
     *                            this function. The block should have this property if this is true, of course!
     * @return Any Direction, representing what direction the block should be facing based on the player's position.
     */
    public static Direction getForwardFromPlayerUpAndDownFilterBlockFacing(Player player, BlockPos pos, boolean filterOnBlockFacing) {
        Direction.Axis filter = filterOnBlockFacing ? player.level().getBlockState(pos).getValue(DirectionalBlock.FACING).getAxis() : null;
        Vec3 playerPos = player.position();
        if (playerPos.y >= pos.getY() + 0.625 && filter != Direction.Axis.Y) {
            return Direction.UP;
        } else if (playerPos.y <= pos.getY() - 0.625 && filter != Direction.Axis.Y) {
            return Direction.DOWN;
        } else {
            Direction forward = getForwardFromPlayer(player, pos);
            if (forward.getAxis() != filter) {
                return forward;
            } else {
                // We filter on non-Y axis, and getForwardFromPlayer was on our filter. Find the closest and get it.
                Direction blockFacing = player.level().getBlockState(pos).getValue(DirectionalBlock.FACING);
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                Direction blockLeftDir = blockFacing.getCounterClockWise();
                Vec3 blockLeftVec = new Vec3(blockLeftDir.getNormal().getX(), blockLeftDir.getNormal().getY(), blockLeftDir.getNormal().getZ());
                Vec3 counterClockwisePos = blockCenter.add(blockLeftVec.scale(0.5));
                Vec3 clockwisePos = blockCenter.add(blockLeftVec.scale(-0.5));
                Vec3 upPos = blockCenter.add(0, 0.5, 0);
                Vec3 downPos = blockCenter.add(0, -0.5, 0);

                double counterClockwiseDist = counterClockwisePos.distanceToSqr(playerPos);
                double clockwiseDist = clockwisePos.distanceToSqr(playerPos);
                double upDist = upPos.distanceToSqr(playerPos);
                double downDist = downPos.distanceToSqr(playerPos);

                double min = Math.min(counterClockwiseDist, clockwiseDist);
                min = Math.min(min, upDist);
                min = Math.min(min, downDist);

                if (min == counterClockwiseDist) {
                    return forward.getCounterClockWise();
                } else if (min == clockwiseDist) {
                    return forward.getClockWise();
                } else if (min == upDist) {
                    return Direction.UP;
                } else {
                    return Direction.DOWN;
                }
            }
        }
    }

}
