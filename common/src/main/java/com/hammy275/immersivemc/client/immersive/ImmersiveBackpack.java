package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.client.compat.ipn.IPN;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.model.BackpackBundleModel;
import com.hammy275.immersivemc.client.model.BackpackCraftingModel;
import com.hammy275.immersivemc.client.model.BackpackLowDetailModel;
import com.hammy275.immersivemc.client.model.BackpackModel;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.FetchBackpackStoragePacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.swap.Swap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.List;

public class ImmersiveBackpack extends AbstractPlayerAttachmentImmersive<BackpackInfo, NullStorage> {
    public static final BackpackBundleModel bundleModel =
            new BackpackBundleModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackBundleModel.LAYER_LOCATION));

    public static final BackpackModel model =
            new BackpackModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackModel.LAYER_LOCATION));
    public static final BackpackLowDetailModel modelLowDetail =
            new BackpackLowDetailModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackLowDetailModel.LAYER_LOCATION));
    public static final BackpackCraftingModel craftingModel =
            new BackpackCraftingModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackpackCraftingModel.LAYER_LOCATION));

    private static final int maxColor = 0xFFFFFFFF;

    private final double spacing = 3d/8d;

    public ImmersiveBackpack() {
        super(1); // A player only has one backpack
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    protected void renderTick(BackpackInfo info, boolean isInVR) {
        super.renderTick(info, isInVR);
        VRPose vrPose = VRClientAPI.instance().getWorldRenderPose();
        calculatePositions(info, vrPose);
    }

    @Override
    protected void doTick(BackpackInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        info.light = getLight(getLightPos(info));
    }

    @Override
    public @Nullable ImmersiveHandler getHandler() {
        return null;
    }

    @Override
    public BlockPos getLightPos(BackpackInfo info) {
        // Light position is bag position if not in light-blocking block, or HMD position if it is in one.
        BlockPos c1 = BlockPos.containing(VRClientAPI.instance().getPreTickWorldPose().getHand(getBagHand()).getPos());
        if (!Minecraft.getInstance().level.getBlockState(c1).canOcclude()) {
            return c1;
        } else {
            return BlockPos.containing(VRClientAPI.instance().getPreTickWorldPose().getHead().getPos());
        }
    }

    public static void onHitboxInteract(Player player, BackpackInfo info, int slot) {
        if (slot <= 26) { // Inventory handle
            Inventory inventory = player.getInventory();
            if (IPN.ipnCompat.available() && !Util.stacksEqualBesidesCount(inventory.getItem(slot + 9), inventory.getItem(inventory.getSelectedSlot()))) {
                IPN.ipnCompat.doInventorySwap(slot + 9, inventory.getSelectedSlot());
            } else {
                Network.INSTANCE.sendToServer(new SwapPacket(BlockPos.ZERO, List.of(slot + 9), InteractionHand.MAIN_HAND, SwapMode.SINGLE, SwapPacket.SwapDestination.INVENTORY));
                Swap.handleInventorySwap(player, slot + 9, InteractionHand.MAIN_HAND); // Do swap on both sides
            }
        } else {
            Network.INSTANCE.sendToServer(new SwapPacket(BlockPos.ZERO, List.of(slot), InteractionHand.MAIN_HAND, SwapMode.SINGLE, SwapPacket.SwapDestination.BAG_CRAFTING));
            Network.INSTANCE.sendToServer(new FetchBackpackStoragePacket());
        }
    }

    protected boolean inRange(int num, int start, int end) {
        return start <= num && num <= end;
    }

    @Override
    public boolean shouldRender(BackpackInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                VRVerify.playerInVR(Minecraft.getInstance().player) && info.light >= 0;
    }

    @Override
    protected void render(BackpackInfo info, PoseStack stack, boolean isInVR) {
        boolean leftHanded = leftHanded();
        for (int i = 0; i <= 31; i++) {
            renderHitbox(stack, info.getHitbox(i));
        }

        for (int i = 0; i <= 26; i++) {
            ItemStack item = Minecraft.getInstance().player.getInventory().getItem(i + 9);
            if (!item.isEmpty() && info.getPosition(i) != null) {
                final float size =
                        info.slotHovered == i ? ClientConstants.itemScaleSizeBackpackSelected : ClientConstants.itemScaleSizeBackpack;
                renderItem(item, stack, info.getPosition(i), size, null, info.getHitbox(i), true, info.light);
            }
        }

        for (int i = 27; i <= 31; i++) {
            // info actually holds item information, so we use that here
            ItemStack item = i == 31 ? info.craftingOutput : info.craftingInput[i - 27];
            if (!item.isEmpty() && info.getPosition(i) != null) {
                final float size =
                        info.slotHovered == i ? ClientConstants.itemScaleSizeBackpackSelected : ClientConstants.itemScaleSizeBackpack;
                renderItem(item, stack, info.getPosition(i), size, null, info.getHitbox(i), true, info.light);
            }
        }

        stack.pushPose();
        Vec3 pos = info.renderPos;

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.position().x + pos.x,
                -cameraInfo.position().y + pos.y,
                -cameraInfo.position().z + pos.z);

        stack.scale(0.5f, 0.5f, 0.5f);

        stack.mulPose(Axis.YN.rotation(info.handYaw));
        stack.mulPose(Axis.XN.rotation(info.handPitch));
        stack.mulPose(Axis.ZP.rotation((float) Math.PI + info.handRoll)); // Rotate

        stack.translate(0, -3, 0); // Move model up since the model center is not the visual center

        // Render the model (finally!)
        getBackpackModel().renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderTypes.entityCutout(getBackpackTexture())),
                info.light, OverlayTexture.NO_OVERLAY,
                info.argb);

        // Translate and render the crafting on the side of the backpack and down a bit
        // (yes, positive y in this context moves it down lol)
        stack.translate(leftHanded ? -0.75 : 0.75, 0.25, 0);
        craftingModel.renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderTypes.entityCutout(BackpackCraftingModel.textureLocation)),
                info.light, OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);

        stack.popPose();

        // Render item guides here instead since we're using info
        if (ActiveConfig.active().placementGuideMode != PlacementGuideMode.OFF) {
            for (int i = 0; i < info.getInputSlots().length; i++) {
                if (inputSlotShouldRenderHelpHitbox(info, i)) { // Use info here since it holds info about crafting
                    enqueueItemGuideRender(stack, info.getInputSlots()[i], 0.2f, slotHelpBoxIsSelected(info, i), info.light);
                }
            }
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.active().useBagImmersive;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(BackpackInfo info, int slotNum) {
        if (Minecraft.getInstance().player == null) return false;
        if (slotNum <= 26) {
            return Minecraft.getInstance().player.getInventory().getItem(slotNum + 9).isEmpty();
        } else { // Crafting input
            int tableIndex = slotNum - 27;
            return info.craftingInput[tableIndex] == null || info.craftingInput[tableIndex].isEmpty();
        }
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return true;
    }

    @Override
    public BackpackInfo refreshOrTrackObject(BlockPos pos, Level level) {
        return null;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractPlayerAttachmentInfo info) {
        return false;
    }

    @Override
    protected void initInfo(BackpackInfo info) {
        // Get inventory data on initialization
        Network.INSTANCE.sendToServer(new FetchBackpackStoragePacket());
    }

    @Override
    public void handleRightClick(AbstractPlayerAttachmentInfo info, Player player, int closest, InteractionHand hand) {}

    @Override
    public void processStorageFromNetwork(AbstractPlayerAttachmentInfo info, NullStorage storage) {
        // Intentional NO-OP
    }

    public void processFromNetwork(List<ItemStack> items) {
        if (this.infos.size() > 0) {
            BackpackInfo info = this.infos.get(0);
            for (int i = 0; i <= 3; i++) {
                info.craftingInput[i] = items.get(i);
            }
            info.craftingOutput = items.get(4);
        }
    }


    public void doTrack() {
        if (this.infos.isEmpty()) {
            this.infos.add(new BackpackInfo());
        } else {
            this.infos.clear();
        }
    }

    public static Model getBackpackModel() {
        switch (ActiveConfig.active().bagMode) {
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

    public static Identifier getBackpackTexture() {
        switch (ActiveConfig.active().bagMode) {
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

    public static int getBackpackColor() {
        if (ActiveConfig.active().bagMode.colorable) {
            return ActiveConfig.active().bagColor | 0xFF000000;
        } else {
            return maxColor;
        }
    }

    private Vec3 getRightVec(BackpackInfo info) {
        Vector3f leftF = new Vector3f(0, 0, 1); // +Z is the default forward vector
        leftF.rotate(Axis.YN.rotation((float) Math.PI / 2f));
        leftF.rotate(Axis.ZP.rotation(info.handRoll));
        leftF.rotate(Axis.XN.rotation(info.handPitch));
        leftF.rotate(Axis.YN.rotation(info.handYaw));
        return new Vec3(leftF.x(), leftF.y(), leftF.z());
    }

    private void calculatePositions(BackpackInfo info, VRPose vrPose) {
        VRBodyPartData backpackData = vrPose.getHand(getBagHand());
        info.handPos = backpackData.getPos();
        info.handPitch = (float) backpackData.getPitch();
        info.handYaw = (float) backpackData.getYaw();
        info.handRoll = (float) backpackData.getRoll();
        info.lookVec = backpackData.getDir();

        Vec3 rightVec = getRightVec(info).scale(0.25);
        if (leftHanded()) {
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

        info.argb = getBackpackColor();

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
            info.setPosition(i, slotPos);
            info.setHitbox(i, OBBFactory.instance().create(AABB.ofSize(info.getPosition(i), 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));
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
            info.setPosition(i, craftingPositions[i - 27]);
            info.setHitbox(i, OBBFactory.instance().create(AABB.ofSize(info.getPosition(i), 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));
        }

        info.setPosition(31, centerCraftingPos.add(upVec.scale(0.125)));
        info.setHitbox(31, OBBFactory.instance().create(AABB.ofSize(info.getPosition(31), 0.1f, 0.1f, 0.1f), info.handPitch, info.handYaw, info.handRoll));

        info.setInputSlots();
    }

    private boolean leftHanded() {
        boolean vrLeftHanded = VRClientAPI.instance().isLeftHanded();
        boolean useSwappedHands = ActiveConfig.active().swapBagHand;
        return vrLeftHanded != useSwappedHands; // If both are true or both are false, we're using the right hand.
    }

    private InteractionHand getBagHand() {
        return ActiveConfig.active().swapBagHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}
