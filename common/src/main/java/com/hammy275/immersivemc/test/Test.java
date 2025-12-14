package com.hammy275.immersivemc.test;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Class for tests run by {@link Tests}.
 * <br>
 * All public methods that start with "test" are tests that will be run.
 * <br>
 * All test methods should take the same parameter types in the same order as {@link #setup(ServerPlayer)} and
 * {@link #teardown(ServerPlayer)}.
 * <br>
 * All test methods should return a String or void. An empty string or null is a passing test, while a non-empty string
 * is a test failure. A test that returns void will always pass if it doesn't throw.
 * <br>
 * Test objects should be reusable! As such, all initialization should take place within {@link #setup(ServerPlayer)}
 * and all teardowns in {@link #teardown(ServerPlayer)}.
 * <br>
 * This interface contains some utility methods. One shouldn't overwrite these!
 */
public interface Test {

    /**
     * Setup method called before every test. This method will always have an empty body in this interface.
     * @param player Player performing tests.
     */
    default void setup(ServerPlayer player) {}

    /**
     * Teardown method called after every test. This method will always have an empty body in this interface.
     * This method will always be called, even with a test failure. However, if a test fails and teardown fails,
     * only the test failure will be logged.
     * @param player Player performing tests.
     */
    default void teardown(ServerPlayer player) {}

    /**
     * Fast-forwards the game by some number of ticks, blocking until done.
     * @param player Player performing tests.
     * @param numTicks Number of ticks to fast-forward by.
     */
    default void fastForward(ServerPlayer player, int numTicks) {
        for (int i = 0; i < numTicks; i++) {
            player.level().tick(() -> false);
        }
    }

    /**
     * Create a testing area to easily perform tests within. Useful to run before each tests to have a "fresh slate".
     * @param player Player performing tests.
     */
    default void createTestingArea(ServerPlayer player) {
        player.level().getEntities(player, AABB.ofSize(player.position(), 20, 20, 20)).forEach(Entity::discard);
        BlockPos floorCenter = player.blockPosition().below();
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    player.level().setBlock(floorCenter.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                player.level().setBlock(floorCenter.offset(x, 0, z), Blocks.BEDROCK.defaultBlockState(), 3);
            }
        }
    }

    default void lookAt(ServerPlayer player, BlockPos pos) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos));
    }

    default void setBlock(ServerPlayer player, BlockPos pos, Block block) {
        setBlock(player, pos,  block.defaultBlockState());
    }

    default void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        player.level().setBlock(pos, state, 3);
    }

    default void setItemInMainHand(ServerPlayer player, Item item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
    }

    default BlockPos fromVec3(Vec3 vec3) {
        return BlockPos.containing(vec3);
    }

    default ServerLevel level(ServerPlayer player) {
        return player.level();
    }

    default void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionFailure("Expected true but was false!");
        }
    }

    default void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionFailure("Expected false but was true!");
        }
    }

    default <T> void assertEquals(T expected, T actual) {
        if (expected.getClass() == actual.getClass()) {
            Class<?> clazz = expected.getClass();
            if (clazz == ItemStack.class) {
                if (!ItemStack.matches((ItemStack) expected, (ItemStack) actual)) {
                    throw new AssertionFailure("Expected '%s' but got '%s'!".formatted(expected, actual));
                }
                return;
            }
        }
        if (!Objects.equals(expected, actual)) {
            throw new AssertionFailure("Expected '%s' but got '%s'!".formatted(expected, actual));
        }
    }

    default void assertEmpty(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            throw new AssertionFailure("Expected empty ItemStack, but got %s!".formatted(itemStack));
        }
    }

    default void assertEmptyMainHand(ServerPlayer player) {
        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!handItem.isEmpty()) {
            throw new AssertionFailure("Expected empty main hand, but got %s!".formatted(handItem));
        }
    }
}
