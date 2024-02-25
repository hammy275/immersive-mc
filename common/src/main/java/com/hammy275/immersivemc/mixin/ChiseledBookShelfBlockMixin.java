package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.immersive.handler.ChiseledBookshelfHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ChiseledBookShelfBlock.class)
public abstract class ChiseledBookShelfBlockMixin {

    @Shadow
    private static int getHitSlot(Vec2 vec2) {
        return 0;
    }

    @Shadow
    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction) {
        return null;
    }

    @Shadow
    private static void addBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i) {
    }

    @Redirect(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
    at = @At(value = "INVOKE",
    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;getRelativeHitCoordinatesForBlockFace(Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/core/Direction;)Ljava/util/Optional;")
    )
    private Optional<Vec2> getRelativeHitCoordOverride(BlockHitResult blockHitResult, Direction direction) {
        if (ChiseledBookshelfHandler.bookshelfBlockSlotOverride != -1) {
            return Optional.of(new Vec2(1, 1)); // Return something to bypass Optional.isEmpty()
        } else {
            return getRelativeHitCoordinatesForBlockFace(blockHitResult, direction);
        }
    }

    @Redirect(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;getHitSlot(Lnet/minecraft/world/phys/Vec2;)I")
    )
    private int getHitSlotOverride(Vec2 vec2) {
        if (ChiseledBookshelfHandler.bookshelfBlockSlotOverride != -1) {
            return ChiseledBookshelfHandler.bookshelfBlockSlotOverride;
        } else {
            return getHitSlot(vec2);
        }
    }

    @Redirect(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;addBook(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;Lnet/minecraft/world/item/ItemStack;I)V")
    )
    private void addBookOverride(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i) {
        if (ChiseledBookshelfHandler.bookshelfBlockHandOverride != null) {
            addBook(level, blockPos, player, chiseledBookShelfBlockEntity, player.getItemInHand(ChiseledBookshelfHandler.bookshelfBlockHandOverride), i);
        } else {
            addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, i);
        }
    }

    @Redirect(method = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;removeBook(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private static boolean removeBlockInvAddOverride(Inventory inventory, ItemStack itemStack, Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, int i) {
        if (ChiseledBookshelfHandler.bookshelfBlockHandOverride != null && player.getItemInHand(ChiseledBookshelfHandler.bookshelfBlockHandOverride).isEmpty()) {
            player.setItemInHand(ChiseledBookshelfHandler.bookshelfBlockHandOverride, itemStack);
            return true;
        } else {
            return inventory.add(itemStack);
        }
    }
}
