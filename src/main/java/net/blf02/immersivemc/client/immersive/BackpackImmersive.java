package net.blf02.immersivemc.client.immersive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.client.immersive.info.BackpackInfo;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class BackpackImmersive extends AbstractImmersive<BackpackInfo> {

    public static final BackpackImmersive singleton = new BackpackImmersive();

    public static final BackpackModel model = new BackpackModel();

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
        Vector3d lookVec = controller.getLookAngle();
        // TODO: Counterclockwise rotation when attached to other controller
        info.sideVec = new Vector3d(-lookVec.z, 0, lookVec.x);
        info.renderPos = info.handPos.add(0, -0.675, 0);
        info.renderPos = info.renderPos.add(info.sideVec.multiply(0.25, 0, 0.25));
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
        Vector3d pos = info.renderPos;
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.scale(0.5f, 0.5f, 0.5f);

        stack.translate(0, 1.5, 0.5); // Translate to origin

        stack.mulPose(Vector3f.YN.rotation(info.handYaw));
        stack.mulPose(Vector3f.XN.rotation(info.handPitch));
        stack.mulPose(Vector3f.ZP.rotation((float) Math.PI));
        stack.mulPose(Vector3f.ZP.rotation(info.handRoll)); // Rotate

        stack.translate(0, -1.5, -0.5); // Move back

        model.renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(BackpackModel.textureLocation)),
                15728880, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        stack.popPose();
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
