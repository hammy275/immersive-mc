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
    public final ClientBookData bookData = new ClientBookData();

    public ScreenBookInfo(ItemStack item, InteractionHand hand, Screen screen) {
        super(item, hand);
        setScreen(screen);
        bookData.renderables.add(new ScreenRenderable());
    }

    public void setScreen(@Nullable Screen screen) {
        this.screen = screen;
        if (screen != null) {
            screen.init(Minecraft.getInstance(), 1280, 720);
        }
    }

    protected class ScreenRenderable implements BookRenderable {

        private static final Vec3 offset = new Vec3(-1, 1, 5);

        @Override
        public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot) {
            if (screen != null) {
                stack.pushPose();
                stack.mulPose(Axis.ZP.rotation((float) Math.PI));
                float size = 1/2048f;
                stack.scale(size, size, size);
                GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), stack, Minecraft.getInstance().renderBuffers().bufferSource());
                screen.render(graphics, 0, 0, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks());
                stack.popPose();
                LevelRenderer.renderLineBox(stack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
                        AABB.ofSize(Vec3.ZERO, 0.1, 0.1, 0.1),
                        1, 1, 1, 1);
            }
        }

        @Override
        public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot) {
            if (leftPage) {
                return new Vec3(-1, 1, 0.31);
            } else {
                return new Vec3(3.1, 1, -0.9575);
            }
        }
    }
}
