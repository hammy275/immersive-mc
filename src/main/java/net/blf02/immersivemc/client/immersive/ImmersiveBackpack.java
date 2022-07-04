package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.BackpackInfo;
import net.blf02.immersivemc.client.model.BackpackCraftingModel;
import net.blf02.immersivemc.client.model.BackpackLowDetailModel;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.FetchPlayerStoragePacket;
import net.blf02.immersivemc.common.network.packet.InteractPacket;
import net.blf02.immersivemc.common.network.packet.InventorySwapPacket;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.common.vr.util.Util;
import net.blf02.immersivemc.server.swap.Swap;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.Optional;

public class ImmersiveBackpack extends AbstractImmersive<BackpackInfo> {

    public static final ImmersiveBackpack singleton = new ImmersiveBackpack();

    public static final BackpackModel model = new BackpackModel();
    public static final BackpackLowDetailModel modelLowDetail = new BackpackLowDetailModel();
    public static final BackpackCraftingModel craftingModel = new BackpackCraftingModel();

    private final double spacing = 3d/8d;

    public ImmersiveBackpack() {
        super(1); // A player only has one backpack
    }

    @Override
    protected void doTick(BackpackInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        int controllerNum = 1;
        IVRData backpackController = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(controllerNum);
        IVRData handController = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(controllerNum == 1 ? 0 : 1);
        info.handPos = backpackController.position();
        info.handPitch = (float) Math.toRadians(backpackController.getPitch());
        info.handYaw = (float) Math.toRadians(backpackController.getYaw());
        info.lookVec = backpackController.getLookAngle();

        // Render backpack closer to the player, and attached to the inner-side of the arm
        info.backVec = info.lookVec.normalize().multiply(-1, -1, -1);
        info.renderPos = info.handPos.add(0, -0.75, 0);
        info.renderPos = info.renderPos.add(info.backVec.multiply(1d/6d, 1d/6d, 1d/6d));

        info.rgb = new Vector3f(ActiveConfig.backpackColor >> 16, ActiveConfig.backpackColor >> 8 & 255,
                ActiveConfig.backpackColor & 255);
        info.rgb.mul(1f/255f);


        info.centerTopPos = info.handPos.add(0, -0.05, 0);
        info.centerTopPos = info.centerTopPos.add(info.backVec.multiply(1d/6d, 1d/6d, 1d/6d)); // Back on arm
        // Multiply massively so the < 1E-4D check from .normalize() rarely kicks in
        Vector3d rightVec = info.lookVec.multiply(1E16D, 0, 1E16D).normalize();
        if (ActiveConfig.leftHandedBackpack) {
            rightVec = new Vector3d(rightVec.z, 0, -rightVec.x).multiply(0.25, 0, 0.25);
        } else {
            rightVec = new Vector3d(-rightVec.z, 0, rightVec.x).multiply(0.25, 0, 0.25);
        }
        info.centerTopPos = info.centerTopPos.add(rightVec);


        Vector3d leftVec = rightVec.multiply(-1, 0, -1);

        // Note: rightVec and leftVec refer to the vectors for right-handed people. Swap the names if referring to
        // left-handed guys, gals, and non-binary pals.

        Vector3f downVecF = new Vector3f(0, -1, 0);
        downVecF.transform(Vector3f.XN.rotation(info.handPitch));
        downVecF.transform(Vector3f.YN.rotation(info.handYaw));
        info.downVec = new Vector3d(downVecF.x(), downVecF.y(), downVecF.z()).normalize();

        // Item hitboxes and positions
        Vector3d leftOffset = new Vector3d(
                leftVec.x * spacing, leftVec.y * spacing, leftVec.z * spacing);
        Vector3d rightOffset = new Vector3d(
                rightVec.x * spacing, rightVec.y * spacing, rightVec.z * spacing);

        double tbSpacing = spacing / 4d;
        Vector3d topOffset = info.lookVec.multiply(tbSpacing, tbSpacing, tbSpacing);
        Vector3d botOffset = info.backVec.multiply(tbSpacing, tbSpacing, tbSpacing);

        Vector3d pos = info.centerTopPos;
        Vector3d[] positions = new Vector3d[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)};

        int start = 9 * info.topRow;
        int end = start + 8;
        int midStart = 9 * info.getMidRow();
        int midEnd = midStart + 8;

        Vector3d downOne = info.downVec.multiply(0.125, 0.125, 0.125);
        Vector3d downTwo = downOne.multiply(2, 2, 2);

        for (int i = 0; i <= 26; i++) {
            Vector3d posRaw = positions[i % 9];
            Vector3d yDown = inRange(i, start, end) ? Vector3d.ZERO :
                    inRange(i, midStart, midEnd) ? downOne : downTwo;
            Vector3d slotPos = posRaw;
            slotPos = slotPos.add(yDown);
            info.setPosition(i, slotPos);
            info.setHitbox(i, createHitbox(info.getPosition(i), 0.05f));
        }

        Vector3d upVec = info.downVec.multiply(-1, -1, -1);

        double upMult = 0.05;

        // Multiply these by 4 since rightVec is multiplied by 0.25 above
        Vector3d leftCraftingPos = info.centerTopPos.add(rightVec.multiply(0.3125*4, 0.3125*4, 0.3125*4))
                .add(upVec.multiply(upMult, upMult, upMult));
        Vector3d rightCraftingPos = info.centerTopPos.add(rightVec.multiply(0.4375*4, 0.4375*4, 0.4375*4))
                .add(upVec.multiply(upMult, upMult, upMult));
        Vector3d centerCraftingPos = info.centerTopPos.add(rightVec.multiply(0.375*4, 0.375*4, 0.375*4))
                .add(upVec.multiply(upMult, upMult, upMult));

        double craftingOffset = 0.625;
        Vector3d[] craftingPositions = new Vector3d[]{
                leftCraftingPos.add(topOffset.multiply(craftingOffset, craftingOffset, craftingOffset)),
                rightCraftingPos.add(topOffset.multiply(craftingOffset, craftingOffset, craftingOffset)),
                leftCraftingPos.add(botOffset.multiply(craftingOffset, craftingOffset, craftingOffset)),
                rightCraftingPos.add(botOffset.multiply(craftingOffset, craftingOffset, craftingOffset))
        };

        for (int i = 27; i <= 30; i++) {
            info.setPosition(i, craftingPositions[i - 27]);
            info.setHitbox(i, createHitbox(info.getPosition(i), 0.05f));
        }

        info.setPosition(31, centerCraftingPos.add(upVec.multiply(0.125, 0.125, 0.125)));
        info.setHitbox(31, createHitbox(info.getPosition(31), 0.05f));


        Optional<Integer> hitboxIntersect = Util.getFirstIntersect(handController.position(),
                info.getAllHitboxes());
        if (hitboxIntersect.isPresent()) {
            info.slotHovered = hitboxIntersect.get();
        } else {
            info.slotHovered = -1;
        }
    }

    public static void onHitboxInteract(PlayerEntity player, BackpackInfo info, int slot) {
        if (slot <= 26) { // Inventory handle
            Network.INSTANCE.sendToServer(new InventorySwapPacket(slot + 9));
            Swap.handleInventorySwap(player, slot + 9, Hand.MAIN_HAND); // Do swap on both sides
        } else {
            Network.INSTANCE.sendToServer(new InteractPacket("backpack", slot, Hand.MAIN_HAND));
            Network.INSTANCE.sendToServer(new FetchPlayerStoragePacket("backpack"));
        }
    }

    protected boolean inRange(int num, int start, int end) {
        return start <= num && num <= end;
    }

    @Override
    public boolean hasValidBlock(BackpackInfo info, World level) {
        return true;
    }

    @Override
    public boolean shouldRender(BackpackInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(Minecraft.getInstance().player) &&
                VRPlugin.API.apiActive(Minecraft.getInstance().player) &&
                info.readyToRender();
    }

    @Override
    protected void render(BackpackInfo info, MatrixStack stack, boolean isInVR) {
        for (int i = 0; i <= 31; i++) {
            AxisAlignedBB hitbox = info.getHitbox(i);
            renderHitbox(stack, hitbox, info.getPosition(i));
        }

        for (int i = 0; i <= 26; i++) {
            ItemStack item = Minecraft.getInstance().player.inventory.getItem(i + 9);
            if (!item.isEmpty() && info.getPosition(i) != null) {
                final float size =
                        info.slotHovered == i ? ClientConstants.itemScaleSizeBackpackSelected : ClientConstants.itemScaleSizeBackpack;
                renderItem(item, stack, info.getPosition(i), size, null, info.getHitbox(i), true);
            }
        }

        for (int i = 27; i <= 31; i++) {
            ItemStack item = i == 31 ? info.craftingOutput : info.craftingInput[i - 27];
            if (!item.isEmpty() && info.getPosition(i) != null) {
                renderItem(item, stack, info.getPosition(i), ClientConstants.itemScaleSizeBackpack, null, info.getHitbox(i), i == 31);
            }
        }

        stack.pushPose();
        Vector3d pos = info.renderPos;

        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.scale(0.5f, 0.5f, 0.5f);

        stack.translate(0, 1.5, 0); // Translate origin to our hand

        stack.mulPose(Vector3f.YN.rotation(info.handYaw));
        stack.mulPose(Vector3f.XN.rotation(info.handPitch));
        stack.mulPose(Vector3f.ZP.rotation((float) Math.PI)); // Rotate

        stack.translate(0, -1.5, 0); // Move back to where we started

        // Basically move the model to the side of the origin
        stack.translate(ActiveConfig.leftHandedBackpack ? -0.5 : 0.5, 0, 0);

        // Render the model (finally!)
        getBackpackModel().renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(BackpackModel.textureLocation)),
                15728880, OverlayTexture.NO_OVERLAY,
                info.rgb.x(), info.rgb.y(), info.rgb.z(), 1);

        // Translate and render the crafting on the side of the backpack and down a bit
        // (yes, positive y in this context moves it down lol)
        stack.translate(ActiveConfig.leftHandedBackpack ? -0.75 : 0.75, 0.25, 0);
        craftingModel.renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(BackpackCraftingModel.textureLocation)),
                15728880, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useBackpack;
    }

    @Override
    protected boolean slotShouldRenderHelpHitbox(BackpackInfo info, int slotNum) {
        if (Minecraft.getInstance().player == null) return false;
        if (slotNum <= 26) {
            return Minecraft.getInstance().player.inventory.getItem(slotNum + 9).isEmpty();
        } else { // Crafting input
            int tableIndex = slotNum - 27;
            return info.craftingInput[tableIndex] == null || info.craftingInput[tableIndex].isEmpty();
        }
    }

    @Override
    protected void initInfo(BackpackInfo info) {
        // Get inventory data on initialization
        Network.INSTANCE.sendToServer(new FetchPlayerStoragePacket("backpack"));
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, PlayerEntity player, int closest, Hand hand) {}

    public void processFromNetwork(ImmersiveStorage storage) {
        if (singleton.infos.size() > 0) {
            BackpackInfo info = singleton.infos.get(0);
            for (int i = 0; i <= 3; i++) {
                info.craftingInput[i] = storage.items[i];
            }
            info.craftingOutput = storage.items[4];
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
        if (ActiveConfig.useLowDetailBackpack) {
            return modelLowDetail;
        }
        return model;
    }
}
