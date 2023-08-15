package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive_item.info.WrittenBookInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.debug.DevModeData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WrittenBookImmersive extends AbstractItemImmersive<WrittenBookInfo> {
    /*
        Notes:
        - Book is 14 lines long, with 20 characters per line
        - Page turning is done using a central hitbox and edge hitboxes. Hand moves from edge to center to confirm turn.
        If it doesn't make it to center, cancel the turn.
     */

    public static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    public static final ResourceLocation writtenBookTexture = new ResourceLocation(ImmersiveMC.MOD_ID, "written_book.png");

    // User Controlled
    public static final float scaleSize = 1f;

    // Derived from user controlled
    public static final double halfPageWidth = scaleSize * 0.3d;
    public static final double pageHalfHeight = scaleSize / 4d;
    public static final float textStackScaleSize = -scaleSize * 0.0025f;
    public static final double textUpAmount = 0.2 * (scaleSize / 2f);
    public static final double hitboxUpAmount = textUpAmount + (scaleSize / 12f);
    public static final double hitboxMoveMinAmount = halfPageWidth;
    public static final double hitboxMoveMaxAmount = halfPageWidth + (scaleSize * 0.15d);

    // Helpful constants
    public static final double moveScaleForCenter = 0.15;
    public static final float pageTilt = 11f;
    public static final int bookLines = 14;
    public static final int charsPerLine = 20;

    /*
     * Pitch is 0 forward, with 30 up and -30 down
     * Yaw is just point direction
     */

    @Override
    protected void render(WrittenBookInfo info, PoseStack stack, IVRData hand) {
        stack.pushPose();

        Vec3 pos = hand.position();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        stack.scale(scaleSize, scaleSize, scaleSize);

        stack.mulPose(Vector3f.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(hand.getPitch()));

        float pageOneProgress = 0.0f;
        float pageTwoProgress = 0.0f;
        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                0, // Partial tick time is always 0 to have page stay in one constant spot
                pageOneProgress, // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                pageTwoProgress, //0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
                bookOpenAmount // How open the book is. A good range seems to be (0f,1.2f]
        );
        bookModel.render(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entitySolid(writtenBookTexture)),
                15728880, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();

        renderPage(stack, hand, info.left, true);
        renderPage(stack, hand, info.right, false);

        if (info.prevHitbox != null) {
            AbstractImmersive.renderHitbox(stack, info.prevHitbox, info.prevHitbox.getCenter(),
                    false, 1, 1, 1);
        }

        if (info.nextHitbox != null) {
            AbstractImmersive.renderHitbox(stack, info.nextHitbox, info.nextHitbox.getCenter(),
                    false, 1, 1, 1);
        }

        if (info.centerHitbox != null) {
            AbstractImmersive.renderHitbox(stack, info.centerHitbox, info.centerHitbox.getCenter(),
                    false, 1, 1, 1);
        }

        if (info.bookHitbox != null) {
            AbstractImmersive.renderHitbox(stack, info.bookHitbox, info.bookHitbox.getCenter(),
                    false, 0.5f, 0.5f, 0.5f);
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }

    protected void renderPage(PoseStack stack, IVRData hand, FormattedText textRaw, boolean leftPage) {
        stack.pushPose();

        Vec3 up = hand.getLookAngle();
        Vec3 left = getLeftRight(hand, leftPage); // Should be called "right" for right page
        Vec3 pos = hand.position().add(up.scale(pageHalfHeight)).add(left.scale(halfPageWidth / 2d))
                .add(new Vec3(0, textUpAmount, 0));

        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);
        stack.mulPose(Vector3f.YN.rotationDegrees(hand.getYaw() + 90f));
        stack.mulPose(Vector3f.ZP.rotationDegrees(hand.getPitch()));
        stack.mulPose(Vector3f.XP.rotationDegrees(90f + (leftPage ? pageTilt : -pageTilt)));
        stack.mulPose(Vector3f.ZP.rotationDegrees(270f));
        stack.scale(textStackScaleSize, textStackScaleSize, textStackScaleSize);

        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> text = font.split(textRaw, 114);
        int lineNum = 0;
        for (FormattedCharSequence seq : text) {
            // -56f is used to make the text left-aligned.
            font.drawInBatch(seq, -56f, 32 + lineNum++ * 9, 0xFF000000, false,
                    stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(),
                    false, 0, AbstractImmersive.maxLight);
        }

        stack.popPose();
    }

    @Override
    protected void tick(WrittenBookInfo info, IVRData hand) {
        // TODO: Remove
        DevModeData.rightRot = new Vec3(0.8, 0.2, 0).normalize();
        // TODO: End Remove
        ItemStack book = info.item;
        if (info.pageChanged()) {
            BookViewScreen.WrittenBookAccess access = new BookViewScreen.WrittenBookAccess(book);
            info.left = access.getPage(info.getLeftPageIndex());
            info.right = access.getPage(info.getRightPageIndex());
            info.setPageChanged(false);
        }


        Vec3 up = hand.getLookAngle();
        Vec3 down = up.scale(-1);
        Vec3 left = getLeftRight(hand, true);
        Vec3 right = getLeftRight(hand, false);

        Vec3 leftUpBoxPos = hand.position().add(up.scale(pageHalfHeight)).add(left.scale(hitboxMoveMinAmount))
                .add(new Vec3(0, hitboxUpAmount, 0));
        Vec3 leftDownBoxPos = hand.position().add(down.scale(pageHalfHeight)).add(left.scale(hitboxMoveMaxAmount));
        Vec3 rightUpBoxPos = hand.position().add(up.scale(pageHalfHeight)).add(right.scale(hitboxMoveMinAmount))
                .add(new Vec3(0, hitboxUpAmount, 0));
        Vec3 rightDownBoxPos = hand.position().add(down.scale(pageHalfHeight)).add(right.scale(hitboxMoveMaxAmount));
        Vec3 centerUpBoxPos = hand.position().add(up.scale(pageHalfHeight)).add(left.scale(hitboxMoveMinAmount).scale(moveScaleForCenter))
                .add(new Vec3(0, hitboxUpAmount, 0));
        Vec3 centerDownBoxPos = hand.position().add(down.scale(pageHalfHeight)).add(left.scale(hitboxMoveMaxAmount).scale(-moveScaleForCenter));


        info.prevHitbox = new AABB(leftDownBoxPos, leftUpBoxPos);
        info.nextHitbox = new AABB(rightDownBoxPos, rightUpBoxPos);
        info.centerHitbox = new AABB(centerDownBoxPos, centerUpBoxPos);
        info.bookHitbox = new AABB(leftDownBoxPos, rightUpBoxPos);


    }

    @Override
    protected boolean itemMatches(ItemStack item) {
        return item.getItem() == Items.WRITTEN_BOOK;
    }

    @Override
    protected WrittenBookInfo createInfo(ItemStack item, InteractionHand hand) {
        return new WrittenBookInfo(item, hand);
    }

    private Vec3 getLeftRight(IVRData hand, boolean left) {
        Vec3 look = hand.getLookAngle();
        Vector3f leftF = new Vector3f((float) look.x(), (float) look.y(), (float) look.z());
        leftF.transform(Vector3f.YN.rotationDegrees(left ? 270 : 90));
        return new Vec3(leftF.x(), Math.abs(leftF.y()), leftF.z());
    }
}
