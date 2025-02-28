package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Contains several methods to help with rendering Immersives into the world.
 */
public interface ImmersiveRenderHelpers {

    /**
     * @return An ImmersiveRenderHelpers instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveRenderHelpers instance() {
        return ImmersiveRenderHelpersImpl.INSTANCE;
    }

    /**
     * Renders an item based on an ImmersiveInfo, or an item guide if applicable. As of writing, items
     * will grow when hovered-over/selected, will shrink for the start of the info's existence, and an item guide
     * will render if there is no item and
     * @param item The item to render.
     * @param stack The pose stack being rendered with.
     * @param size The size of the item to render before automatic changes from ImmersiveMC
     * @param renderItemCounts Whether to render a number representing the item count with the item.
     * @param light The packed sky light and block light to render with.
     * @param info The {@link ImmersiveInfo} that is rendering this item.
     * @param shouldRenderItemGuide Whether this slot should render an item guide if other conditions are met to do so.
     * @param hitboxIndex The index into {@link ImmersiveInfo#getAllHitboxes()} that is being rendered.
     * @param spinDegrees The number of degrees to spin on x/z. Ignored if null.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN. Ignored if spinDegrees is at least 0.
     * @param upDown Direction upwards or downwards for the item to face. Can be null if not facing up or down. If this
     *               is not null, facing instead controls the direction the item is rotated towards.
     */
    public void renderItemWithInfo(@Nullable ItemStack item, PoseStack stack, float size, boolean renderItemCounts,
                                   int light, ImmersiveInfo info, boolean shouldRenderItemGuide, int hitboxIndex,
                                   @Nullable Float spinDegrees, @Nullable Direction facing, @Nullable Direction upDown);

    /**
     * Renders an item at the specified position facing the camera.
     *
     * @param item The item to render.
     * @param stack The pose stack being rendered with.
     * @param size The size to render at.
     * @param hitbox The hitbox for interacting with this item. Used for displaying a hitbox when hitboxes are enabled.
     * @param renderItemCounts Whether to render a number representing the item count with the item.
     * @param light The packed sky light and block light to render with.
     */
    public void renderItem(@Nullable ItemStack item, PoseStack stack, float size, BoundingBox hitbox,
                           boolean renderItemCounts, int light);

    /**
     * Renders an item at the specified position.
     *
     * @param item The item to render.
     * @param stack The pose stack being rendered with.
     * @param size The size to render at.
     * @param hitbox The hitbox for interacting with this item. Used for displaying a hitbox when hitboxes are enabled.
     * @param renderItemCounts Whether to render a number representing the item count with the item.
     * @param light The packed sky light and block light to render with.
     * @param spinDegrees The number of degrees to spin on x/z. Ignored if null.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN. Ignored if spinDegrees is at least 0.
     * @param upDown Direction upwards or downwards for the item to face. Can be null if not facing up or down. If this
     *               is not null, facing instead controls the direction the item is rotated towards.
     */
    public void renderItem(@Nullable ItemStack item, PoseStack stack, float size, BoundingBox hitbox,
                           boolean renderItemCounts, int light, @Nullable Float spinDegrees, @Nullable Direction facing,
                           @Nullable Direction upDown);

    /**
     * Renders an item guide. Any place where items can be input should call this method if there isn't an item there.
     * <br>
     * Technically, this doesn't immediately render the item guide, but renders it after all Immersives have already
     * rendered. This way, transparency happens properly.
     * @param stack Pose stack to render with.
     * @param hitbox The hitbox defining the item guide.
     * @param isSelected Whether the item guide is currently selected/hovered over.
     * @param light The packed sky light and block light to render with.
     */
    public void renderItemGuide(PoseStack stack, BoundingBox hitbox, boolean isSelected, int light);

    /**
     * Render a white, solid, hitbox in the world if showing hitboxes.
     * @param stack The pose stack being rendered with.
     * @param hitbox The hitbox to render.
     */
    public void renderHitbox(PoseStack stack, BoundingBox hitbox);

    /**
     * Render a white, solid, hitbox in the world.
     * @param stack The pose stack being rendered with.
     * @param hitbox The hitbox to render.
     * @param alwaysRender If true, the hitbox will always render. If false, the hitbox will only render if hitbox
     *                     rendering is enabled.
     */
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender);

    /**
     * Render a solid hitbox in the world.
     * @param stack The pose stack being rendered with.
     * @param hitbox The hitbox to render.
     * @param alwaysRender If true, the hitbox will always render. If false, the hitbox will only render if hitbox
     *                     rendering is enabled.
     * @param red The amount of red to color this hitbox with. Should be between 0 and 1 inclusive.
     * @param green The amount of green to color this hitbox with. Should be between 0 and 1 inclusive.
     * @param blue The amount of blue to color this hitbox with. Should be between 0 and 1 inclusive.
     */
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue);

    /**
     * Render a hitbox in the world.
     * @param stack The pose stack being rendered with.
     * @param hitbox The hitbox to render.
     * @param alwaysRender If true, the hitbox will always render. If false, the hitbox will only render if hitbox
     *                     rendering is enabled.
     * @param red The amount of red to color this hitbox with. Should be between 0 and 1 inclusive.
     * @param green The amount of green to color this hitbox with. Should be between 0 and 1 inclusive.
     * @param blue The amount of blue to color this hitbox with. Should be between 0 and 1 inclusive.
     * @param alpha The alpha value for the hitbox. 0 makes the hitbox lines completely invisible, and 1 makes them
     *              not transparent at all. Should be between 0 and 1 inclusive.
     */
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue, float alpha);

    /**
     * Render some text in the world.
     *
     * @param text The text to render in the world.
     * @param stack The pose stack being rendered with.
     * @param pos The world position to render the text at.
     * @param light The packed sky light and block light to render with.
     * @param textSize The text size to render with. ImmersiveMC uses 0.02 for this value in most cases.
     */
    public void renderText(Component text, PoseStack stack, Vec3 pos, int light, float textSize);

    /**
     * Render an image in the world.
     * @param stack The pose stack being rendered with.
     * @param imageLocation The location to the image PNG file.
     * @param pos The position in the world to render at.
     * @param size The size to render this image at.
     * @param light The packed sky light and block light to render with.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN.
     */
    public void renderImage(PoseStack stack, ResourceLocation imageLocation, Vec3 pos, float size, int light,
                            @Nullable Direction facing);

    /**
     * Render an image in the world.
     * @param stack The pose stack being rendered with.
     * @param imageLocation The location to the image PNG file.
     * @param minImageU Minimum U coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param minImageV Minimum V coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param maxImageU Maximum U coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param maxImageV Maximum V coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param pos The position in the world to render at.
     * @param size The size to render this image at.
     * @param light The packed sky light and block light to render with.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN.
     */
    public void renderImage(PoseStack stack, ResourceLocation imageLocation,
                            float minImageU, float minImageV, float maxImageU, float maxImageV,
                            Vec3 pos, float size, int light,
                            @Nullable Direction facing);

    /**
     * Render an image in the world.
     * @param stack The pose stack being rendered with.
     * @param imageLocation The location to the image PNG file.
     * @param minImageU Minimum U coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param minImageV Minimum V coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param maxImageU Maximum U coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param maxImageV Maximum V coordinate in the image. Should be in the range [0.0f, 1.0f]
     * @param pos The position in the world to render at.
     * @param size The size to render this image at.
     * @param light The packed sky light and block light to render with.
     * @param roll The roll to render with, in degrees.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN.
     */
    public void renderImage(PoseStack stack, ResourceLocation imageLocation,
                            float minImageU, float minImageV, float maxImageU, float maxImageV,
                            Vec3 pos, float size, int light, float roll,
                            @Nullable Direction facing);

    /**
     * Returns a float between 0 and 1 denoting the multiplier to the size of something being rendered that ImmersiveMC
     * uses to create the initial "growing" animation. If you want to create such an animation, or are using a function
     * that doesn't already handle it for you, use this in case the formula ImmersiveMC uses changes later.
     * @param ticksExisted The amount of ticks your Immersive has existed.
     * @return The aforementioned float.
     */
    public float getTransitionMultiplier(long ticksExisted);

    /**
     * Gets the multiplier to apply to the size of an item to attach when the item is being hovered over. For example,
     * if you want to render an item at some size n, you should render it as size
     * n * {@link #getTransitionMultiplier(long)} * hoverScaleSizeMultiplier() if being hovered over, (or
     * n * {@link #getTransitionMultiplier(long)} if not being hovered over).
     * @return A float denoting the multiplier to use to increase the render size of an item to denote it's being
     *         hovered over (pointed at or has a VR hand placed inside).
     */
    public float hoverScaleSizeMultiplier();
}
