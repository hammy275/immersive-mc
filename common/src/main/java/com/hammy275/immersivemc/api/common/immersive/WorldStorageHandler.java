package com.hammy275.immersivemc.api.common.immersive;

import com.google.common.annotations.Beta;
import com.hammy275.immersivemc.api.server.WorldStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * An {@link ImmersiveHandler} that saves and loads from world save data using {@link WorldStorage} instances. See
 * {@link WorldStorage} for more info.
 * <br>
 * Important info:
 * <ul>
 *     <li>ImmersiveMC only handles saving and loading {@link WorldStorage} instances. ImmersiveMC does NOT
 *     manage the creation or deletion of them. This means you'll need to handle creating {@link WorldStorage}
 *     instances (usually by calling
 *     {@link com.hammy275.immersivemc.api.server.WorldStorages#getOrCreate(BlockPos, ServerLevel)} in
 *     {@link ImmersiveHandler#makeInventoryContents(ServerPlayer, BlockPos)} and/or
 *     {@link ImmersiveHandler#swap(int, InteractionHand, BlockPos, ServerPlayer, com.hammy275.immersivemc.api.server.ItemSwapAmount)} and by
 *     calling {@link com.hammy275.immersivemc.api.server.WorldStorages#remove(BlockPos, ServerLevel)} in
 *     {@link ImmersiveHandler#onStopTracking(ServerPlayer, BlockPos)}.</li>
 * </ul>
 */
@Beta
public interface WorldStorageHandler<S extends NetworkStorage> extends ImmersiveHandler<S> {

    /**
     * Since the retrieval and saving of world storages are server-side only, this Immersive cannot be client
     * authoritative.
     */
    @Override
    default boolean clientAuthoritative() {
        return false;
    }

    /**
     * @return An empty WorldStorage to load from NBT in.
     */
    public WorldStorage getEmptyWorldStorage();

    /**
     * @return The class this handler's world storage uses.
     */
    public Class<? extends WorldStorage> getWorldStorageClass();

}
