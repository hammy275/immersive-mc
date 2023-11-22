package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
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

public class HitboxInfo implements Cloneable {

    // Settings
    public final Function<BuiltImmersiveInfo, Vec3> centerOffset;
    public final double sizeX;
    public final double sizeY;
    public final double sizeZ;
    public final boolean isInput;
    public final boolean holdsItems;
    public final Direction upDownRenderDir;
    public final boolean itemSpins;
    public final float itemRenderSizeMultiplier;
    public final boolean isTriggerHitbox;
    public final Function<BuiltImmersiveInfo, List<Pair<Component, Vec3>>> textSupplier;

    // Calculated data to be returned out
    private AABB box;
    private Vec3 pos;
    boolean didCalc = false; // One-time flag to make sure recalculate() is called before getting data.
    final List<TextData> textData = new ArrayList<>();

    // Calculated data used across functions internally to this class. Easier than passing parameters everywhere.
    private Vec3 xVec;
    private Vec3 yVec;
    private Vec3 zVec;
    private Vec3 centerPos;

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    /**
     * Constructor. See the respective functions in {@link HitboxInfoBuilder} for more information.
     * @param centerOffset Function that takes an info instance and returns the offset from the center position for the
     *                     location of this hitbox.
     * @param sizeX Size of hitbox on relative X axis.
     * @param sizeY Size of hitbox on relative Y axis.
     * @param sizeZ Size of hitbox on relative Z axis.
     * @param holdsItems Whether this hitbox holds items.
     * @param isInput Whether this hitbox is an input hitbox.
     * @param upDownRenderDir Direction for item rotation when this hitbox renders facing the sky or ground.
     * @param itemSpins Whether the item in this hitbox should spin.
     * @param itemRenderSizeMultiplier Multiplier to the size passed in the {@link ImmersiveBuilder} for the size the
     *                                 item should render at.
     * @param isTriggerHitbox Whether this hitbox is a trigger hitbox.
     * @param textSupplier A function taking an info instance and returning a list of text components.
     */
    public HitboxInfo(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ,
                      boolean holdsItems, boolean isInput,
                      Direction upDownRenderDir, boolean itemSpins, float itemRenderSizeMultiplier,
                      boolean isTriggerHitbox, Function<BuiltImmersiveInfo, List<Pair<Component, Vec3>>> textSupplier) {
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.holdsItems = holdsItems;
        this.isInput = isInput;
        this.upDownRenderDir = upDownRenderDir;
        this.itemSpins = itemSpins;
        this.itemRenderSizeMultiplier = itemRenderSizeMultiplier;
        this.isTriggerHitbox = isTriggerHitbox;
        this.textSupplier = textSupplier;
    }

    /**
     * Force the position and box of this hitbox to be null.
     */
    public void forceNull() {
        this.pos = null;
        this.box = null;
        this.didCalc = true;
        this.textData.clear();
    }

    /**
     * Main external function call. Calculates the actual, in-world data from the relative information provided.
     * @param level Level instance.
     * @param mode Positioning mode. See {@link HitboxPositioningMode} for what each mode does.
     * @param info Info instance.
     */
    public void recalculate(Level level, HitboxPositioningMode mode, BuiltImmersiveInfo info) {
        Vec3 offset = this.centerOffset.apply(info);
        if (offset == null) {
            forceNull();
            return; // Bail early if we don't actually have a position to work with
        }
        BlockPos pos = info.getBlockPosition();
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcHorizBlockFacing(blockFacing, info, offset);
        } else if (mode == HitboxPositioningMode.PLAYER_FACING) {
            Direction blockFacing = Minecraft.getInstance().player.getDirection();
            xVec = Vec3.atLowerCornerOf(blockFacing.getClockWise().getNormal());
            yVec = Vec3.atLowerCornerOf(blockFacing.getNormal());
            zVec = new Vec3(0, 1, 0);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
            double actualYSize = this.sizeZ;
            double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;

            this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
            this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
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
        } else if (mode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcTopBottomBlockFacing(blockFacing, info, offset, false);
        } else if (mode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            Direction blockFacing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
            recalcHorizBlockFacing(blockFacing, info, offset);
        } else if (mode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
            // Delegate to other calculation modes
            Direction blockFacing = level.getBlockState(pos).getValue(DirectionalBlock.FACING);
            if (blockFacing.getAxis() != Direction.Axis.Y) {
                recalcHorizBlockFacing(blockFacing, info, offset);
            } else {
                // Pretend the block is facing west so that way west becomes +x
                recalcTopBottomBlockFacing(Direction.WEST, info, offset, blockFacing == Direction.DOWN);
            }
        } else {
            throw new UnsupportedOperationException("Hitbox calculation for positioning mode " + mode + " unimplemented!");
        }
        calcTextOffsets(info);
        didCalc = true;
    }

    /**
     * Helper function for recalculate().
     */
    private void recalcHorizBlockFacing(Direction blockFacing, BuiltImmersiveInfo info, Vec3 offset) {
        BlockPos pos = info.getBlockPosition();

        // Vectors that are combined with centerOffset. May not necessarily correspond to the actual in-game axis.
        // For example, zVec corresponds always to the in-game Y-axis for PLAYER_FACING (such as crafting tables).
        xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
        yVec = new Vec3(0, 1, 0);
        zVec = Vec3.atLowerCornerOf(blockFacing.getNormal());

        centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                .add(xVec.scale(0.5)).add(yVec.scale(0.5));

        // If, for example, the furnace is facing the X-axis, then the size should come from sizeZ, since the
        // Z size represents going "into"/"out of" the furnace (as the X size is for left and right on the furnace's
        // face, and the y size is for going up or down the face).
        // The actual hitbox size on the in-game x, y, and z axis.
        double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
        double actualYSize = this.sizeY;
        double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    /**
     * Helper function for recalculate().
     */
    private void recalcTopBottomBlockFacing(Direction blockFacing, BuiltImmersiveInfo info, Vec3 offset, boolean bottomOfBlock) {

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

    /**
     * Helper function for recalculate(). Calculates the textData list.
     */
    private void calcTextOffsets(BuiltImmersiveInfo info) {
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
    public HitboxInfo cloneWithNewOffset(Function<BuiltImmersiveInfo, Vec3> newOffset) {
        return new HitboxInfo(newOffset, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }

    /**
     * Clones this HitboxInfo, offsetting the position by the supplied offset.
     * @param offset Offset relative to this HitboxInfo's offset.
     * @return HitboxInfo that is offset by offset in comparison to this HitboxInfo.
     */
    public HitboxInfo cloneWithAddedOffset(Vec3 offset) {
        return new HitboxInfo((info) -> {
            Vec3 offsetOut = centerOffset.apply(info);
            if (offsetOut == null) {
                return null;
            }
            return offsetOut.add(offset);
        }, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }
}
