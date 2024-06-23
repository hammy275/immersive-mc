package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A version of the {@link ImmersiveHandler} with support for multiblock structures. Multiblocks in this context
 * are Immersives that consist of multiple blocks that should be treated as one Immersive and also need to be treated
 * as such. For example, a double chest IS a multiblock since it consists of multiple blocks and needs to be treated
 * as one Immersive "instance". On the other hand, a beacon is NOT a multiblock by this definition, as the only block
 * that players interact with immersively is the beacon block itself, rather than any of the pyramid blocks that
 * are placed below the beacon.
 * <br>
 * All {@link ImmersiveHandler} methods that take a block position now represent some block position in the
 * multiblock. Which block position is passed is not specified by the API, so it may be any block position in
 * your multiblock.
 * <br>
 * Immersives that are built for MultiblockImmersiveHandlers may need to be ready to adjust if a new part of the
 * multiblock is suddenly added. For example, if a chest Immersive's info stored a value containing the secondary
 * chest of a double chest, this field may need to be updated on every tick in case such a second chest is placed, or
 * if a second chest is removed.
 */
public interface MultiblockImmersiveHandler<S extends NetworkStorage> extends ImmersiveHandler<S> {

    /**
     * This method should return all blocks that are part of this multiblock, given the position of one block
     * in this multiblock. For example, if this multiblock was for a double-chest, the position provided to calls
     * of this function may be either the left chest or the right chest, and should return a set that contains
     * BOTH the left chest AND the right chest. Note that if the multiblock is in a correct state (no part of the
     * multiblock has been broken), {@link ImmersiveHandler#isValidBlock(BlockPos, Level)} should return true
     * for all blocks of the multiblock. This way, when a block in the multiblock breaks, the aforementioned function
     * will return false, thus alerting ImmersiveMC that the multiblock is no longer valid.
     * <br>
     * If your multiblock isn't always a multiblock (such as chests, which can sometimes simply be single-chests), you
     * can simply return a set containing the provided block position.
     * <br>
     * Some additional rules about this function:
     * <ul>
     *     <li>If the block provided is unable to make a valid multiblock (such as if the multiblock is still
     *     under construction, or it was broken), you may return null. You may also simply return a set which contains
     *     at least one element that returns false for {@link ImmersiveHandler#isValidBlock(BlockPos, Level)}.</li>
     *     <li>You are not required to check if all blocks of the multiblock are valid. ImmersiveMC will check
     *     if every block in your multiblock is valid.</li>
     *     <li>Rarely, a multiblock may change shape while never ending up in an invalid state. For example, the left
     *     chest of a double chest may become the right chest of a double chest via command blocks. Your Immersive
     *     should handle this gracefully.</li>
     *     <li>This function should NOT return an empty set or a set containing null elements.</li>
     * </ul>
     * @param pos The position of one block in this multiblock.
     * @param level The level where one block of this multiblock is found.
     * @return All blocks that make up this multiblock, or null if the provided position cannot make a valid multiblock.
     *         This set will not be modified by ImmersiveMC, and it should NOT contain null nor should it be an empty
     *         set.
     */
    @Nullable
    public Set<BlockPos> getHandledBlocks(BlockPos pos, Level level);
}
