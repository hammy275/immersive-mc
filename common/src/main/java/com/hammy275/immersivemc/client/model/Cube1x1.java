package com.hammy275.immersivemc.client.model;

import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class Cube1x1 extends Model<Void> {
    public static final ResourceLocation textureLocation = Util.id("immersive/cube.png");

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Util.id("cube"), "main");
    private final ModelPart bb_main;

    public Cube1x1(ModelPart root) {
        super(root, RenderType::entityCutoutNoCull);
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public void render(PoseStack stack, VertexConsumer consumer, int color, float size, int light) {
        stack.pushPose();
        stack.scale(size * 16f, size * 16f, size * 16f);
        renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, color);
        stack.popPose();
    }
}
