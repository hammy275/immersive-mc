package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.ForcedUpDownRenderDir;
import com.hammy275.immersivemc.api.client.immersive.HitboxPositioningMode;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.RelativeHitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.LastClientVRData;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfoImpl;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RelativeHitboxInfoImpl implements RelativeHitboxInfo, HitboxInfo, Cloneable {

    // Settings
    private final RelativeHitboxInfoBuilderImpl usedBuilder;

    public final Function<BuiltImmersiveInfoImpl<?>, Vec3> centerOffset;
    public final double sizeX;
    public final double sizeY;
    public final double sizeZ;
    public final boolean isInput;
    public final boolean holdsItems;
    public final boolean itemSpins;
    public final float itemRenderSizeMultiplier;
    public final boolean isTriggerHitbox;
    public final Function<BuiltImmersiveInfoImpl<?>, List<Pair<Component, Vec3>>> textSupplier;
    public final ForcedUpDownRenderDir forcedUpDownRenderDir;
    // Not directly configured by programmers. This is whether the offset from centerOffset returns a constant value.
    public final boolean constantOffset;
    public final boolean needs3dCompat;
    public final HitboxVRMovementInfo vrMovementInfo;
    public final boolean renderItem;
    public final boolean renderItemCount;

    // Calculated data to be returned out
    private AABB box;
    private Vec3 pos;
    private boolean didCalc = false; // One-time flag to make sure recalculate() is called before getting data.
    private final List<TextData> textData = new ArrayList<>();
    private Direction upDownRenderDir = null;

    // Calculated data used across functions internally to this class. Easier than passing parameters everywhere.
    private Vec3 xVec;
    private Vec3 yVec;
    private Vec3 zVec;
    private Vec3 centerPos;

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    /**
     * Constructor. See the respective functions in {@link RelativeHitboxInfoBuilderImpl} for more information.
     * @param usedBuilder The builder used to create this HitboxInfo. Only used to allow cloning this hitbox.
     * @param centerOffset Function that takes an info instance and returns the offset from the center position for the
     *                     location of this hitbox.
     * @param sizeX Size of hitbox on relative X axis.
     * @param sizeY Size of hitbox on relative Y axis.
     * @param sizeZ Size of hitbox on relative Z axis.
     * @param holdsItems Whether this hitbox holds items.
     * @param isInput Whether this hitbox is an input hitbox.
     * @param itemSpins Whether the item in this hitbox should spin.
     * @param itemRenderSizeMultiplier Multiplier to the size passed in the {@link ImmersiveBuilderImpl} for the size the
     *                                 item should render at.
     * @param isTriggerHitbox Whether this hitbox is a trigger hitbox.
     * @param textSupplier A function taking an info instance and returning a list of text components.
     * @param forcedUpDownDir An override for upDownRenderDir to use instead of automatically determining it.
     * @param constantOffset Whether centerOffset's return value is constant.
     * @param needs3dCompat Whether the offset should be moved by a need for compatibility with 3D resource packs.
     * @param vrMovementInfo Hitbox VR movement info to recognize hand gestures and perform some action.
     * @param renderItem Whether to render the item in this hitbox if it can contain one.
     * @param renderItemCount Whether to render the item count in this hitbox if it can contain an item.
     */
    public RelativeHitboxInfoImpl(RelativeHitboxInfoBuilderImpl usedBuilder,
                                  Function<BuiltImmersiveInfoImpl<?>, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ,
                                  boolean holdsItems, boolean isInput, boolean itemSpins, float itemRenderSizeMultiplier,
                                  boolean isTriggerHitbox, Function<BuiltImmersiveInfoImpl<?>, List<Pair<Component, Vec3>>> textSupplier,
                                  ForcedUpDownRenderDir forcedUpDownDir, boolean constantOffset, boolean needs3dCompat,
                                  HitboxVRMovementInfo vrMovementInfo, boolean renderItem, boolean renderItemCount) {
        this.usedBuilder = usedBuilder;
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.holdsItems = holdsItems;
        this.isInput = isInput;
        this.itemSpins = itemSpins;
        this.itemRenderSizeMultiplier = itemRenderSizeMultiplier;
        this.isTriggerHitbox = isTriggerHitbox;
        this.textSupplier = textSupplier;
        this.forcedUpDownRenderDir = forcedUpDownDir;
        this.constantOffset = constantOffset;
        this.needs3dCompat = needs3dCompat;
        this.vrMovementInfo = vrMovementInfo;
        this.renderItem = renderItem;
        this.renderItemCount = renderItemCount;
    }

    /**
     * Force the position and box of this hitbox to be null.
     */
    public void forceNull() {
        this.pos = null;
        this.box = null;
        this.didCalc = true;
        this.textData.clear();
        this.upDownRenderDir = null;
    }

    /**
     * Main external function call. Calculates the actual, in-world data from the relative information provided.
     * @param level Level instance.
     * @param mode Positioning mode. See {@link HitboxPositioningMode} for what each mode does.
     * @param info Info instance.
     */
    public void recalculate(Level level, HitboxPositioningMode mode, BuiltImmersiveInfoImpl<?> info) {
        Vec3 offset = this.centerOffset.apply(info);
        if (offset == null) {
            forceNull();
            return; // Bail early if we don't actually have a position to work with
        }
        // Offset a bit if we have 3D resource pack compat enabled and this hitbox declares wanting it.
        if (ActiveConfig.active().resourcePack3dCompat && needs3dCompat) {
            offset = offset.add(0, 0, 1d/16d);
        }
        BlockPos pos = info.getBlockPosition();
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcHorizBlockFacing(blockFacing, info, offset);
            upDownRenderDir = null;
        } else if (mode == HitboxPositioningMode.TOP_PLAYER_FACING) {
            recalcTopPlayerFacing(info.immersiveDir, info, offset);
            upDownRenderDir = Direction.UP;
        } else if (mode == HitboxPositioningMode.TOP_LITERAL) {
            xVec = new Vec3(1, 0, 0);
            yVec = new Vec3(0, 1, 0);
            zVec = new Vec3(0, 0, 1);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            double actualXSize = this.sizeX;
            double actualYSize = this.sizeY;
            double actualZSize = this.sizeZ;

            this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
            this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
            upDownRenderDir = Direction.UP;
        } else if (mode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcTopBottomBlockFacing(blockFacing, info, offset, false);
            upDownRenderDir = Direction.UP;
        } else if (mode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            Direction blockFacing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player, info.getBlockPosition());
            recalcHorizBlockFacing(blockFacing, info, offset);
            upDownRenderDir = null;
        } else if (mode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
            // Delegate to other calculation modes
            Direction blockFacing = level.getBlockState(pos).getValue(DirectionalBlock.FACING);
            if (blockFacing.getAxis() != Direction.Axis.Y) {
                recalcHorizBlockFacing(blockFacing, info, offset);
                upDownRenderDir = null;
            } else {
                // Pretend the block is facing south so that way west becomes +x
                recalcTopBottomBlockFacing(Direction.SOUTH, info, offset, blockFacing == Direction.DOWN);
                upDownRenderDir = blockFacing;
            }
        } else if (mode == HitboxPositioningMode.PLAYER_FACING_NO_DOWN) {
            Direction playerFacing = AbstractImmersive.getForwardFromPlayerUpAndDown(Minecraft.getInstance().player, info.getBlockPosition());
            if (playerFacing == Direction.UP) {
                recalcTopPlayerFacing(info.immersiveDir, info, offset);
                upDownRenderDir = Direction.UP;
            } else {
                Direction blockFacing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player, info.getBlockPosition());
                recalcHorizBlockFacing(blockFacing, info, offset);
                upDownRenderDir = null;
            }
        } else if (mode == HitboxPositioningMode.PLAYER_FACING_FILTER_BLOCK_FACING) {
            Direction dir = info.immersiveDir; // Direction the block should be "facing" to the player
            Direction literalFacing = level.getBlockState(pos).getValue(DirectionalBlock.FACING); // The direction the block is actually facing
            if (dir.getAxis() == Direction.Axis.Y) {
                recalcTopBottomBlockFacing(literalFacing, info, offset, dir == Direction.DOWN);
            } else {
                recalcHorizBlockFacing(dir, info, offset, literalFacing);
                upDownRenderDir = null;
            }
        } else {
            throw new UnsupportedOperationException("Hitbox calculation for positioning mode " + mode + " unimplemented!");
        }
        calcTextOffsets(info);
        if (forcedUpDownRenderDir != ForcedUpDownRenderDir.NOT_FORCED) {
            upDownRenderDir = forcedUpDownRenderDir.direction;
        }
        // Detect VR hand movements and run callback
        if (vrMovementInfo != null && VRPluginVerify.clientInVR()) {
            boolean[] passed = {false, false};
            for (int c = 0; c <= 1; c++) {
                if (!LastClientVRData.canGetVelocityChange()) continue;
                if (!box.contains(VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c).position())) continue;
                Vec3 velocity = LastClientVRData.changeForVelocity(c == 0 ? LastClientVRData.VRType.C0 : LastClientVRData.VRType.C1);
                if (vrMovementInfo.relativeAxis() == null) {
                    passed[c] = velocity.lengthSqr() >= vrMovementInfo.thresholds()[0] * vrMovementInfo.thresholds()[0];
                } else {
                    double posThreshold;
                    double negThreshold;
                    if (vrMovementInfo.thresholds().length == 2) {
                        posThreshold = Math.max(vrMovementInfo.thresholds()[0], vrMovementInfo.thresholds()[1]);
                        negThreshold = Math.min(vrMovementInfo.thresholds()[0], vrMovementInfo.thresholds()[1]);
                    } else {
                        posThreshold = Math.max(vrMovementInfo.thresholds()[0], 0);
                        negThreshold = Math.min(vrMovementInfo.thresholds()[0], 0);
                    }
                    // We have a relative axis. Let's "mask out" the other axes, then do our threshold check.
                    Vec3 velocityMask = vrMovementInfo.relativeAxis() == Direction.Axis.X ? xVec :
                            vrMovementInfo.relativeAxis() == Direction.Axis.Y ? yVec : zVec;
                    if (posThreshold != 0) {
                        passed[c] = velocity.multiply(velocityMask).lengthSqr() >= posThreshold * posThreshold;
                    }
                    if (negThreshold != 0) {
                        passed[c] = passed[c] || velocity.multiply(velocityMask).lengthSqr() <= negThreshold * negThreshold;
                    }
                }
            }
            boolean passedOverall;
            switch (vrMovementInfo.controllerMode()) {
                case C0 -> passedOverall = passed[0];
                case C1 -> passedOverall = passed[1];
                case EITHER -> passedOverall = passed[0] || passed[1];
                case BOTH -> passedOverall = passed[0] && passed[1];
                default -> throw new IllegalArgumentException("Invalid controllerMOde for HitboxVRMovementInfo.");
            }
            if (passedOverall) {
                vrMovementInfo.action().accept(info);
            }
        }
        didCalc = true;
    }

    /**
     * See below docstring for recalcHorizBlockFacing().
     */
    private void recalcHorizBlockFacing(Direction blockFacing, BuiltImmersiveInfoImpl<?> info, Vec3 offset) {
        recalcHorizBlockFacing(blockFacing, info, offset, Direction.UP);
    }

    /**
     * Helper function for recalculate() for blocks facing a horizontal direction.
     * @param blockFacing Direction the block is facing towards the player.
     * @param info Info instance.
     * @param offset Relative offset from the center of the block. X and Y represent a horizontal grid, while Z
     *               goes into/out of the block.
     * @param blockLiteralFacing The direction the block is facing in the world, or more precisely,
     *                           the direction that +Y should be.
     */
    private void recalcHorizBlockFacing(Direction blockFacing, BuiltImmersiveInfoImpl<?> info, Vec3 offset, Direction blockLiteralFacing) {
        BlockPos pos = info.getBlockPosition();

        // Vectors that are combined with centerOffset. May not necessarily correspond to the actual in-game axis.
        // For example, zVec corresponds always to the in-game Y-axis for PLAYER_FACING (such as crafting tables).
        if (blockLiteralFacing.getAxis() != Direction.Axis.Y) {
            xVec = new Vec3(0, 1, 0);
        } else {
            xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
        }
        yVec = Vec3.atLowerCornerOf(blockLiteralFacing.getNormal());
        zVec = Vec3.atLowerCornerOf(blockFacing.getNormal());

        centerPos = Vec3.atCenterOf(info.getBlockPosition()).add(zVec.scale(0.5));

        // If, for example, the furnace is facing the X-axis, then the size should come from sizeZ, since the
        // Z size represents going "into"/"out of" the furnace (as the X size is for left and right on the furnace's
        // face, and the y size is for going up or down the face).
        // The actual hitbox size on the in-game x, y, and z axis.
        double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
        double actualYSize = this.sizeY;
        double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeZ;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    /**
     * Helper function for recalculate().
     */
    private void recalcTopBottomBlockFacing(Direction blockFacing, BuiltImmersiveInfoImpl<?> info, Vec3 offset, boolean bottomOfBlock) {

        BlockPos pos = info.getBlockPosition();

        xVec = Vec3.atLowerCornerOf(blockFacing.getClockWise().getNormal());
        yVec = Vec3.atLowerCornerOf(blockFacing.getNormal());
        zVec = new Vec3(0, 1, 0);

        centerPos = Vec3.atBottomCenterOf(pos).add(0, bottomOfBlock ? 0 : 1, 0);

        double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
        double actualYSize = this.sizeZ;
        double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    private void recalcTopPlayerFacing(Direction playerFacing, BuiltImmersiveInfoImpl<?> info, Vec3 offset) {
        BlockPos pos = info.getBlockPosition();
        xVec = Vec3.atLowerCornerOf(playerFacing.getCounterClockWise().getNormal());
        yVec = Vec3.atLowerCornerOf(playerFacing.getOpposite().getNormal());
        zVec = new Vec3(0, 1, 0);

        centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

        double actualXSize = playerFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
        double actualYSize = this.sizeZ;
        double actualZSize = playerFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    /**
     * Helper function for recalculate(). Calculates the textData list.
     */
    private void calcTextOffsets(BuiltImmersiveInfoImpl<?> info) {
        if (textSupplier != null) {
            List<Pair<Component, Vec3>> textList = textSupplier.apply(info);
            textData.clear();
            if (textList != null) {
                for (Pair<Component, Vec3> pair : textList) {
                    if (pair != null) {
                        textData.add(new TextData(
                                pair.getFirst(),
                                this.pos.add(xVec.scale(pair.getSecond().x)).add(yVec.scale(pair.getSecond().y)).add(zVec.scale(pair.getSecond().z))
                        ));
                    }
                }
            }
        }
    }

    /**
     * @return Literal AABB for this hitbox.
     */
    public AABB getAABB() {
        if (!didCalc) {
            throw new IllegalStateException("Should call recalculate() or forceNull() before getting hitbox.");
        }
        return box;
    }

    /**
     * @return Literal position for this hitbox.
     */
    public Vec3 getPos() {
        if (!didCalc) {
            throw new IllegalStateException("Should call recalculate() or forceNull() before getting position.");
        }
        return pos;
    }

    public Direction getUpDownRenderDir() {
        if (!didCalc) {
            throw new IllegalStateException("Should call recalculate() or forceNull() before getting upDownRenderDir.");
        }
        return upDownRenderDir;
    }

    /**
     * @return Whether this has its calculated position.
     */
    public boolean hasPos() {
        return pos != null;
    }

    /**
     * @return Whether this has its calculated hitbox.
     */
    public boolean hasAABB() {
        return box != null;
    }

    public List<TextData> getTextData() {
        return this.textData;
    }

    /**
     * @return Whether the relative to literal calculation has been performed at least one.
     */
    public boolean calcDone() {
        return didCalc;
    }

    /**
     * @return A clone of this info instance.
     */
    @Override
    public Object clone() {
        return cloneWithNewOffset(centerOffset);
    }

    /**
     * Clones this HitboxInfo replacing the offset with the supplied one.
     * @param newOffset New offset for the clone.
     * @return Clone with the offset replaced with newOffset.
     */
    public RelativeHitboxInfoImpl cloneWithNewOffset(Function<BuiltImmersiveInfoImpl<?>, Vec3> newOffset) {
        return getBuilderClone().setCenterOffset(newOffset).build();
    }

    /**
     * Clones this HitboxInfo, offsetting the position by the supplied offset.
     * @param offset Offset relative to this HitboxInfo's offset.
     * @return HitboxInfo that is offset by offset in comparison to this HitboxInfo.
     */
    public RelativeHitboxInfoImpl cloneWithAddedOffset(Vec3 offset) {
        return getBuilderClone().setCenterOffset((info) -> {
            Vec3 offsetOut = centerOffset.apply(info);
            if (offsetOut == null) {
                return null;
            }
            return offsetOut.add(offset);
        }).build();
    }

    public RelativeHitboxInfoBuilderImpl getBuilderClone() {
        return usedBuilder.clone();
    }

    @Override
    public BoundingBox getHitbox() {
        return hasAABB() ? getAABB() : null;
    }

    @Override
    public boolean isTriggerHitbox() {
        return this.isTriggerHitbox;
    }
}
