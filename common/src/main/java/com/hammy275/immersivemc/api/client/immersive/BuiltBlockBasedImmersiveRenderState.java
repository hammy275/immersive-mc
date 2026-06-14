package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Render state for {@link BuiltBlockBasedImmersive}. Allows access to the extra render data assuming the class and converter
 * are non-null when creating the Immersive using
 * {@link BlockBasedImmersiveBuilder#create(BlockBasedImmersiveHandler, Class, Class, BiConsumer)} or
 * {@link BlockBasedImmersiveBuilder#copy(BlockBasedImmersiveHandler, Class, Class, BiConsumer)}.
 * <p>
 * Do not implement this interface yourself! Instead implement {@link ImmersiveRenderState} directly.
 * @param <ER> The type of the extra render data.
 */
public interface BuiltBlockBasedImmersiveRenderState<ER> extends ImmersiveRenderState {

    /**
     * @return The position of the block this render state represents.
     */
    public BlockPos getBlockPos();

    /**
     * @return The extra render data, or null if no render data class and converter is defined.
     */
    @Nullable
    public ER getExtraRenderData();
}
