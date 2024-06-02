package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.common.obb.BoundingBox;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ImmersiveRenderHelpers {

    /**
     * @return An ImmersiveRenderHelpers instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveRenderHelpers instance() {
        return ImmersiveRenderHelpersImpl.INSTANCE;
    }

    /**
     * Renders an item at the specified position facing the camera.
     *
     * @param item The item to render.
     * @param stack The pose stack being rendered with.
     * @param pos The in-world position to render at.
     * @param size The size to render at.
     * @param hitbox The hitbox for interacting with this item. Used for displaying a hitbox when hitboxes are enabled.
     * @param renderItemCounts Whether to render a number representing the item count with the item.
     * @param light The packed sky light and block light to render with.
     */
    public void renderItem(ItemStack item, PoseStack stack, Vec3 pos, float size, BoundingBox hitbox,
                           boolean renderItemCounts, int light);

    /**
     * Renders an item at the specified position.
     *
     * @param item The item to render.
     * @param stack The pose stack being rendered with.
     * @param pos The in-world position to render at.
     * @param size The size to render at.
     * @param hitbox The hitbox for interacting with this item. Used for displaying a hitbox when hitboxes are enabled.
     * @param renderItemCounts Whether to render a number representing the item count with the item.
     * @param light The packed sky light and block light to render with.
     * @param spinDegrees The number of degrees to spin on x/z. Ignored if negative.
     * @param facing The direction for the item to face. If null, the item will face the camera. This value should not
     *               be UP or DOWN. Ignored if spinDegrees is at least 0.
     * @param upDown Direction upwards or downwards for the item to face. Can be null if not facing up or down. If this
     *               is not null, facing instead controls the direction the item is rotated towards.
     */
    public void renderItem(ItemStack item, PoseStack stack, Vec3 pos, float size, BoundingBox hitbox,
                           boolean renderItemCounts, int light, int spinDegrees, @Nullable Direction facing,
                           @Nullable Direction upDown);

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
     * Get the intended light level to render with at the given position as packed sky light and block light. This
     * may not necessarily return the actual light level at the given block, though it should be treated as such.
     * @param pos The position to get the intended light level for rendering at.
     * @return The intended light level for rendering.
     */
    public int getLight(BlockPos pos);

    /**
     * Get the intended light level to render with given several positions. The value returned is a packed sky light
     * and block light, and intentionally may not necessarily return data consistent with all the positions, though
     * should be treated as such.
     * @param positions The positions to get the intended light rendering for.
     * @return The intended light level for rendering.
     */
    public int getLight(Iterable<BlockPos> positions);
}
