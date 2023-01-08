package com.hammy275.immersivemc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.hammy275.immersivemc.ImmersiveMC;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BackpackLowDetailModel extends Model {

    public static final ResourceLocation textureLocation = new ResourceLocation("textures/block/white_wool.png");

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ImmersiveMC.MOD_ID, "backpack_low_model"), "main");
    private final ModelPart wall;
    private final ModelPart bb_main;

    public BackpackLowDetailModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.wall = root.getChild("wall");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wall = partdefinition.addOrReplaceChild("wall", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition lower = wall.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(42, 13).addBox(-7.0F, 0.0F, -8.0F, 14.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(-7.0F, 0.0F, 7.0F, 14.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 23).addBox(-8.0F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(7.0F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper = wall.addOrReplaceChild("upper", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 11.0F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        wall.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
