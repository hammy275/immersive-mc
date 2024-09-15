package com.hammy275.immersivemc.client.model;

// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hammy275.immersivemc.ImmersiveMC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BackpackBundleModel extends EntityModel<Entity> {
    public static final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "nahnotfox_bundle_bag.png");
    public static final ResourceLocation textureLocationColorable = ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "nahnotfox_bundle_bag_colorable.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "bundle_backpack"), "main");
    private final ModelPart bone;

    public BackpackBundleModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(40, 37).addBox(-7.0F, -11.0F, -7.0F, 14.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 15).addBox(-7.0F, -11.0F, 6.0F, 14.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 30).addBox(-7.0F, -11.0F, 7.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 26).addBox(-7.0F, -11.0F, -8.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 18).addBox(-8.0F, -11.0F, -8.0F, 1.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(7.0F, -11.0F, -8.0F, 1.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(26, 37).addBox(6.0F, -11.0F, -6.0F, 1.0F, 10.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-7.0F, -11.0F, -6.0F, 1.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 35.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float f, float g, float h, float i, float j) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}