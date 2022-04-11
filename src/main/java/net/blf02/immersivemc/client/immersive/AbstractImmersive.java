package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract immersive anything
 * @param <I> Info type
 */
public abstract class AbstractImmersive<I extends AbstractImmersiveInfo> {

    protected final List<I> infos;
    public final int maxImmersives;

    public AbstractImmersive(int maxImmersives) {
        Immersives.IMMERSIVES.add(this);
        this.maxImmersives = maxImmersives;
        this.infos = new ArrayList<>(maxImmersives > 0 ? maxImmersives + 1 : 16);
    }

    public abstract boolean shouldRender(I info, boolean isInVR);

    protected abstract void render(I info, MatrixStack stack, boolean isInVR);

    protected abstract boolean enabledInConfig();

    public void tick(I info, boolean isInVR) {
        if (enabledInConfig()) {
            doTick(info, isInVR);
        }
    }

    protected void doTick(I info, boolean isInVR) {
        // Set the cooldown (transition time) based on how long we've existed or until we stop existing
        if (info.getCountdown() > 1 && info.getTicksLeft() > 20) {
            info.changeCountdown(-1);
        } else if (info.getCountdown() < ClientConstants.transitionTime && info.getTicksLeft() <= 20) {
            info.changeCountdown(1);
        }

        if (info.getTicksLeft() > 0) {
            info.changeTicksLeft(-1);
        }
        info.ticksActive++;
    }

    // Below this line are utility functions. Everything above MUST be overwritten, and have super() called!

    /**
     * Do rendering
     *
     * This is the render method that should be called by outside functions
     */
    public void doRender(I info, MatrixStack stack, boolean isInVR) {
        if (shouldRender(info, isInVR)) {
            render(info, stack, isInVR);
        }
    }

    public List<I> getTrackedObjects() {
        return infos;
    }

    public void renderItem(ItemStack item, MatrixStack stack, Vector3d pos, float size, Direction facing,
                           AxisAlignedBB hitbox) {
        renderItem(item, stack, pos, size, facing, null, hitbox);
    }

    /**
     * Renders an item at the specified position
     * @param item Item to render
     * @param stack MatrixStack in
     * @param pos Position to render at
     * @param size Size to render at
     * @param facing Direction to face (should be the direction of the block)
     * @param upDown Direction upwards or downwards. Can be null if not facing up or down.
     * @param hitbox Hitbox for debug rendering
     */
    public void renderItem(ItemStack item, MatrixStack stack, Vector3d pos, float size, Direction facing, Direction upDown,
                           AxisAlignedBB hitbox) {
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (item != ItemStack.EMPTY) {
            stack.pushPose();

            // Move the stack to be relative to the camera
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);

            // Scale the item to be a good size
            stack.scale(size, size, size);

            // Rotate the item to face the player properly
            int degreesRotation = 0; // If North, we're already good
            if (facing == Direction.WEST) {
                degreesRotation = 90;
            } else if (facing == Direction.SOUTH) {
                degreesRotation = 180;
            } else if (facing == Direction.EAST) {
                degreesRotation = 270;
            }

            int upDownRot = 0; // If null, we're good
            if (upDown == Direction.UP) {
                upDownRot = 90;
            } else if (upDown == Direction.DOWN) {
                upDownRot = 270;
            }


            stack.mulPose(Vector3f.YP.rotationDegrees(degreesRotation));
            stack.mulPose(Vector3f.XP.rotationDegrees(upDownRot));

            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemCameraTransforms.TransformType.FIXED,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    stack, Minecraft.getInstance().renderBuffers().bufferSource());

            // Actually draw what's in our buffer to the screen
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

            stack.popPose();
        }
        renderHitbox(stack, hitbox, pos);
    }

    protected void renderHitbox(MatrixStack stack, AxisAlignedBB hitbox, Vector3d pos) {
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() &&
                hitbox != null) {
            ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
            stack.pushPose();
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);
            OutlineLayerBuffer buffer = Minecraft.getInstance().renderBuffers().outlineBufferSource();
            buffer.setColor(255, 255, 255, 255);
            WorldRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    hitbox.move(-pos.x, -pos.y, -pos.z),
                    1, 1, 1, 1);
            buffer.endOutlineBatch();
            stack.popPose();
        }
    }

    /**
     * Gets the direction to the left from the Direction's perspective, assuming the Direction is
     * looking at the player. This makes it to the right for the player.
     * @param forward Forward direction of the block
     * @return The aforementioned left
     */
    public Direction getLeftOfDirection(Direction forward) {
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

    /**
     * Gets the position precisely to the front-right of pos given the direction forward.
     *
     * This is effectively the bottom right of the front face from the block's perspective, or the bottom left
     * from the player's perspective facing the block.
     *
     * @param forwardFromBlock Direction forward from block. Can also be opposite direction of player
     * @param pos BlockPos of block
     * @return Vector3d of the front-right of the block face from the block's perspective (front left from the player's)
     */
    public Vector3d getDirectlyInFront(Direction forwardFromBlock, BlockPos pos) {
        // This mess sets pos to always be directly in front of the face of the tile entity
        if (forwardFromBlock == Direction.SOUTH) {
            BlockPos front = pos.relative(forwardFromBlock);
            return new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.WEST) {
            BlockPos front = pos;
            return new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.NORTH) {
            BlockPos front = pos.relative(Direction.EAST);
            return new Vector3d(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.EAST) {
            BlockPos front = pos.relative(Direction.SOUTH).relative(Direction.EAST);
            return new Vector3d(front.getX(), front.getY(), front.getZ());
        } else {
            throw new IllegalArgumentException("Furnaces can't point up or down?!?!");
        }
    }

    public Vector3d getTopCenterOfBlock(BlockPos pos) {
        // Only add 0.5 to y since atCenterOf moves it up 0.5 for us
        return Vector3d.upFromBottomCenterOf(pos, 1);
    }

    /**
     * Gets the forward direction of the block based on the player
     *
     * Put simply, this returns the opposite of the Direction the player is currently facing (only N/S/E/W)
     * @param player Player to get forward from
     * @return The forward direction of a block to use.
     */
    public Direction getForwardFromPlayer(PlayerEntity player) {
        return player.getDirection().getOpposite();
    }

    /**
     * Creates the hitbox for an item.
     *
     * Practically identical to how hitboxes are created manually in ImmersiveFurnace
     * @param pos Position for center of hitbox
     * @param size Size of hitbox
     * @return
     */
    public AxisAlignedBB createHitbox(Vector3d pos, float size) {
        return new AxisAlignedBB(
                pos.x - size,
                pos.y - size,
                pos.z - size,
                pos.x + size,
                pos.y + size,
                pos.z + size);
    }

}
