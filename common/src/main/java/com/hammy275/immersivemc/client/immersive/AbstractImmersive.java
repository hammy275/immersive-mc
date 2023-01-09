package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.mixin.DragonFireballRendererMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Abstract immersive anything
 * @param <I> Info type
 */
public abstract class AbstractImmersive<I extends AbstractImmersiveInfo> {
    public static final int maxLight = LightTexture.pack(15, 15);

    protected final List<I> infos;
    public final int maxImmersives;
    protected static final Cube1x1 cubeModel = new Cube1x1(Minecraft.getInstance().getEntityModels().bakeLayer(Cube1x1.LAYER_LOCATION));
    protected boolean forceDisableItemGuide = false;

    public AbstractImmersive(int maxImmersives) {
        Immersives.IMMERSIVES.add(this);
        this.maxImmersives = maxImmersives;
        this.infos = new ArrayList<>(maxImmersives > 0 ? maxImmersives + 1 : 16);
    }

    public boolean hitboxesAvailable(AbstractImmersiveInfo info) {
        return true;
    }

    public abstract boolean shouldRender(I info, boolean isInVR);

    protected abstract void render(I info, PoseStack stack, boolean isInVR);

    protected abstract boolean enabledInConfig();

    protected abstract boolean slotShouldRenderHelpHitbox(I info, int slotNum);

    public abstract boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level);

    public abstract void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level);

    public abstract AbstractImmersive<? extends AbstractImmersiveInfo> getSingleton();

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
    public void onAnyRightClick(AbstractImmersiveInfo info) {

    }

    public boolean isVROnly() {
        return false;
    }

    public abstract void handleRightClick(AbstractImmersiveInfo info, Player player, int closest,
                                          InteractionHand hand);

    public void handleTriggerHitboxRightClick(InfoTriggerHitboxes info, Player player, int hitboxNum) {
        // No-op by default. Only needed realistically if the `info` implements InfoTriggerHitboxes
    }

    public void onRemove(I info) {}

    protected boolean slotHelpBoxIsGreen(I info, int slotNum) {
        return info.slotHovered == slotNum;
    }

    public void tick(I info, boolean isInVR) {
        if (enabledInConfig()) {
            if (!info.initCompleted) {
                initInfo(info);
                info.initCompleted = true;
            }
            if (Minecraft.getInstance().level != null && shouldTrack(info.getBlockPosition(),
                    Minecraft.getInstance().level.getBlockState(info.getBlockPosition()),
                    Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()),
                    Minecraft.getInstance().level)) {
                doTick(info, isInVR);
                info.setInputSlots();
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
                render(info, stack, isInVR);
                // Need to end batch here so items show behind item guides
                Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
                if (ActiveConfig.placementGuideMode != PlacementGuideMode.OFF && !forceDisableItemGuide) {
                    // Add from -1 because we're adding lengths, so we subtract one to have valid indexes
                    for (int i = 0; i < info.getInputSlots().length; i++) {
                        if (slotShouldRenderHelpHitbox(info, i)) {
                            AABB itemBox = info.getInputSlots()[i];
                            renderItemGuide(stack, itemBox, 0.2f, slotHelpBoxIsGreen(info, i));
                        }
                    }
                }
            } catch (NullPointerException | ConcurrentModificationException ignored) {}
            // Until we have some sort of lock (if we ever do), we need to try-catch NPEs and CMEs during rendering
            // in case if the other thread modifies things while we render

        }
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
                           AABB hitbox, boolean renderItemCounts) {
        renderItem(item, stack, pos, size, facing, null, hitbox, renderItemCounts, -1);
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
     */
    public void renderItem(ItemStack item, PoseStack stack, Vec3 pos, float size, Direction facing, Direction upDown,
                           AABB hitbox, boolean renderItemCounts, int spinDegrees) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (item != null && item != ItemStack.EMPTY && pos != null) {
            stack.pushPose();

            // Move the stack to be relative to the camera
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);

            // Scale the item to be a good size
            stack.scale(size, size, size);

            Vec3 textPos = pos;

            // Rotate the item to face the player properly
            int degreesRotation = 0; // If North, we're already good
            if (spinDegrees > -1) {
                degreesRotation = spinDegrees;
            } else if (facing == Direction.WEST) {
                degreesRotation = 90;
            } else if (facing == Direction.SOUTH) {
                degreesRotation = 180;
            } else if (facing == Direction.EAST) {
                degreesRotation = 270;
            }

            int upDownRot = 0; // If null, we're good
            if (upDown == Direction.UP) {
                upDownRot = 90;
                textPos = textPos.add(0, 0.15, 0);
            } else if (upDown == Direction.DOWN) {
                upDownRot = 270;
                textPos = textPos.add(0, -0.15, 0);
            } else if (facing == Direction.WEST) {
                textPos = textPos.add(-0.15, 0, 0);
            } else if (facing == Direction.SOUTH) {
                textPos = textPos.add(0, 0, 0.15);
            } else if (facing == Direction.EAST) {
                textPos = textPos.add(0.15, 0, 0);
            } else if (facing == Direction.NORTH) {
                textPos = textPos.add(0, 0, -0.15);
            } else if (facing == null) {
                stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                stack.mulPose(Vector3f.YP.rotationDegrees(180));
                Vec3 textMove = VRPluginVerify.hasAPI && VRPluginVerify.clientInVR ?
                        VRPlugin.API.getRenderVRPlayer().getHMD().getLookAngle() :
                        Minecraft.getInstance().player.getLookAngle();
                textMove = textMove.multiply(-0.05, -0.05, -0.05);
                textPos = textPos.add(textMove);
            }

            if (facing != null) {
                stack.mulPose(Vector3f.YP.rotationDegrees(degreesRotation));
                stack.mulPose(Vector3f.XP.rotationDegrees(upDownRot));
            }

            ItemTransforms.TransformType type = facing == null ? ItemTransforms.TransformType.GROUND :
                    ItemTransforms.TransformType.FIXED;

            Minecraft.getInstance().getItemRenderer().renderStatic(item, type,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    stack, Minecraft.getInstance().renderBuffers().bufferSource(), 0);

            stack.popPose();

            if (renderItemCounts && item.getCount() > 1) {
                this.renderText(new TextComponent(String.valueOf(item.getCount())),
                        stack, textPos, facing == null ? 0.0025f : 0.01f);
            }
        }
        renderHitbox(stack, hitbox, pos);
    }

    protected void renderItemGuide(PoseStack stack, AABB hitbox, float alpha, boolean isGreen) {
        if (hitbox != null) {
            if (ActiveConfig.placementGuideMode == PlacementGuideMode.CUBE) {
                hitbox = hitbox
                        .move(0, hitbox.getYsize() / 2, 0);
                Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 pos = hitbox.getCenter();
                stack.pushPose();
                stack.translate(-renderInfo.getPosition().x + pos.x,
                        -renderInfo.getPosition().y + pos.y,
                        -renderInfo.getPosition().z + pos.z);
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                cubeModel.render(stack, buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                        0, 1, isGreen ? 0 : 1, alpha, (float) (hitbox.getSize() / 2f));
                stack.popPose();
            } else if (ActiveConfig.placementGuideMode == PlacementGuideMode.OUTLINE) {
                renderHitbox(stack, hitbox, hitbox.getCenter(), true, 0, 1, isGreen ? 0 : 1);
            }
        }
    }

    protected void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos) {
        renderHitbox(stack, hitbox, pos, false);
    }

    protected void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos, boolean alwaysRender) {
        renderHitbox(stack, hitbox, pos, alwaysRender, 1, 1, 1);
    }

    public static void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos, boolean alwaysRender,
                                float red, float green, float blue) {
        if ((Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || alwaysRender) &&
                hitbox != null && pos != null) {
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
            stack.pushPose();
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    hitbox.move(-pos.x, -pos.y, -pos.z),
                    red, green, blue, 1);
            stack.popPose();
        }
    }

    public void renderText(Component text, PoseStack stack, Vec3 pos) {
        renderText(text, stack, pos, 0.02f);
    }

    public void renderText(Component text, PoseStack stack, Vec3 pos, float textSize) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.mulPose(renderInfo.rotation());
        stack.scale(-textSize, -textSize, -textSize);
        Font font = Minecraft.getInstance().font;
        float size = -font.width(text) / 2f;
        font.drawInBatch(text, size, 0, 0xFFFFFFFF, false,
                stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), false,
                0, 15728880);
        stack.popPose();
    }

    public void renderImage(PoseStack stack, ResourceLocation imageLocation, Vec3 pos, Direction facing,
                            float size) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.scale(size, size, size);

        // If north, we're good to go
        if (facing == Direction.WEST) {
            stack.mulPose(Vector3f.YP.rotationDegrees(90));
        } else if (facing == Direction.SOUTH) {
            stack.mulPose(Vector3f.YP.rotationDegrees(180));
        } else if (facing == Direction.EAST) {
            stack.mulPose(Vector3f.YP.rotationDegrees(270));
        } else if (facing == null) {
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            stack.mulPose(Vector3f.YP.rotationDegrees(180));
        }

        VertexConsumer vertexConsumer =
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(imageLocation));
        PoseStack.Pose pose = stack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        DragonFireballRendererMixin.vertex(vertexConsumer, matrix4f, matrix3f, maxLight, 0f, 0, 0, 1);
        DragonFireballRendererMixin.vertex(vertexConsumer, matrix4f, matrix3f, maxLight, 1f, 0, 1, 1);
        DragonFireballRendererMixin.vertex(vertexConsumer, matrix4f, matrix3f, maxLight, 1f, 1, 1, 0);
        DragonFireballRendererMixin.vertex(vertexConsumer, matrix4f, matrix3f, maxLight, 0f, 1, 0, 0);

        stack.popPose();
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
     * @return Vec3 of the front-right of the block face from the block's perspective (front left from the player's)
     */
    public Vec3 getDirectlyInFront(Direction forwardFromBlock, BlockPos pos) {
        // This mess sets pos to always be directly in front of the face of the tile entity
        if (forwardFromBlock == Direction.SOUTH) {
            BlockPos front = pos.relative(forwardFromBlock);
            return new Vec3(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.WEST) {
            BlockPos front = pos;
            return new Vec3(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.NORTH) {
            BlockPos front = pos.relative(Direction.EAST);
            return new Vec3(front.getX(), front.getY(), front.getZ());
        } else if (forwardFromBlock == Direction.EAST) {
            BlockPos front = pos.relative(Direction.SOUTH).relative(Direction.EAST);
            return new Vec3(front.getX(), front.getY(), front.getZ());
        } else {
            throw new IllegalArgumentException("Furnaces can't point up or down?!?!");
        }
    }

    public Vec3 getTopCenterOfBlock(BlockPos pos) {
        // Only add 0.5 to y since atCenterOf moves it up 0.5 for us
        return Vec3.upFromBottomCenterOf(pos, 1);
    }

    /**
     * Gets the forward direction of the block based on the player
     *
     * Put simply, this returns the opposite of the Direction the player is currently facing (only N/S/E/W)
     * @param player Player to get forward from
     * @return The forward direction of a block to use.
     */
    public Direction getForwardFromPlayer(Player player) {
        if (VRPluginVerify.clientInVR && VRPlugin.API.playerInVR(player)) {
            return Util.horizontalDirectionFromLook(VRPlugin.API.getVRPlayer(player).getHMD().getLookAngle()).getOpposite();
        }
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
    public AABB createHitbox(Vec3 pos, float size) {
        return new AABB(
                pos.x - size,
                pos.y - size,
                pos.z - size,
                pos.x + size,
                pos.y + size,
                pos.z + size);
    }

    public Vec3[] get3x3HorizontalGrid(BlockPos blockPos, double spacing) {
        return get3x3HorizontalGrid(blockPos, spacing, getForwardFromPlayer(Minecraft.getInstance().player), false);
    }

    public Vec3[] get3x3HorizontalGrid(BlockPos blockPos, double spacing, Direction blockForward,
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

    public Vec3[] get3x3VerticalGrid(BlockPos blockPos, double spacing) {
        return get3x3VerticalGrid(blockPos, spacing, getForwardFromPlayer(Minecraft.getInstance().player));
    }

    public Vec3[] get3x3VerticalGrid(BlockPos blockPos, double spacing, Direction blockForward) {
        Vec3 posBotLeft = getDirectlyInFront(blockForward, blockPos);
        Direction left = getLeftOfDirection(blockForward);
        Vec3 pos = posBotLeft.add(left.getNormal().getX() * 0.5, 0.5, left.getNormal().getZ() * 0.5);
        Vec3 leftOffset = new Vec3(
                left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);
        Vec3 rightOffset = new Vec3(
                left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);

        Vec3 upOffset = new Vec3(0, spacing, 0);
        Vec3 downOffset = new Vec3(0, -spacing, 0);

        return new Vec3[]{
                pos.add(leftOffset).add(upOffset), pos.add(upOffset), pos.add(rightOffset).add(upOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(downOffset), pos.add(downOffset), pos.add(rightOffset).add(downOffset)
        };

    }

}
