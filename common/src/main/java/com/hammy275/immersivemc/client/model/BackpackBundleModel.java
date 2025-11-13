package com.hammy275.immersivemc.client.model;

// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class BackpackBundleModel extends EntityModel<EntityRenderState> {
    public static final ResourceLocation textureLocation = Util.id("immersive/bag/nahnotfox_bundle_bag.png");
    public static final ResourceLocation textureLocationColorable = Util.id("immersive/bag/nahnotfox_bundle_bag_colorable.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Util.id("bundle_backpack"), "main");

    public BackpackBundleModel(ModelPart root) {
        super(root);
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
}