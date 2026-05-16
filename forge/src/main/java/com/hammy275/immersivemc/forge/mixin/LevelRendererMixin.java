package com.hammy275.immersivemc.forge.mixin;

import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @WrapMethod(method = "submitEntities")
    private void afterEntityRender(PoseStack poseStack, LevelRenderState renderState, SubmitNodeCollector nodeCollector, Operation<Void> original) {
        original.call(poseStack, renderState, nodeCollector);
        ClientRenderSubscriber.onWorldRender(poseStack);
        ClientRenderSubscriber.onTransparentRender(poseStack);
    }
}
