package com.hammy275.immersivemc.common.util;


import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.MultiblockImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.immersive.ImmersiveChecker;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Util {

    public static UseInfo activeUseInfo = null;

    public static ResourceLocation mcId(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
    
    public static ResourceLocation id(String path) {
        return id(ImmersiveMC.MOD_ID, path);
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static boolean hasTooLowVivecraftVersion() {
        // If missing Vivecraft or the API loaded successfully, we're on a compatible Vivecraft version
        return Platform.isModLoaded("vivecraft") && !VRVerify.hasAPI;
    }

    public static boolean blockIsActiveImmersive(Player player, BlockPos pos) {
        Level level = player.level();
        if (level.isClientSide()) {
            for (Immersive<?, ?> singleton : Immersives.IMMERSIVES) {
                if (singleton.getHandler() instanceof MultiblockImmersiveHandler<?> handler) {
                    // Need to actually do blockstate checks if a multiblock handler, since that data isn't known
                    for (ImmersiveInfo info : singleton.getTrackedObjects()) {
                        Set<BlockPos> handledBlocks = handler.getHandledBlocks(pos, level);
                        if (handledBlocks != null && handledBlocks.contains(info.getBlockPosition())) {
                            return true;
                        }
                    }
                } else {
                    // Can just look at stored block positions
                    for (ImmersiveInfo info : singleton.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(pos)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            return TrackedImmersives.TRACKED_IMMERSIVES.stream()
                    .anyMatch(data -> data.playerUUID.equals(player.getUUID()) && data.getPos().contains(pos));
        }
    }

    public static InteractionHand otherHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static InteractionResult doUse(Player player, InteractionHand hand, BlockPos pos) {
        InteractionResult result;
        try {
            activeUseInfo = new UseInfo(player, hand, pos);
            result = player.getItemInHand(hand).use(player.level(), player, hand);
        } finally {
            activeUseInfo = null;
        }
        return result;
    }

    /**
     * Gets a look angle from pitch/yaw.
     * @param pitch The pitch in radians.
     * @param yaw The yaw in radians.
     * @return The look angle represented by the pitch and yaw.
     */
    public static Vec3 getLookAngle(float pitch, float yaw) {
        float yawCos = Mth.cos(pitch);
        return new Vec3(Mth.sin(yaw) * yawCos, -Mth.sin(pitch), Mth.cos(yaw) * yawCos);
    }

    /**
     * Check if Immersive has valid blocks. This is equivalent to {@link ImmersiveHandler#isValidBlock(BlockPos, Level)}
     * for single-block Immersives, and running the
     * former method on all blocks in {@link MultiblockImmersiveHandler#getHandledBlocks(BlockPos, Level)} for
     * multiblock Immersives.
     * @param handler Handler to run on.
     * @param pos Position to check, or a position to check if part of a multiblock.
     * @param level Level to check in.
     * @return Whether all blocks are valid or not.
     */
    public static boolean isValidBlocks(ImmersiveHandler<?> handler, BlockPos pos, Level level) {
        return getValidBlocks(handler, pos, level).contains(pos);
    }

    /**
     * Checks if Immersive exactly matches the supplied set of blocks.
     * @param handler Handler.
     * @param pos Set of all positions in the Immersive.
     * @param level Level.
     * @return Whether the provided set of positions match the Immersive as it exists in-world.
     */
    public static boolean isValidBlocks(ImmersiveHandler<?> handler, Set<BlockPos> pos, Level level) {
        return getValidBlocks(handler, pos.iterator().next(), level).equals(pos);
    }

    /**
     * Get all blocks part of the Immersive at the specified position for the given handler.
     * @param handler Handler to get valid positions of.
     * @param pos A BlockPos that's part of the Immersive.
     * @param level The level to get in.
     * @return A set of valid positions for the Immersive, or an empty set if not valid.
     */
    public static Set<BlockPos> getValidBlocks(ImmersiveHandler<?> handler, BlockPos pos, Level level) {
        boolean valid = handler.isValidBlock(pos, level);
        if (valid) {
            if (handler instanceof MultiblockImmersiveHandler<?> mih) {
                Set<BlockPos> positions = mih.getHandledBlocks(pos, level);
                if (positions != null && positions.stream().allMatch(p -> handler.isValidBlock(p, level))) {
                    return positions;
                } else {
                    return Set.of();
                }
            } else {
                return Set.of(pos);
            }
        }
        return Set.of();
    }

    public static Vec3 average(Set<BlockPos> positions) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (BlockPos pos : positions) {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        return new Vec3(
                x / positions.size(),
                y / positions.size(),
                z / positions.size()
        );
    }

    public static boolean isThrowableItem(Item item) {
        return item == Items.EXPERIENCE_BOTTLE || item instanceof EggItem ||
                item == Items.ENDER_PEARL || item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION || item == Items.SNOWBALL ||
                item instanceof TridentItem || item instanceof FishingRodItem;
    }

    public static Direction horizontalDirectionFromLook(Vec3 look) {
        double maxLook = Math.max(
                Math.abs(look.x),
                Math.abs(look.z)
        );
        if (maxLook == Math.abs(look.x)) {
            return look.x < 0 ? Direction.WEST : Direction.EAST;
        } else {
            return look.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    public static boolean isHittingImmersive(BlockHitResult result, Level level) {
        BlockPos pos = result.getBlockPos();
        for (ImmersiveChecker checker : ImmersiveCheckers.CHECKERS) {
            if (checker.apply(pos, level)) {
                return true; // "I'm totally not crouching" if SHIFT+Right-clicking an immersive
            }
        }
        return false;
    }

    public static boolean hasItemInInventoryWithStackSpace(Player player, ItemStack stack) {
        for (ItemStack invItem : player.getInventory().getNonEquipmentItems()) {
            if (Util.stacksEqualBesidesCount(invItem, stack) && invItem.getCount() < invItem.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canPickUpItem(ItemEntity item, Player player) {
        /* It seems pickup delay isn't synced client side.
           Although this doesn't cover a lot of use cases, odds are, if an item isn't moving, it can be picked up
           Plus, it somewhat makes sense */
        return (!item.hasPickUpDelay() || player.getAbilities().instabuild)
                && Math.abs(item.getDeltaMovement().x) <= 0.01 && Math.abs(item.getDeltaMovement().z) <= 0.01;
    }

    public static Optional<Integer> rayTraceClosest(Pair<Vec3, Vec3> rayStartAndEnd, BoundingBox... targets) {
        return rayTraceClosest(rayStartAndEnd.getFirst(), rayStartAndEnd.getSecond(), targets);
    }

    /**
     * Find the closest BoundingBox that is ray traced through the ray.
     * @param rayStart Start of the ray.
     * @param rayEnd End of the ray.
     * @param targets List of targets.
     * @return Target in targets that intersects the ray and is closer to rayStart than all other intersecting targets.
     */
    public static Optional<Integer> rayTraceClosest(Vec3 rayStart, Vec3 rayEnd, BoundingBox... targets) {
        return rayTraceClosest(rayStart, rayEnd, Arrays.stream(targets).toList());
    }

    public static Optional<Integer> rayTraceClosest(Vec3 rayStart, Vec3 rayEnd, Collection<? extends HitboxInfo> targets) {
        return rayTraceClosest(rayStart, rayEnd, targets.stream().map((info) -> info != null ? info.getHitbox() : null).toList());
    }

    public static Optional<Integer> rayTraceClosest(Vec3 rayStart, Vec3 rayEnd, Iterable<BoundingBox> targets) {
        double dist = Double.MAX_VALUE;
        Integer winner = null;
        int i = 0;
        for (BoundingBox target : targets) {
            // This is needed since, with chest immersives for example, we don't know
            // if we have a single chest or double chest. As a result, we can have null targets.
            if (target != null) {
                // If the start or end of the ray is in the target hitbox, we immediately return true
                if (BoundingBox.contains(target, rayStart)) {
                    return Optional.of(i);
                }
                // Gets the "hit" for our ray.
                Optional<Vec3> closestHitOpt;
                if (target.isAABB()) {
                    closestHitOpt = target.asAABB().clip(rayStart, rayEnd);
                } else {
                    closestHitOpt = target.asOBB().rayHit(rayStart, rayEnd);
                }
                double distTemp = closestHitOpt.isPresent() ? closestHitOpt.get().distanceToSqr(rayStart) : -1;
                if (closestHitOpt.isPresent() && distTemp < dist) {
                    winner = i;
                    dist = distTemp;
                }
            }
            i++;
        }
        return Optional.ofNullable(winner);
    }

    public static Optional<Integer> getFirstIntersect(Vec3 pos, BoundingBox... targets) {
        return getFirstIntersect(pos, Arrays.stream(targets).toList());
    }

    public static Optional<Integer> getFirstIntersect(Vec3 pos, Collection<? extends HitboxInfo> targets) {
        return getFirstIntersect(pos, targets.stream().map((info) -> info != null ? info.getHitbox() : null).toList());
    }

    public static Optional<Integer> getFirstIntersect(Vec3 pos, Iterable<BoundingBox> targets) {
        int i = 0;
        for (BoundingBox target : targets) {
            if (target != null && BoundingBox.contains(target, pos)) {
                return Optional.of(i);
            }
            i++;
        }
        return Optional.empty();
    }

    public static Optional<Integer> getClosestIntersect(Vec3 pos, List<BoundingBox> targets) {
        int res = -1;
        double distanceToBeat = Double.MAX_VALUE;
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i) != null && BoundingBox.contains(targets.get(i), pos)) {
                double newDist = pos.distanceToSqr(BoundingBox.getCenter(targets.get(i)));
                if (newDist < distanceToBeat) {
                    distanceToBeat = newDist;
                    res = i;
                }
            }
        }
        return res == -1 ? Optional.empty() : Optional.of(res);
    }

    public static ChestBlockEntity getOtherChest(ChestBlockEntity chest) {
        return getOtherChest(chest, true);
    }

    protected static ChestBlockEntity getOtherChest(ChestBlockEntity chest, boolean checkOther) {
        // Gets the chest this one is connected to. Can be null.
        if (chest == null) return null;
        Direction otherDir = ChestBlock.getConnectedDirection(chest.getBlockState());
        BlockPos otherPos = chest.getBlockPos().relative(otherDir);
        if (chest.getLevel() != null && chest.getLevel().getBlockEntity(otherPos) instanceof ChestBlockEntity) {
            ChestBlockEntity other = (ChestBlockEntity) chest.getLevel().getBlockEntity(otherPos);
            if (checkOther && other != null) { // Make sure the chest we think we're connected to is connected back to us
                return getOtherChest(other, false) == chest ? other : null;
            }
            return other;
        }
        return null;
    }

    public static boolean stacksEqualBesidesCount(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        int oldCountA = a.getCount();
        int oldCountB = b.getCount();
        a.setCount(1);
        b.setCount(1);
        boolean res = ItemStack.matches(a, b);
        a.setCount(oldCountA);
        b.setCount(oldCountB);
        return res;
    }

    /**
     * Merges two ItemStacks together
     * @param mergeIntoIn ItemStack to merge into
     * @param mergeFromIn ItemStack to merge from
     * @param useCopy Whether or not to use copies of the ItemStacks supplied
     * @return An ItemStackMergeResult containing the results post-merge.
     * If no merge takes place, the returned result just contains the inputted ItemStacks.
     */
    public static ItemStackMergeResult mergeStacks(ItemStack mergeIntoIn, ItemStack mergeFromIn, boolean useCopy) {
        return mergeStacks(mergeIntoIn, mergeFromIn, useCopy, -1);
    }

    /**
     * Merges two ItemStacks together
     * @param mergeIntoIn ItemStack to merge into
     * @param mergeFromIn ItemStack to merge from
     * @param useCopy Whether or not to use copies of the ItemStacks supplied
     * @param forcedMaxMergeIntoSize A force maximum stack size for mergeIntoIn, or -1 to not force.
     * @return An ItemStackMergeResult containing the results post-merge.
     * If no merge takes place, the returned result just contains the inputted ItemStacks.
     */
    public static ItemStackMergeResult mergeStacks(ItemStack mergeIntoIn, ItemStack mergeFromIn, boolean useCopy, int forcedMaxMergeIntoSize) {
        int mergeIntoMaxStackSize = forcedMaxMergeIntoSize == -1 ? mergeIntoIn.getMaxStackSize() : forcedMaxMergeIntoSize;
        if (!stacksEqualBesidesCount(mergeIntoIn, mergeFromIn) || mergeIntoMaxStackSize <= 1) {
            return new ItemStackMergeResult(mergeIntoIn, mergeFromIn);
        }
        ItemStack into = useCopy ? mergeIntoIn.copy() : mergeIntoIn;
        ItemStack from = useCopy ? mergeFromIn.copy() : mergeFromIn;
        int totalCount = into.getCount() + from.getCount();
        int fromAmount = 0;
        if (totalCount > mergeIntoMaxStackSize) {
            fromAmount = totalCount - mergeIntoMaxStackSize;
            totalCount = mergeIntoMaxStackSize;
        }
        into.setCount(totalCount);
        from.setCount(fromAmount);
        return new ItemStackMergeResult(into, fromAmount == 0 ? ItemStack.EMPTY : from);
    }

    public static void setRepeater(Level level, BlockPos pos, int newDelay) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RepeaterBlock) {
            state = state.setValue(RepeaterBlock.DELAY, newDelay);
            level.setBlock(pos, state, 3);
        }
    }

    public static void useLever(Player player, BlockPos pos) {
        if (ImmersiveCheckers.isLever(pos, player.level())) {
            BlockState lever = player.level().getBlockState(pos);
            lever.useWithoutItem(player.level(), player,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, true));
        }
    }

    public static void useTrapdoor(Player player, Level level, BlockPos pos) {
        if (ImmersiveHandlers.trapdoorHandler.isValidBlock(pos, level)) {
            BlockState trapdoor = level.getBlockState(pos);
            trapdoor.useWithoutItem(level, player,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, true));
        }
    }

    public static void useDoor(Player player, Level level, BlockPos pos) {
        if (ImmersiveHandlers.doorHandler.isValidBlock(pos, level)) {
            BlockState door = level.getBlockState(pos);
            door.useWithoutItem(level, player,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, true));
        }
    }

    public static Vec3 getPlayerVelocity(Vec3 lastTickPos, Vec3 currentTickPos) {
        return new Vec3(currentTickPos.x - lastTickPos.x, currentTickPos.y - lastTickPos.y,
                currentTickPos.z - lastTickPos.z);
    }

    public static double moveTowardsZero(double num, double subtract) {
        subtract = Math.abs(subtract);
        if (subtract >= Math.abs(num)) {
            return 0;
        } else if (num < 0) {
            return num + subtract;
        }
        return num - subtract;
    }

    public static void giveStackHandFirst(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, stack);
        } else {
            placeLeftovers(player, stack);
        }
    }

    public static void placeLeftovers(Player player, ItemStack leftovers) {
        placeLeftovers(player, leftovers, player.position());
    }

    public static void placeLeftovers(Player player, ItemStack leftovers, Vec3 pos) {
        if (!leftovers.isEmpty()) {
            ItemEntity item = new ItemEntity(player.level(), pos.x, pos.y, pos.z, leftovers);
            player.level().addFreshEntity(item);
        }
    }

    public static void putResourceLocation(CompoundTag nbt, String key, ResourceLocation loc) {
        CompoundTag locTag = new CompoundTag();
        locTag.putString("namespace", loc.getNamespace());
        locTag.putString("path", loc.getPath());
        nbt.put(key, locTag);
    }

    public static ResourceLocation getResourceLocation(CompoundTag nbt, String key) {
        CompoundTag subTag = nbt.getCompound(key).get();
        return Util.id(subTag.getString("namespace").get(), subTag.getString("path").get());
    }

    public static List<BlockPos> allPositionsWithAABB(AABB box) {
        List<BlockPos> positions = new ArrayList<>();
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    public static Vec3[] get3x3HorizontalGrid(BlockPos blockPos, double spacing, Direction blockForward,
                                              boolean use3DCompat) {
        Vec3 pos = Vec3.upFromBottomCenterOf(blockPos, 1);
        if (use3DCompat) {
            pos = pos.add(0, 1d/16d, 0);
        }
        Direction left = blockForward.getCounterClockWise();

        Vec3 leftOffset = new Vec3(
                left.getUnitVec3i().getX() * -spacing, 0, left.getUnitVec3i().getZ() * -spacing);
        Vec3 rightOffset = new Vec3(
                left.getUnitVec3i().getX() * spacing, 0, left.getUnitVec3i().getZ() * spacing);

        Vec3 topOffset = new Vec3(
                blockForward.getUnitVec3i().getX() * -spacing, 0, blockForward.getUnitVec3i().getZ() * -spacing);
        Vec3 botOffset = new Vec3(
                blockForward.getUnitVec3i().getX() * spacing, 0, blockForward.getUnitVec3i().getZ() * spacing);


        return new Vec3[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
        };
    }

    public static Direction getForwardFromPlayerUpAndDown(Player player, BlockPos pos) {
        return getForwardFromPlayerUpAndDownFilterBlockFacing(player, pos, false);
    }

    /**
     * Same as getForwardFromPlayer, but can return the block facing up or down, alongside any of the four
     * directions of N/E/S/W.
     * @param player Player.
     * @param pos Position of block.
     * @param filterOnBlockFacing If true, the axis of the block's DirectionalBlock.FACING will not be returned from
     *                            this function. The block should have this property if this is true, of course!
     * @return Any Direction, representing what direction the block should be facing based on the player's position.
     */
    public static Direction getForwardFromPlayerUpAndDownFilterBlockFacing(Player player, BlockPos pos, boolean filterOnBlockFacing) {
        Direction.Axis filter = filterOnBlockFacing ? player.level().getBlockState(pos).getValue(DirectionalBlock.FACING).getAxis() : null;
        Vec3 playerPos = player.position();
        if (playerPos.y >= pos.getY() + 0.625 && filter != Direction.Axis.Y) {
            return Direction.UP;
        } else if (playerPos.y <= pos.getY() - 0.625 && filter != Direction.Axis.Y) {
            return Direction.DOWN;
        } else {
            Direction forward = ImmersiveLogicHelpers.instance().getHorizontalBlockForward(player, pos);
            if (forward.getAxis() != filter) {
                return forward;
            } else {
                // We filter on non-Y axis, and getForwardFromPlayer was on our filter. Find the closest and get it.
                Direction blockFacing = player.level().getBlockState(pos).getValue(DirectionalBlock.FACING);
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                Direction blockLeftDir = blockFacing.getCounterClockWise();
                Vec3 blockLeftVec = blockLeftDir.getUnitVec3();
                Vec3 counterClockwisePos = blockCenter.add(blockLeftVec.scale(0.5));
                Vec3 clockwisePos = blockCenter.add(blockLeftVec.scale(-0.5));
                Vec3 upPos = blockCenter.add(0, 0.5, 0);
                Vec3 downPos = blockCenter.add(0, -0.5, 0);

                double counterClockwiseDist = counterClockwisePos.distanceToSqr(playerPos);
                double clockwiseDist = clockwisePos.distanceToSqr(playerPos);
                double upDist = upPos.distanceToSqr(playerPos);
                double downDist = downPos.distanceToSqr(playerPos);

                double min = Math.min(counterClockwiseDist, clockwiseDist);
                min = Math.min(min, upDist);
                min = Math.min(min, downDist);

                if (min == counterClockwiseDist) {
                    return forward.getCounterClockWise();
                } else if (min == clockwiseDist) {
                    return forward.getClockWise();
                } else if (min == upDist) {
                    return Direction.UP;
                } else {
                    return Direction.DOWN;
                }
            }
        }
    }

    public static class ItemStackMergeResult {

        public final ItemStack mergedInto;
        public final ItemStack mergedFrom;

        public ItemStackMergeResult(ItemStack mergedInto, ItemStack mergedFrom) {
            this.mergedInto = mergedInto;
            this.mergedFrom = mergedFrom;
        }

        @Override
        public String toString() {
            return "Merged Into: " + this.mergedInto + "\n" + "Merged From: " + this.mergedFrom;
        }
    }

    public record UseInfo(Player player, InteractionHand hand, BlockPos pos) {
        public Vec3 getVec3Pos() {
            return Vec3.atCenterOf(pos);
        }
    }
}
