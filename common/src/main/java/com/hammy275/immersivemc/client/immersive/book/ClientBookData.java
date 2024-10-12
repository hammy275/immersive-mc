package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.common.immersive.CommonBookData;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.PageChangeState;
import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Data used only by the client for a book that can be used in Immersives. Has everything from its parent class, along
 * with the ability to add renderables and interactables for rendering and allowing interactions. Interactions are
 * only handled for VR users.
 */
public class ClientBookData extends CommonBookData {
    private static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    private static final ResourceLocation writtenBookTexture = new ResourceLocation(ImmersiveMC.MOD_ID, "nahnotfox_written_book.png");

    public final List<BookInteractable> interactables = new ArrayList<>();
    public final List<BookRenderable> renderables = new ArrayList<>();

    protected final List<OBB> obbs = new ArrayList<>();

    public ClientBookData() {
        super();
        this.pageTurner = Minecraft.getInstance().player;
    }

    /**
     * Should be called every tick.
     * @param bookPosRot PosRot of the book itself.
     * @param others PosRots for things that can interact with the book (VR controllers).
     */
    @Override
    public void tick(PosRot bookPosRot, PosRot... others) {
        super.tick(bookPosRot, others);
        obbs.clear();
        BookInteractable[] interacted = new BookInteractable[others.length];
        for (BookInteractable interactable : interactables) {
            OBB obb = interactable.getOBB();
            obbs.add(obb);
            for (int o = 0; o < others.length; o++) {
                PosRot other = others[o];
                if (interacted[o] == null && obb.contains(other.position())) {
                     interacted[o] = interactable;
                }
            }
        }

        for (int o = 0; o < others.length; o++) {
            if (interacted[o] != null) {
                if (Minecraft.getInstance().options.keyAttack.isDown()) {
                    interacted[o].interact(this, bookPosRot, others[o]);
                } else {
                    interacted[o].hover(this, bookPosRot, others[o]);
                }
            }
        }
    }

    /**
     * Should be called when rendering.
     * @param stack The PoseStack to render with.
     * @param light The light level being rendered at.
     * @param bookPosRot The PosRot of the book.
     */
    public void render(PoseStack stack, int light, PosRot bookPosRot) {
        stack.pushPose();

        Vec3 pos = bookPosRot.position();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        stack.scale(scaleSize, scaleSize, scaleSize);

        stack.mulPose(Axis.YN.rotationDegrees(bookPosRot.getYaw() + 90f));
        stack.mulPose(Axis.ZP.rotationDegrees(90f));
        stack.mulPose(Axis.ZP.rotationDegrees(bookPosRot.getPitch()));
        stack.mulPose(Axis.YN.rotationDegrees(bookPosRot.getRoll()));

        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                0, // Partial tick time is always 0 to have page stay in one constant spot
                leftPageTurn, // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                rightPageTurn, // 0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
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

    /**
     * Merges a {@link CommonBookData} instance from the server into this instance. Important to keep the
     * server and client in-sync.
     * @param fromServer Data from the server.
     */
    public void mergeFromServer(CommonBookData fromServer) {
        this.leftPageIndex = fromServer.leftPageIndex;
        this.leftPageTurn = fromServer.leftPageTurn;
        this.rightPageTurn = fromServer.rightPageTurn;
        this.pageChangeState = fromServer.pageChangeState;
    }

    public List<OBB> getPageTurnHitboxes() {
        return List.of(pageTurnBoxes);
    }

    public List<OBB> getInteractableHitboxes() {
        return interactables.stream().map(BookInteractable::getOBB).toList();
    }

    protected void renderPage(PoseStack stack, PosRot bookPosRot, boolean leftPage, int light) {
        stack.pushPose();

        Vec3 awayFromBookUp = getAwayVector(bookPosRot);

        Vec3 pageUp = bookPosRot.getLookAngle();
        Vec3 left = getLeftRightVector(bookPosRot, leftPage); // Should be called "right" for right page
        Vec3 pos = bookPosRot.position().add(pageUp.scale(pageHalfHeight)).add(left.scale(singlePageWidth / 2d))
                .add(awayFromBookUp.scale(textUpAmount));

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);
        stack.mulPose(Axis.YN.rotationDegrees(bookPosRot.getYaw() + 90f));
        stack.mulPose(Axis.ZP.rotationDegrees(bookPosRot.getPitch()));
        stack.mulPose(Axis.XP.rotationDegrees(90f + (leftPage ? pageTilt : -pageTilt)));
        stack.mulPose(Axis.ZP.rotationDegrees(270f));
        stack.mulPose(Axis.YP.rotationDegrees(bookPosRot.getRoll()));

        for (BookRenderable renderable : renderables) {
            renderable.render(stack, this, leftPage, light, bookPosRot);
        }

        stack.popPose();
    }
}
