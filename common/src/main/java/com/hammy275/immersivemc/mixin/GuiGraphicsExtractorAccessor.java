package com.hammy275.immersivemc.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    @Accessor("guiRenderState")
    public GuiRenderState immersiveMC$getGuiRenderState();
}
