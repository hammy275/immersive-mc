package net.blf02.immersivemc.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.blf02.immersivemc.ImmersiveMC;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class Cube1x1 extends Model {
    private final ModelRenderer bb_main;

    public static final ResourceLocation textureLocation = new ResourceLocation(ImmersiveMC.MOD_ID,
            "cube.png");

    public Cube1x1() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 16;
        texHeight = 16;

        bb_main = new ModelRenderer(this);
        bb_main.setPos(0.0F, 0.0F, 0.0F);
        bb_main.texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void renderToBuffer(MatrixStack stack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void render(MatrixStack stack, IVertexBuilder buffer,
                       float red, float green, float blue, float alpha, float size) {
        // size is in blocks
        stack.pushPose();
        stack.scale(size * 16f, size * 16f, size * 16f);
        renderToBuffer(stack, buffer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        stack.popPose();
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
