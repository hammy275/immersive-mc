package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.client.immersive.book.BookRenderable;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.hammy275.immersivemc.common.immersive.CommonBookData.*;

public class BookDataRenderState {

    private static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    private static final ResourceLocation writtenBookTexture = Util.id("immersive/nahnotfox_written_book.png");

    public OBB[] pageTurnBoxes;
    public Vec3[] pageTurnPositions;
    public int leftPageIndex;
    public float leftPageTurn;
    public float rightPageTurn;
    public PageChangeState pageChangeState;
    public List<BookRenderable> renderables;
    public List<OBB> obbs;
    public float lastLeftPageTurn;
    public float lastRightPageTurn;

    /**
     * Should be called when rendering.
     * @param stack The PoseStack to render with.
     * @param light The light level being rendered at.
     * @param bookPosRot The PosRot of the book.
     * @param partialTicks Partial ticks time between last frame and current frame
     */
    public void render(PoseStack stack, int light, PosRot bookPosRot, float partialTicks) {
        stack.pushPose();

        Vec3 pos = bookPosRot.getPos();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        stack.scale(scaleSize, scaleSize, scaleSize);

        stack.mulPose(Vector3f.YN.rotation(bookPosRot.getYawF() + (float) Math.PI / 2f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(90f));
        stack.mulPose(Vector3f.ZP.rotation(bookPosRot.getPitchF()));
        stack.mulPose(Vector3f.YN.rotation(bookPosRot.getRollF()));

        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                0, // Partial tick time is always 0 to have page stay in one constant spot
                Mth.lerp(partialTicks, lastLeftPageTurn, leftPageTurn), // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                Mth.lerp(partialTicks, lastRightPageTurn, rightPageTurn), // 0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
                bookOpenAmount // How open the book is. A good range seems to be (0f,1.2f]
        );
        bookModel.render(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entitySolid(writtenBookTexture)),
                light, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();

        if (pageChangeState == PageChangeState.NONE) {
            renderPage(stack, bookPosRot, true, light);
            renderPage(stack, bookPosRot, false, light);
        }

        if (pageChangeState == PageChangeState.NONE) {
            for (int i = 0; i <= 1; i++) {
                OBBClientUtil.renderOBB(stack, pageTurnBoxes[i], false, 1f, 1f, 1f, 1f);
            }
        } else if (!pageChangeState.isAnim) {
            OBBClientUtil.renderOBB(stack, pageTurnBoxes[2], false, 1f, 1f, 1f, 1f);
        }

        for (OBB obb : obbs) {
            OBBClientUtil.renderOBB(stack, obb, false, 1f, 1f, 1f, 1f);
        }
    }

    protected void renderPage(PoseStack stack, PosRot bookPosRot, boolean leftPage, int light) {
        Vec3 awayFromBookUp = getAwayVector(bookPosRot);

        Vec3 pageUp = bookPosRot.getDir();
        Vec3 left = getLeftRightVector(bookPosRot, leftPage); // Should be called "right" for right page
        Vec3 posBase = bookPosRot.getPos().add(left.scale(singlePageWidth / 2d))
                .add(awayFromBookUp.scale(textUpAmount));

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();


        for (BookRenderable renderable : renderables) {
            stack.pushPose();
            Vec3 renderableOffset = renderable.getStartOffset(this, leftPage, bookPosRot);
            Vec3 pos = posBase.add(pageUp.scale(pageHalfHeight * renderableOffset.y))
                    .add(left.scale(singlePageWidth / -2d * renderableOffset.x))
                    .add(awayFromBookUp.scale(textUpAmount * renderableOffset.z));
            stack.translate(-cameraInfo.getPosition().x + pos.x,
                    -cameraInfo.getPosition().y + pos.y,
                    -cameraInfo.getPosition().z + pos.z);
            stack.mulPose(Vector3f.YN.rotation(bookPosRot.getYawF() + (float) Math.PI / 2f));
            stack.mulPose(Vector3f.ZP.rotation(bookPosRot.getPitchF()));
            stack.mulPose(Vector3f.XP.rotationDegrees(90f + (leftPage ? pageTilt : -pageTilt)));
            stack.mulPose(Vector3f.ZP.rotationDegrees(270f));
            stack.mulPose(Vector3f.YP.rotation(bookPosRot.getRollF()));
            renderable.render(stack, this, leftPage, light, bookPosRot);
            stack.popPose();
        }
    }
}
