package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersive;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.compat.ipn.IPN;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BagInfo;
import com.hammy275.immersivemc.client.immersive.info.HitboxItemPair;
import com.hammy275.immersivemc.client.model.BackpackBundleModel;
import com.hammy275.immersivemc.client.model.BackpackCraftingModel;
import com.hammy275.immersivemc.client.model.BackpackLowDetailModel;
import com.hammy275.immersivemc.client.model.BackpackModel;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BagStorage;
import com.hammy275.immersivemc.common.obb.OBBUtil;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.server.swap.Swap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmersiveBag implements PlayerAttachmentImmersive<BagInfo, BagInfo.RenderState, BagStorage> {

    public static final BackpackBundleModel bundleModel =
            new BackpackBundleModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackBundleModel.LAYER_LOCATION));
    public static final BackpackModel model =
            new BackpackModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackModel.LAYER_LOCATION));
    public static final BackpackLowDetailModel modelLowDetail =
            new BackpackLowDetailModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackLowDetailModel.LAYER_LOCATION));
    public static final BackpackCraftingModel craftingModel =
            new BackpackCraftingModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackCraftingModel.LAYER_LOCATION));

    protected final List<BagInfo> trackedObjects = new ArrayList<>();

    private static final double spacing = 3d/8d;

    @Override
    public BagInfo buildInfo(AbstractClientPlayer player) {
        return new BagInfo(player);
    }

    @Override
    public Collection<BagInfo> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public int handleHitboxInteract(BagInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        int firstSlot = hitboxIndices.get(0);
        if (firstSlot < 27) {
            if (IPN.ipnCompat.available()) {
                IPN.ipnCompat.doInventorySwap(firstSlot + 9, player.getInventory().getSelectedSlot());
            } else {
                ImmersiveClientLogicHelpers.instance().sendSwapPacket(getHandler(), player, List.of(firstSlot), hand, modifierPressed);
                Swap.handleInventorySwap(player, firstSlot + 9, hand);

            }
        } else {
            ImmersiveClientLogicHelpers.instance().sendSwapPacket(getHandler(), player,
                    hitboxIndices.stream().filter(slot -> slot >= 27).toList(), hand, modifierPressed);
        }
        return 8;
    }

    @Override
    public void tick(BagInfo info) {
        info.tick();
        calculatePositions(info, VR.API.getVRPose(info.getOwner()));
        info.light = ImmersiveClientLogicHelpers.instance().getLight(BlockPos.containing(info.handPos));

    }

    private void calculatePositions(BagInfo info, VRPose vrPose) {
        info.leftHanded = leftHanded(vrPose, info);
        VRBodyPartData backpackData = vrPose.getHand(getBagHand());
        info.handPos = backpackData.getPos();
        info.handPitch = (float) backpackData.getPitch();
        info.handYaw = (float) backpackData.getYaw();
        info.handRoll = (float) backpackData.getRoll();
        info.lookVec = backpackData.getDir();

        Vec3 rightVec = getRightVec(info).scale(0.25);
        if (info.leftHanded) {
            // Means we can imagine for right-handed players, and the code will work for left-handed players
            rightVec = rightVec.scale(-1);
        }
        Vec3 leftVec = rightVec.scale(-1);

        Vector3f downVecF = new Vector3f(0, -1, 0);
        downVecF.rotate(Axis.ZP.rotation(info.handRoll));
        downVecF.rotate(Axis.XN.rotation(info.handPitch));
        downVecF.rotate(Axis.YN.rotation(info.handYaw));
        info.downVec = new Vec3(downVecF.x(), downVecF.y(), downVecF.z());

        // Render backpack closer to the player, and attached to the inner-side of the arm
        info.backVec = info.lookVec.scale(-1);
        info.renderPos = info.handPos.add(info.downVec.scale(0.75));
        info.renderPos = info.renderPos.add(info.backVec.scale(1d/6d));
        info.renderPos = info.renderPos.add(rightVec);

        if (info.ownerIsLocalPlayer()) {
            info.argb = getBackpackColor();
            info.bagMode = ActiveConfig.active().bagMode;
        }

        info.centerTopPos = info.renderPos.add(info.downVec.scale(-0.7));

        // Item hitboxes and positions
        Vec3 leftOffset = leftVec.scale(spacing);
        Vec3 rightOffset = rightVec.scale(spacing);

        double tbSpacing = spacing / 4d;
        Vec3 topOffset = info.lookVec.scale(tbSpacing);
        Vec3 botOffset = info.backVec.scale(tbSpacing);

        Vec3 pos = info.centerTopPos;
        Vec3[] positions = new Vec3[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)};

        int start = 9 * info.topRow;
        int end = start + 8;
        int midStart = 9 * info.getMidRow();
        int midEnd = midStart + 8;

        Vec3 downOne = info.downVec.scale(0.105);
        Vec3 downTwo = downOne.scale(2);

        for (int i = 0; i <= 26; i++) {
            Vec3 posRaw = positions[i % 9];
            Vec3 yDown = inRange(i, start, end) ? Vec3.ZERO :
                    inRange(i, midStart, midEnd) ? downOne : downTwo;
            Vec3 slotPos = posRaw;
            slotPos = slotPos.add(yDown);
            info.setHitbox(i, OBBFactory.instance().create(AABB.ofSize(slotPos, 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));
        }

        Vec3 upVec = info.downVec.scale(-1);

        double upMult = 0.05;

        // Multiply these by 4 since rightVec is multiplied by 0.25 above
        Vec3 leftCraftingPos = info.centerTopPos.add(rightVec.scale(0.3125*4))
                .add(upVec.scale(upMult));
        Vec3 rightCraftingPos = info.centerTopPos.add(rightVec.scale(0.4375*4))
                .add(upVec.scale(upMult));
        Vec3 centerCraftingPos = info.centerTopPos.add(rightVec.scale(0.375*4))
                .add(upVec.scale(upMult));

        double craftingOffset = 0.625;
        Vec3[] craftingPositions = new Vec3[]{
                leftCraftingPos.add(topOffset.scale(craftingOffset)),
                rightCraftingPos.add(topOffset.scale(craftingOffset)),
                leftCraftingPos.add(botOffset.scale(craftingOffset)),
                rightCraftingPos.add(botOffset.scale(craftingOffset))
        };

        for (int i = 27; i <= 30; i++) {
            info.setHitbox(i, OBBFactory.instance().create(AABB.ofSize(craftingPositions[i - 27], 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));
        }

        info.setHitbox(31, OBBFactory.instance().create(AABB.ofSize(centerCraftingPos.add(upVec.scale(0.125)), 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));

        for (int i = 0; i < 27; i++) {
            info.hitboxes.get(i).item = Minecraft.getInstance().player.getInventory().getItem(i + 9);
        }

        if (info.ownerIsLocalPlayer()) {
            OBB obb = OBBUtil.getEncompassingOBB(info.hitboxes.subList(27, 31).stream()
                    .map(pair -> (OBB) pair.getHitbox()).toList());
            info.dragHitbox = OBBFactory.instance().create(obb.getUnderlyingAABB().inflate(0.0625), obb.getRotation());
        }


        if (info.clearLastPos) {
            for (HitboxItemPair hitbox : info.hitboxes) {
                hitbox.lastPos = null;
            }
        }
    }

    @Override
    public @Nullable BoundingBox getDragHitbox(BagInfo info) {
        return info.dragHitbox;
    }

    @Override
    public boolean isInputHitbox(BagInfo info, int hitboxIndex) {
        return hitboxIndex < 31;
    }

    @Override
    public boolean shouldRender(BagInfo.RenderState renderState) {
        return true;
    }

    @Override
    public void render(BagInfo.RenderState renderState, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        int start = renderState.ownedByLocalPlayer ? 0 : 27;
        for (int i = start; i <= 31; i++) {
            helpers.renderItemWithRenderState(renderState.items.get(i), stack, ClientConstants.itemScaleSizeBackpack,
                    true, renderState.light, renderState, renderState.ownedByLocalPlayer, i, null, null, null);
        }

        if (renderState.dragHitbox != null) {
            helpers.renderHitbox(stack, renderState.dragHitbox, false, 0, 1, 1);
        }

        stack.pushPose();
        Vec3 pos = renderState.renderPos;

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.position().x + pos.x,
                -cameraInfo.position().y + pos.y,
                -cameraInfo.position().z + pos.z);

        stack.scale(0.5f, 0.5f, 0.5f);

        stack.mulPose(Axis.YN.rotation(renderState.handYaw));
        stack.mulPose(Axis.XN.rotation(renderState.handPitch));
        stack.mulPose(Axis.ZP.rotation((float) Math.PI + renderState.handRoll)); // Rotate

        stack.translate(0, -3, 0); // Move model up since the model center is not the visual center

        // Render the model (finally!)
        getBackpackModel(renderState.bagMode).renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderTypes.entityCutout(getBackpackTexture(renderState.bagMode))),
                renderState.light, OverlayTexture.NO_OVERLAY,
                renderState.argb);

        // Translate and render the crafting on the side of the backpack and down a bit
        // (yes, positive y in this context moves it down lol)
        stack.translate(renderState.leftHanded ? -0.75 : 0.75, 0.25, 0);
        craftingModel.renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderTypes.entityCutout(BackpackCraftingModel.textureLocation)),
                renderState.light, OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);

        stack.popPose();
    }

    @Override
    public PlayerAttachmentImmersiveHandler<BagStorage> getHandler() {
        return ImmersiveHandlers.bagHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return ClientUtil.createConfigScreenInfo(
                "backpack_button", () -> new ItemStack(Items.TRAPPED_CHEST), config -> config.useBagImmersive,
                (config, newVal) -> config.useBagImmersive = newVal
        );
    }

    @Override
    public void processStorageFromNetwork(BagInfo info, BagStorage storage) {
        for (int i = 0; i <= 4; i++) {
            info.hitboxes.get(i + 27).item = storage.getItems().get(i);
        }
        if (!info.ownerIsLocalPlayer()) {
            info.otherPlayerSwappedHands = storage.useSwappedHands;
            info.argb = storage.bagColor;
            info.bagMode = storage.bagMode;
        }
    }

    @Override
    public BagInfo.RenderState createRenderState() {
        return new BagInfo.RenderState();
    }

    @Override
    public void extractRenderState(BagInfo info, BagInfo.RenderState renderState, float partialTicks) {
        // We can't really approximate the hitboxes ourselves, so we need to do the same math as tick() but with the
        // render pose.
        info = new BagInfo(info);
        VRPose pose;
        // Need the render pose for the local player, but otherwise just get the pose Vivecraft knows of for the other
        // player, since that's what it's already displaying.
        if (info.ownerIsLocalPlayer()) {
            pose = VR.ClientAPI.getWorldRenderPose();
        } else {
            pose = VR.API.getVRPose(info.getOwner());
        }
        calculatePositions(info, pose);
        renderState.hitboxes = info.hitboxes.stream().map(hitbox -> hitbox.box).toList();
        renderState.items = info.hitboxes.stream().map(hitbox -> hitbox.item).toList();
        renderState.light = info.light;
        renderState.renderPos = info.renderPos;
        renderState.handPitch = info.handPitch;
        renderState.handYaw = info.handYaw;
        renderState.handRoll = info.handRoll;
        renderState.argb = info.argb;
        renderState.tickCount = info.getTicksExisted();
        renderState.slotHovered = info.getSlotHovered(Util.otherHand(getBagHand()).ordinal());
        renderState.ownedByLocalPlayer = info.ownerIsLocalPlayer();
        renderState.leftHanded = info.leftHanded;
        renderState.dragHitbox = info.dragHitbox;
        renderState.bagMode = info.bagMode;
    }

    public static int getBackpackColor() {
        if (ActiveConfig.active().bagMode.colorable) {
            return ActiveConfig.active().bagColor | 0xFF000000;
        } else {
            return 0xFFFFFFFF;
        }
    }

    public static Model getBackpackModel(BackpackMode bagMode) {
        switch (bagMode) {
            case BUNDLE, BUNDLE_COLORABLE -> {
                return bundleModel;
            }
            case ORIGINAL -> {
                return model;
            }
            case ORIGINAL_LOW_DETAIL -> {
                return modelLowDetail;
            }
            default -> throw new IllegalArgumentException("backpackMode set to invalid enum value!");
        }
    }

    public static Identifier getBackpackTexture(BackpackMode bagMode) {
        switch (bagMode) {
            case BUNDLE -> {
                return BackpackBundleModel.textureLocation;
            }
            case BUNDLE_COLORABLE -> {
                return BackpackBundleModel.textureLocationColorable;
            }
            case ORIGINAL -> {
                return BackpackModel.textureLocation;
            }
            case ORIGINAL_LOW_DETAIL -> {
                return BackpackLowDetailModel.textureLocation;
            }
            default -> throw new IllegalArgumentException("backpackMode set to invalid enum value!");
        }
    }

    private static boolean inRange(int num, int start, int end) {
        return start <= num && num <= end;
    }

    private static Vec3 getRightVec(BagInfo info) {
        Vector3f leftF = new Vector3f(0, 0, 1); // +Z is the default forward vector
        leftF.rotate(Axis.YN.rotation((float) Math.PI / 2f));
        leftF.rotate(Axis.ZP.rotation(info.handRoll));
        leftF.rotate(Axis.XN.rotation(info.handPitch));
        leftF.rotate(Axis.YN.rotation(info.handYaw));
        return new Vec3(leftF.x(), leftF.y(), leftF.z());
    }

    private static boolean leftHanded(VRPose pose, BagInfo info) {
        boolean vrLeftHanded = pose.isLeftHanded();
        boolean useSwappedHands = info.ownerIsLocalPlayer() ? ActiveConfig.active().swapBagHand : info.otherPlayerSwappedHands;
        return vrLeftHanded != useSwappedHands; // If both are true or both are false, we're using the right hand.
    }

    private static InteractionHand getBagHand() {
        return ActiveConfig.active().swapBagHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static boolean canGotoNextRowFromClick(BagInfo info) {
        return !info.hasSlotHovered() && (info.dragHitbox == null
                || !BoundingBox.contains(info.dragHitbox, VR.ClientAPI.getPreTickWorldPose().getHand(getBagHand()).getPos()));
    }
}
