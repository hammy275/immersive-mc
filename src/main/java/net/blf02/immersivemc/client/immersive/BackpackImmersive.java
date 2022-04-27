package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.BackpackInfo;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class BackpackImmersive extends AbstractImmersive<BackpackInfo> {

    public static final BackpackImmersive singleton = new BackpackImmersive();

    public static final BackpackModel model = new BackpackModel();
    private final double spacing = 3d/8d;

    public BackpackImmersive() {
        super(1); // A player only has one backpack
    }

    @Override
    public void tick(BackpackInfo info, boolean isInVR) {
        super.tick(info, isInVR);
        int controllerNum = 1; // TODO: Replace with config key lookup
        IVRData controller = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(controllerNum);
        info.handPos = controller.position();
        info.handPitch = (float) Math.toRadians(controller.getPitch());
        info.handYaw = (float) Math.toRadians(controller.getYaw());
        info.handRoll = (float) Math.toRadians(controller.getRoll());
        info.lookVec = controller.getLookAngle();

        // Render backpack closer to the player, and attached to the inner-side of the arm
        info.backVec = info.lookVec.normalize().multiply(-1, -1, -1);
        info.renderPos = info.handPos.add(0, -0.675, 0);
        info.renderPos = info.renderPos.add(info.backVec.multiply(1d/6d, 1d/6d, 1d/6d));

        info.rgb = new Vector3f(ActiveConfig.backpackColor >> 16, ActiveConfig.backpackColor >> 8 & 255,
                ActiveConfig.backpackColor & 255);
        info.rgb.mul(1f/255f);


        info.centerTopPos = info.handPos;
        info.centerTopPos = info.centerTopPos.add(info.backVec.multiply(1d/6d, 1d/6d, 1d/6d)); // Back on arm
        Vector3d rightVec = info.lookVec.multiply(1, 0, 1).normalize();
        rightVec = new Vector3d(-rightVec.z, 0, rightVec.x).multiply(0.25, 0, 0.25);
        info.centerTopPos = info.centerTopPos.add(rightVec);


        Vector3d leftVec = rightVec.multiply(-1, 0, -1);

        // Item hitboxes and positions
        Vector3d leftOffset = new Vector3d(
                leftVec.x * spacing, 0, leftVec.z * spacing);
        Vector3d rightOffset = new Vector3d(
                rightVec.x * spacing, 0, rightVec.z * spacing);

        double tbSpacing = spacing / 4d;
        Vector3d topOffset = info.lookVec.multiply(tbSpacing, tbSpacing, tbSpacing);
        Vector3d botOffset = info.backVec.multiply(tbSpacing, tbSpacing, tbSpacing);

        Vector3d pos = info.centerTopPos;
        Vector3d[] positions = new Vector3d[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)};

        int start = 9 * info.topRow;
        int end = start + 9;

        for (int i = start; i <= end; i++) {
            Vector3d posRaw = positions[i % 9];
            info.setPosition(i, posRaw);
            info.setHitbox(i, createHitbox(posRaw, 0.05f));
        }
    }

    @Override
    public boolean shouldRender(BackpackInfo info, boolean isInVR) {
        return Minecraft.getInstance().player != null &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(Minecraft.getInstance().player) &&
                VRPlugin.API.apiActive(Minecraft.getInstance().player);
    }

    @Override
    protected void render(BackpackInfo info, MatrixStack stack, boolean isInVR) {
        stack.pushPose();
        for (int i = 0; i <= 26; i++) {
            AxisAlignedBB hitbox = info.getHibtox(i);
            if (hitbox != null) {
                // TODO: Remove true so we don't force-render hitbox
                renderHitbox(stack, hitbox, info.getPosition(i), true);
            }
        }
        stack.popPose();

        for (int i = 0; i <= 26; i++) {
            ItemStack item = Minecraft.getInstance().player.inventory.getItem(i + 9);
            if (!item.isEmpty()) {
                stack.pushPose();
                setupCamera(stack, info.getPosition(i));
                final float size = ClientConstants.itemScaleSizeBackpack;
                stack.scale(size, size, size);
                stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemCameraTransforms.TransformType.GROUND,
                        15728880, OverlayTexture.NO_OVERLAY,
                        stack, Minecraft.getInstance().renderBuffers().bufferSource());
                stack.popPose();
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
        stack.mulPose(Vector3f.ZP.rotation((float) Math.PI));
        stack.mulPose(Vector3f.ZP.rotation(info.handRoll)); // Rotate

        stack.translate(0, -1.5, 0); // Move back to where we started

        // Basically move the model to the side of the origin
        // TODO: Move the other way when attached to the other controller
        stack.translate(0.5, 0, 0);

        // Render the model (finally!)
        model.renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(BackpackModel.textureLocation)),
                15728880, OverlayTexture.NO_OVERLAY,
                info.rgb.x(), info.rgb.y(), info.rgb.z(), 1);

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        stack.popPose();
    }

    protected void setupCamera(MatrixStack stack, Vector3d pos) {
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);

    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useBackpack;
    }

    public void doTrack() {
        if (this.infos.isEmpty()) {
            this.infos.add(new BackpackInfo());
        } else {
            this.infos.clear();
        }
    }
}
