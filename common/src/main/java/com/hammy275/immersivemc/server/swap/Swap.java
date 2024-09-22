package com.hammy275.immersivemc.server.swap;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.SwapResult;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.util.NullContainer;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.mixin.AnvilMenuMixin;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.storage.world.impl.ETableWorldStorage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Swap {

    public static boolean doEnchanting(int slot, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        // NOTE: slot is 1-3, depending on which enchantment the player is going for.
        if (!player.getItemInHand(hand).isEmpty()) return false;
        if (slot < 1 || slot > 3) return false;
        ETableWorldStorage storage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        ItemStack toEnchantItem = storage.getItem(0).copy();
        if (toEnchantItem.isEmpty()) return false;
        int lapisInInventory = 0;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().getItem(i).getItem() == Items.LAPIS_LAZULI) {
                lapisInInventory += player.getInventory().getItem(i).getCount();
            }
        }
        if (lapisInInventory < slot && !player.getAbilities().instabuild) return false;

        EnchantmentMenu container = new EnchantmentMenu(-1,
                player.getInventory(), ContainerLevelAccess.create(player.level(), pos));
        container.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
        container.setItem(0, 0, toEnchantItem);
        if (container.clickMenuButton(player, slot - 1)) {
            int lapisToTake = slot;
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                if (player.getInventory().getItem(i).getItem() == Items.LAPIS_LAZULI) {
                    ItemStack stack = player.getInventory().getItem(i);
                    while (!stack.isEmpty() && lapisToTake > 0) {
                        stack.shrink(1);
                        lapisToTake--;
                    }
                }
                if (lapisToTake == 0) {
                    break;
                }
            }
            player.setItemInHand(hand, container.getSlot(0).getItem());
            storage.setItem(0, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static void handleBackpackCraftingSwap(int slot, InteractionHand hand, List<ItemStack> items,
                                                  ServerPlayer player, ItemSwapAmount amount) {
        ItemStack[] itemArray = new ItemStack[5];
        for (int i = 0; i <= 4; i++) {
            itemArray[i] = items.get(i);
        }
        if (slot < 4) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack tableItem = itemArray[slot];
            SwapResult result = ImmersiveLogicHelpers.instance().swapItems(playerItem, tableItem, amount);
            itemArray[slot] = result.immersiveStack();
            givePlayerItemSwap(result.playerHandStack(), playerItem, player, hand);
            Util.placeLeftovers(player, result.leftoverStack());
            itemArray[4] = getRecipeOutput(player, itemArray);
        } else {
            handleDoCraft(player, itemArray, null);
        }
        for (int i = 0; i <= 4; i++) {
            items.set(i, itemArray[i]);
        }
        ImmersiveMCPlayerStorages.getPlayerStorage(player).setDirty();
    }

    public static boolean handleAnvilCraft(AnvilStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack left = storage.getItem(0);
        ItemStack mid = storage.getItem(1);
        Pair<ItemStack, Integer> resAndCost = Swap.getAnvilOutput(left, mid, player);
        if ((player.experienceLevel >= resAndCost.getSecond() || player.getAbilities().instabuild)
                && !resAndCost.getFirst().isEmpty()) {
            ItemCombinerMenu container = new AnvilMenu(-1, player.getInventory(),
                    ContainerLevelAccess.create(player.level(), pos));
                    /* Note: Since we create a fresh container here with only the output
                     (used mainly for causing the anvil to make sounds and possibly break),
                     we never subtract XP levels from it. Instead, we just subtract them
                     ourselves here. */
            container.getSlot(2).onTake(player, resAndCost.getFirst());
            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-resAndCost.getSecond());
            }
            storage.setItem(0, container.getSlot(0).getItem().copy());
            storage.setItem(1, container.getSlot(1).getItem().copy());
            Pair<ItemStack, Integer> output = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
            storage.setItem(2, output.getFirst());
            storage.xpLevels = output.getSecond();
            player.setItemInHand(hand, resAndCost.getFirst());
            return true;
        }
        return false;
    }

    public static boolean handleSmithingTableCraft(SmithingTableStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack left = storage.getItem(0);
        ItemStack mid = storage.getItem(1);
        ItemStack right = storage.getItem(2);
        ItemStack output = Swap.getSmithingTableOutput(left, mid, right, player);
        if (!output.isEmpty()) {
            ItemCombinerMenu container = new SmithingMenu(-1, player.getInventory(),
                    ContainerLevelAccess.create(player.level(), pos));
            container.getSlot(3).onTake(player, output);
            storage.shrinkSlot(0, 1);
            storage.shrinkSlot(1, 1);
            storage.shrinkSlot(2, 1);
            storage.setItem(3, ItemStack.EMPTY);
            player.setItemInHand(hand, output);
            return true;
        }
        return false;
    }

    public static ItemStack getRecipeOutput(ServerPlayer player, ItemStack[] stacksIn) {
        int invDim = stacksIn.length >= 9 ? 3 : 2;
        List<ItemStack> stacks = new ArrayList<>(Arrays.asList(stacksIn).subList(0, invDim * invDim));
        CraftingInput inv = CraftingInput.of(invDim, invDim, stacks);

        Optional<RecipeHolder<CraftingRecipe>> res = player.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                inv, player.level());
        if (res.isPresent()) {
            return res.get().value().assemble(inv, player.level().registryAccess());
        }
        return ItemStack.EMPTY;
    }

    public static void handleDoCraft(ServerPlayer player, ItemStack[] stacksIn,
                                     BlockPos tablePos) {
        boolean isBackpack = stacksIn.length == 5;
        int invDim = isBackpack ? 2 : 3;
        CraftingContainer inv = new TransientCraftingContainer(new NullContainer(), invDim, invDim);
        for (int i = 0; i < stacksIn.length - 1; i++) { // -1 from length since we skip the last index since it's the output
            inv.setItem(i, stacksIn[i]);
        }
        ItemStack stackOut = getRecipeOutput(player, stacksIn);
        if (!stackOut.isEmpty()) {
            // Give our item to us, remove items from crafting inventory, and show new recipe
            stackOut.onCraftedBy(player.level(), player, stackOut.getCount());
            for (int i = 0; i < stacksIn.length - 1; i++) {
                if (stacksIn[i].getItem().hasCraftingRemainingItem()) {
                    if (stacksIn[i].getCount() == 1) {
                        stacksIn[i] = new ItemStack(stacksIn[i].getItem().getCraftingRemainingItem());
                    } else {
                        Util.placeLeftovers(player, new ItemStack(stacksIn[i].getItem().getCraftingRemainingItem()));
                    }

                } else {
                    stacksIn[i].shrink(1);
                }
            }
            ItemStack newOutput = getRecipeOutput(player, stacksIn);
            stacksIn[stacksIn.length - 1] = newOutput;
            ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack toGive = ItemStack.EMPTY;
            if (!handStack.isEmpty() && Util.stacksEqualBesidesCount(stackOut, handStack)) {
                Util.ItemStackMergeResult itemRes = Util.mergeStacks(handStack, stackOut, true);
                player.setItemInHand(InteractionHand.MAIN_HAND, itemRes.mergedInto);
                toGive = itemRes.mergedFrom;
            } else if (handStack.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stackOut);
            } else {
                toGive = stackOut;
            }
            if (!toGive.isEmpty()) {
                BlockPos posBlock = tablePos != null ? tablePos.above() : player.blockPosition();
                Vec3 pos = Vec3.atCenterOf(posBlock);
                ItemEntity entOut = new ItemEntity(player.level(), pos.x, pos.y, pos.z, toGive);
                entOut.setDeltaMovement(0, 0, 0);
                player.level().addFreshEntity(entOut);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, isBackpack ? SoundSource.PLAYERS : SoundSource.BLOCKS,
                        0.2f,
                        ThreadLocalRandom.current().nextFloat() -
                                ThreadLocalRandom.current().nextFloat() * 1.4f + 2f);
            }
        }
    }

    public static void handleInventorySwap(Player player, int slot, InteractionHand hand) {
        // Always do full swap since splitting stacks is done when interacting with immersives instead
        ItemStack handStack = player.getItemInHand(hand).copy();
        ItemStack invStack = player.getInventory().getItem(slot).copy();
        if (handStack.isEmpty() || invStack.isEmpty() || !Util.stacksEqualBesidesCount(handStack, invStack)) {
            player.setItemInHand(hand, invStack);
            player.getInventory().setItem(slot, handStack);
        } else {
            Util.ItemStackMergeResult res = Util.mergeStacks(invStack, handStack, false);
            player.setItemInHand(hand, res.mergedFrom);
            player.getInventory().setItem(slot, res.mergedInto);
        }

    }

    public static void handleChest(ChestBlockEntity chestIn,
                                   ServerPlayer player, InteractionHand hand,
                                   int slot) {
        chestIn = slot > 26 ? Util.getOtherChest(chestIn) : chestIn;
        if (chestIn == null) return;
        Container lootrChest = Lootr.lootrImpl.getContainer(player, chestIn.getBlockPos());
        Container chest = lootrChest != null ? lootrChest : chestIn;
        if (chest != null) {
            slot = slot % 27;
            ItemStack chestItem = chest.getItem(slot).copy();
            ItemStack playerItem = player.getItemInHand(hand);
            if (playerItem.isEmpty() || chestItem.isEmpty() || !Util.stacksEqualBesidesCount(chestItem, playerItem)) {
                player.setItemInHand(hand, chestItem);
                chest.setItem(slot, playerItem);
            } else {
                Util.ItemStackMergeResult result = Util.mergeStacks(chestItem, playerItem, false);
                player.setItemInHand(hand, result.mergedFrom);
                chest.setItem(slot, result.mergedInto);
            }
            chest.setChanged();
        }
    }

    public static void handleEnderChest(Player player, InteractionHand hand, int slot) {
        ItemStack chestItem = player.getEnderChestInventory().getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand);
        if (playerItem.isEmpty() || chestItem.isEmpty() || !Util.stacksEqualBesidesCount(chestItem, playerItem)) {
            player.setItemInHand(hand, chestItem);
            player.getEnderChestInventory().setItem(slot, playerItem);
        } else {
            Util.ItemStackMergeResult result = Util.mergeStacks(chestItem, playerItem, false);
            player.setItemInHand(hand, result.mergedFrom);
            player.getEnderChestInventory().setItem(slot, result.mergedInto);
        }
    }

    public static Pair<ItemStack, Integer> getAnvilOutput(ItemStack left, ItemStack mid, ServerPlayer player) {
        ItemCombinerMenu container = new AnvilMenu(-1, player.getInventory());
        container.setItem(0, 0, left);
        container.setItem(1, 0, mid);
        container.createResult();
        ItemStack res = container.getSlot(2).getItem();
        int level = ((AnvilMenuMixin) container).getCost().get();
        return new Pair<>(res, level);
    }

    public static ItemStack getSmithingTableOutput(ItemStack left, ItemStack mid, ItemStack right, ServerPlayer player) {
        ItemCombinerMenu container = new SmithingMenu(-1, player.getInventory());
        container.setItem(0, 0, left);
        container.setItem(1, 0, mid);
        container.setItem(2, 0, right);
        container.createResult();
        ItemStack res = container.getSlot(3).getItem();
        return res;
    }

    public static int getPlaceAmount(ItemStack handIn, PlacementMode mode) {
        return getPlaceAmount(handIn.getCount(), mode);
    }

    public static int getPlaceAmount(int handInSize, PlacementMode mode) {
        switch (mode) {
            case PLACE_ONE:
                return 1;
            case PLACE_QUARTER:
                return (int) Math.max(handInSize / 4d, 1);
            case PLACE_HALF:
                return (int) Math.max(handInSize / 2d, 1);
            case PLACE_ALL:
                return handInSize;
            default:
                throw new IllegalArgumentException("Unhandled placement mode " + mode);
        }
    }

    public static void givePlayerItemSwap(ItemStack toPlayer, ItemStack fromPlayer, Player player, InteractionHand hand) {
        if (fromPlayer.isEmpty() && toPlayer.getMaxStackSize() > 1) {
            Util.addStackToInventory(player, toPlayer);
        } else {
            player.setItemInHand(hand, toPlayer);
        }
    }


    public static class SwapResultOld {
        public final ItemStack toHand;
        public final ItemStack toOther;
        public final ItemStack leftovers;
        public SwapResultOld(ItemStack toHand, ItemStack toOther, ItemStack leftovers) {
            this.toHand = toHand;
            this.toOther = toOther;
            this.leftovers = leftovers;
        }
    }

}
