package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.mixin.DragonFireballRendererMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.platform.Platform;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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
    protected boolean forceDisableItemGuide = false;
    public boolean forceTickEvenIfNoTrack = false;

    public AbstractImmersive(int maxImmersives) {
        Immersives.IMMERSIVES.add(this);
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
    public abstract ImmersiveHandler getHandler();

    public boolean hasInfo(BlockPos pos) {
        for (I info : this.infos) {
            if (info.getBlockPosition().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public boolean hitboxesAvailable(AbstractImmersiveInfo info) {
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
    public abstract boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info);

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

    protected boolean slotHelpBoxIsSelected(I info, int slotNum) {
        return info.slotHovered == slotNum;
    }

    public abstract void processStorageFromNetwork(AbstractImmersiveInfo info, HandlerStorage storage);

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
                            AABB itemBox = info.getInputSlots()[i];
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
                           AABB hitbox, boolean renderItemCounts, int light) {
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
                           AABB hitbox, boolean renderItemCounts, int spinDegrees, int light) {
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
                stack.mulPose(Axis.YP.rotationDegrees(180));
                Vec3 textMove;
                if (VRPluginVerify.hasAPI && VRPluginVerify.clientInVR()) {
                    IVRPlayer textMovePlayer = Platform.isDevelopmentEnvironment() ?
                            VRPlugin.API.getVRPlayer(Minecraft.getInstance().player) :
                            VRPlugin.API.getRenderVRPlayer();
                    textMove = textMovePlayer.getHMD().getLookAngle();
                } else {
                    textMove = Minecraft.getInstance().player.getLookAngle();
                }
                textMove = textMove.multiply(-0.05, -0.05, -0.05);
                textPos = textPos.add(textMove);
            }

            if (facing != null) {
                stack.mulPose(Axis.YP.rotationDegrees(degreesRotation));
                stack.mulPose(Axis.XP.rotationDegrees(upDownRot));
            }

            ItemDisplayContext type = facing == null ? ItemDisplayContext.GROUND :
                    ItemDisplayContext.FIXED;

            Minecraft.getInstance().getItemRenderer().renderStatic(item, type,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    stack, Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().level, 0);

            stack.popPose();

            if (renderItemCounts && item.getCount() > 1) {
                this.renderText(Component.literal(String.valueOf(item.getCount())),
                        stack, textPos, facing == null ? 0.0025f : 0.01f, light);
            }
        }
        renderHitbox(stack, hitbox, pos);
    }

    protected void enqueueItemGuideRender(PoseStack stack, AABB hitbox, float alpha, boolean isSelected, int light) {
        ClientRenderSubscriber.itemGuideRenderData.add(
                new ClientRenderSubscriber.ItemGuideRenderData(stack, hitbox, alpha, isSelected, light));
    }

    protected void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos) {
        renderHitbox(stack, hitbox, pos, false);
    }

    protected void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos, boolean alwaysRender) {
        renderHitbox(stack, hitbox, pos, alwaysRender, 1, 1, 1);
    }

    public static void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos, boolean alwaysRender,
                                    float red, float green, float blue) {
        renderHitbox(stack, hitbox, pos, alwaysRender, red, green, blue, 1);
    }

    public static void renderHitbox(PoseStack stack, AABB hitbox, Vec3 pos, boolean alwaysRender,
                                    float red, float green, float blue, float alpha) {
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
                    red, green, blue, alpha);
            stack.popPose();
        }
    }

    public static void renderText(Component text, PoseStack stack, Vec3 pos, int light) {
        renderText(text, stack, pos, 0.02f, light);
    }

    public static void renderText(Component text, PoseStack stack, Vec3 pos, float textSize, int light) {
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
                stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL,
                0, light);
        stack.popPose();
    }

    public void renderImage(PoseStack stack, ResourceLocation imageLocation, Vec3 pos, Direction facing,
                            float size, int light) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.scale(size, size, size);

        // If north, we're good to go
        if (facing == Direction.WEST) {
            stack.mulPose(Axis.YP.rotationDegrees(90));
        } else if (facing == Direction.SOUTH) {
            stack.mulPose(Axis.YP.rotationDegrees(180));
        } else if (facing == Direction.EAST) {
            stack.mulPose(Axis.YP.rotationDegrees(270));
        } else if (facing == null) {
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            stack.mulPose(Axis.YP.rotationDegrees(180));
        }

        VertexConsumer vertexConsumer =
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(imageLocation));
        PoseStack.Pose pose = stack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 0f, 0, 0, 1);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 1f, 0, 1, 1);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 1f, 1, 1, 0);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 0f, 1, 0, 0);

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
    public static Vec3 getDirectlyInFront(Direction forwardFromBlock, BlockPos pos) {
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
        return get3x3HorizontalGrid(blockPos, spacing, getForwardFromPlayer(Minecraft.getInstance().player, blockPos), false);
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
        return get3x3VerticalGrid(blockPos, spacing, getForwardFromPlayer(Minecraft.getInstance().player, blockPos));
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

    public int getLight(BlockPos pos) {
        // TODO: Return maxLight here if full bright in ImmersiveMC settings
        return LightTexture.pack(Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, pos),
                Minecraft.getInstance().level.getBrightness(LightLayer.SKY, pos));
    }

    public int getLight(BlockPos[] positions) {
        int maxBlock = 0;
        int maxSky = 0;
        for (BlockPos pos : positions) {
            if (pos == null) {
                continue;
            }

            int blockLight = Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > maxBlock) {
                maxBlock = blockLight;
            }

            int skyLight = Minecraft.getInstance().level.getBrightness(LightLayer.SKY, pos);
            if (skyLight > maxSky) {
                maxSky = skyLight;
            }

            // Have max light for both, no need to continue light calculations!
            if (maxBlock == 15 && maxSky == 15) {
                break;
            }
        }
        return LightTexture.pack(maxBlock, maxSky);
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
