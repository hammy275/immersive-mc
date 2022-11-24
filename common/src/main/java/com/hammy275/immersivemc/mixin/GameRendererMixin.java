package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method="renderLevel", at=
            {@At(value = "INVOKE_STRING", args="ldc=hand", target="Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"),
                    // This one is for non-mixin Vivecraft. Can remove once that is phased out.
            @At(value = "INVOKE_STRING", args="ldc=ShadersEnd", target="Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V")}
    )
    private void renderLevelLast(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        ClientRenderSubscriber.onWorldRender(poseStack);
    }
}
