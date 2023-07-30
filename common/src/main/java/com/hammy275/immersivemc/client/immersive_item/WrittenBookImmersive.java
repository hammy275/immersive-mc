package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.client.immersive_item.info.WrittenBookInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class WrittenBookImmersive extends AbstractItemImmersive<WrittenBookInfo> {

    public static final BookModel bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));

    @Override
    protected void render(WrittenBookInfo info, PoseStack stack, IVRData hand) {
        stack.pushPose();

        Vec3 pos = hand.position();
        Camera cameraInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.translate(-cameraInfo.getPosition().x + pos.x,
                -cameraInfo.getPosition().y + pos.y,
                -cameraInfo.getPosition().z + pos.z);

        float pageOneProgress = 0.25f;
        float pageTwoProgress = 0.75f;
        float bookOpenAmount = 1.1f;

        bookModel.setupAnim(
                Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getFrameTime(),
                pageOneProgress, // 0-1. How far the page is in the turn. Range is [0f, 1f] with 0f being left.
                pageTwoProgress, //0-1. How far across a different page is. Range is [0f, 1f] with 0f being left.
                bookOpenAmount // How open the book is. A good range seems to be (0f,1.2f]
        );
        bookModel.render(stack,
                EnchantTableRenderer.BOOK_LOCATION.buffer(
                        Minecraft.getInstance().renderBuffers().bufferSource(), RenderType::entitySolid
                ),
                15728880, OverlayTexture.NO_OVERLAY,
                1, 1, 1, 1);

        stack.popPose();

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }

    @Override
    protected void tick(WrittenBookInfo info, IVRData hand) {

    }

    @Override
    protected boolean itemMatches(ItemStack item) {
        return item.getItem() == Items.WRITTEN_BOOK;
    }

    @Override
    protected WrittenBookInfo createInfo(ItemStack item, InteractionHand hand) {
        return new WrittenBookInfo(item, hand);
    }
}
