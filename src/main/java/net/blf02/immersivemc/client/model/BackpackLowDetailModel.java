package net.blf02.immersivemc.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class BackpackLowDetailModel extends Model {

    public static final ResourceLocation textureLocation = new ResourceLocation("textures/block/white_wool.png");

    private final ModelRenderer wall;
    private final ModelRenderer lower;
    private final ModelRenderer upper;
    private final ModelRenderer bb_main;

    public BackpackLowDetailModel() {
        super(RenderType::entityCutoutNoCull);
        texWidth = 128;
        texHeight = 128;

        wall = new ModelRenderer(this);
        wall.setPos(0.0F, 24.0F, 0.0F);


        lower = new ModelRenderer(this);
        lower.setPos(0.0F, 0.0F, 0.0F);
        wall.addChild(lower);
        lower.texOffs(42, 13).addBox(-7.0F, 0.0F, -8.0F, 14.0F, 11.0F, 1.0F, 0.0F, false);
        lower.texOffs(36, 0).addBox(-7.0F, 0.0F, 7.0F, 14.0F, 11.0F, 1.0F, 0.0F, false);
        lower.texOffs(14, 23).addBox(-8.0F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F, 0.0F, false);
        lower.texOffs(0, 13).addBox(7.0F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F, 0.0F, false);

        upper = new ModelRenderer(this);
        upper.setPos(0.0F, 0.0F, 0.0F);
        wall.addChild(upper);


        bb_main = new ModelRenderer(this);
        bb_main.setPos(0.0F, 24.0F, 0.0F);
        bb_main.texOffs(0, 0).addBox(-7.0F, 11.0F, -7.0F, 14.0F, 1.0F, 14.0F, 0.0F, false);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        wall.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
