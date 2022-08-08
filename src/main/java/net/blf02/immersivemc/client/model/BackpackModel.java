package net.blf02.immersivemc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

// 99% exported from BlockBench
public class BackpackModel extends Model {

    public static final ResourceLocation textureLocation = new ResourceLocation("textures/block/white_wool.png");

    private final ModelRenderer wall;
    private final ModelRenderer lower;
    private final ModelRenderer upper;
    private final ModelRenderer bb_main;

    public BackpackModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 32;
        texHeight = 32;

        wall = new ModelRenderer(this);
        wall.setPos(0.0F, 24.0F, 0.0F);


        lower = new ModelRenderer(this);
        lower.setPos(0.0F, 0.0F, 0.0F);
        wall.addChild(lower);
        lower.texOffs(0, 0).addBox(-6.0F, 1.0F, -7.0F, 12.0F, 10.0F, 1.0F, 0.0F, false);
        lower.texOffs(0, 0).addBox(-6.0F, 1.0F, 6.0F, 12.0F, 10.0F, 1.0F, 0.0F, false);
        lower.texOffs(0, 0).addBox(-7.0F, 1.0F, -6.0F, 1.0F, 10.0F, 12.0F, 0.0F, false);
        lower.texOffs(0, 0).addBox(6.0F, 1.0F, -6.0F, 1.0F, 10.0F, 12.0F, 0.0F, false);

        upper = new ModelRenderer(this);
        upper.setPos(0.0F, 0.0F, 0.0F);
        wall.addChild(upper);
        upper.texOffs(0, 0).addBox(-7.0F, 0.0F, -7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(6.0F, 0.0F, -7.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(6.0F, 0.0F, 6.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(-7.0F, 0.0F, 6.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(-6.0F, 0.0F, -8.0F, 12.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(-6.0F, 0.0F, 7.0F, 12.0F, 1.0F, 1.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(-8.0F, 0.0F, -6.0F, 1.0F, 1.0F, 12.0F, 0.0F, false);
        upper.texOffs(0, 0).addBox(7.0F, 0.0F, -6.0F, 1.0F, 1.0F, 12.0F, 0.0F, false);

        bb_main = new ModelRenderer(this);
        bb_main.setPos(0.0F, 24.0F, 0.0F);
        bb_main.texOffs(0, 0).addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F, 0.0F, false);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        wall.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(float x, float y, float z) {
        ModelRenderer[] models = new ModelRenderer[]{wall, bb_main};
        for (ModelRenderer modelRenderer : models) {
            modelRenderer.xRot = x;
            modelRenderer.yRot = y;
            modelRenderer.zRot = z;
        }
    }
}
