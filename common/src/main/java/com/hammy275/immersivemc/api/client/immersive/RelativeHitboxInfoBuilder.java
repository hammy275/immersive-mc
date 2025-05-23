package com.hammy275.immersivemc.api.client.immersive;

import com.google.common.annotations.Beta;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.RelativeHitboxInfoBuilderImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * A builder for {@link RelativeHitboxInfo} instances. For all {@link BuiltImmersiveInfo} instances provided by this
 * class, {@link BuiltImmersiveInfo#getExtraData()} is safe to use, and will give an object with your valid data. You
 * only need to cast it to your object type.
 */
public interface RelativeHitboxInfoBuilder {

    /**
     * Set the offset from the starting position for this relative hitbox. See {@link HitboxPositioningMode} for more
     * info on how this positioning works.
     * @param newOffset The offset in blocks from the center this hitbox should be placed at.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder setCenterOffset(Vec3 newOffset);

    /**
     * Set the offset from the starting position for this relative hitbox. See {@link HitboxPositioningMode} for more
     * info on how this positioning works.
     * @param newOffset A function that returns the offset in blocks from the center this hitbox should be placed at.
     *                 The function can return null if this hitbox shouldn't render or be interacted with.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder setCenterOffset(Function<BuiltImmersiveInfo<?>, Vec3> newOffset);

    /**
     * @param holdsItems Whether this relative hitbox should hold items.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder holdsItems(boolean holdsItems);

    /**
     * @param isInput Relative this relative hitbox is an input. If this relative hitbox is both an input and holds
     *                items, it will render an item guide.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder isInput(boolean isInput);

    /**
     * @param spins Whether the item in this hitbox should rotate when displayed. This only matters if this hitbox
     *              already holds items.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder itemSpins(boolean spins);

    /**
     * @param multiplier A multiplier for the size of the item when rendering.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder itemRenderSizeMultiplier(float multiplier);

    /**
     * @param isTriggerHitbox Whether this hitbox should act as a trigger hitbox, meaning it will require the break
     *                        block button to pressed when a controller is inside of it in VR.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder triggerHitbox(boolean isTriggerHitbox);

    /**
     * @param textSupplier A function that provides a list of text components to render and the relative offset from
     *                     the center offset to render the text at.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder textSupplier(Function<BuiltImmersiveInfo<?>, List<Pair<Component, Vec3>>> textSupplier);

    /**
     * @param forcedDir Forces the direction passed to upDown {@link com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers#renderItem(ItemStack, PoseStack, float, BoundingBox, boolean, int, Float, Direction, Direction)}
     *                  instead of determining it based on the hitbox positioning mode.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder forceUpDownRenderDir(ForcedUpDownRenderDir forcedDir);

    /**
     * @param forcedDirFunction Forces the direction passed to upDown {@link com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers#renderItem(ItemStack, PoseStack, float, BoundingBox, boolean, int, Float, Direction, Direction)}
     *                  instead of determining it based on the hitbox positioning mode.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder forceUpDownRenderDir(Function<BuiltImmersiveInfo<?>, ForcedUpDownRenderDir> forcedDirFunction);

    /**
     * @param needs3dCompat Whether this hitbox should be moved on the Z axis for 3D resource packs.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder needs3DResourcePackCompat(boolean needs3dCompat);

    /**
     * @param vrMovementInfo How this hitbox should react to VR hand movements.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder setVRMovementInfo(HitboxVRMovementInfo vrMovementInfo);

    /**
     * @param renderItem Whether this hitbox should render the item it contains, if it contains one.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder renderItem(boolean renderItem);

    /**
     * @param renderItemCount Whether this hitbox should render the item count if it's rendering an item.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder renderItemCount(boolean renderItemCount);

    /**
     * @param lerps Whether this hitbox should lerp (perform linearly interpolation) when moving to animate smoothly.
     * @return This builder object.
     */
    public RelativeHitboxInfoBuilder lerps(boolean lerps);

    /**
     * @param rotationType The type of rotation to apply when rendering an item in this hitbox, or null for no rotation.
     * @return This builder object.
     */
    @Beta
    public RelativeHitboxInfoBuilder rotateItem(@Nullable ItemRotationType rotationType);

    /**
     * Build this builder into a proper relative hitbox.
     * @return A built relative hitbox.
     */
    public RelativeHitboxInfo build();

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size.
     * @param centerOffset The relative offset from the center.
     * @param size The size of this hitbox, in blocks.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder create(Vec3 centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size);
    }

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size.
     * @param centerOffset The function to calculate the relative offset from the center. The function return null to
     *                     denote no hitbox.
     * @param size The size of this hitbox, in blocks.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder create(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size, false);
    }

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size.
     * @param centerOffset The relative offset from the center.
     * @param sizeX The size of this box on the relative x-axis.
     * @param sizeY The size of this box on the relative y-axis.
     * @param sizeZ The size of this box on the relative z-axis.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder create(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, sizeX, sizeY, sizeZ);
    }

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size.
     * @param centerOffset The function to calculate the relative offset from the center. The function can return null
     *                     to denote no hitbox.
     * @param sizeX The size of this box on the relative x-axis.
     * @param sizeY The size of this box on the relative y-axis.
     * @param sizeZ The size of this box on the relative z-axis.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder create(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, sizeX, sizeY, sizeZ, false);
    }

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size that accepts items from players.
     * @param centerOffset The relative offset from the center.
     * @param size The size of this hitbox, in blocks.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder createItemInput(Vec3 centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size).holdsItems(true).isInput(true);
    }

    /**
     * Create a RelativeHitboxInfoBuilder offset by some amount with some size that accepts items from players.
     * @param centerOffset The function to calculate the relative offset from the center. The function can return null
     *                     to denote no hitbox.
     * @param size The size of this hitbox, in blocks.
     * @return A builder object.
     */
    public static RelativeHitboxInfoBuilder createItemInput(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size, false).holdsItems(true).isInput(true);
    }
}
