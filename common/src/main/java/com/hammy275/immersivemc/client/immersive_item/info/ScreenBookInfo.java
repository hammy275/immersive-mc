package com.hammy275.immersivemc.client.immersive_item.info;

import com.hammy275.immersivemc.client.immersive.book.BookRenderable;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ScreenBookInfo extends AbstractItemInfo {

    public Screen screen;
    public final ClientBookData bookData = new ClientBookData(1.25f, 0f);

    public ScreenBookInfo(ItemStack item, InteractionHand hand, Screen screen) {
        super(item, hand);
        setScreen(screen);
        bookData.renderables.add(new ScreenRenderable());
    }

    public void setScreen(@Nullable Screen screen) {
        this.screen = screen;
        if (screen != null) {
            screen.init(Minecraft.getInstance(), 640, 360);
        }
    }

    protected class ScreenRenderable implements BookRenderable {

        private static final Vec3 offset = new Vec3(-1.08, 0.75, -0.3);

        @Override
        public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            if (screen != null && leftPage) {
                stack.pushPose();
                stack.mulPose(Axis.ZP.rotation((float) Math.PI));
                float size = 1/1024f;
                stack.scale(size, size, size);
                GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), stack, Minecraft.getInstance().renderBuffers().bufferSource());
                screen.render(graphics, 0, 0, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
                stack.popPose();
                LevelRenderer.renderLineBox(stack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
                        AABB.ofSize(Vec3.ZERO, 0.1, 0.1, 0.1),
                        1, 1, 1, 1);
                Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            }
        }

        @Override
        public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot) {
            return offset;
        }
    }
}
