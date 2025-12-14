package com.hammy275.immersivemc.server.swap;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.api.common.immersive.SwapResult;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.mixin.AnvilMenuMixin;
import com.hammy275.immersivemc.server.api_impl.SwapResultImpl;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Swap {

    /**
     * Swap items. This is the logic for
     * {@link ImmersiveLogicHelpers#swapItems(ItemStack, ItemStack, ItemSwapAmount, Player)},
     * but also has callbacks for incrementing or clearing the item counts for the purposes of {@link ItemStorage}. See
     * the above-mentioned method for details of this method.
     * @param itemCountIncrementer Callback to run when the amount of items in this Immersive is increased
     * @param itemCountClearer Callback to run when the amount of items in this Immersive is decreased
     */
    public static SwapResult swapItems(ItemStack handStack, ItemStack immersiveStack, ItemSwapAmount swapAmount, int forcedMaxImmersiveStackSize,
                                       Player player, @Nullable Consumer<Integer> itemCountIncrementer, @Nullable Consumer<Void> itemCountClearer) {
        ItemStack toHand;
        ItemStack toImmersive;
        ItemStack leftovers;
        int immersiveMaxStackSize = forcedMaxImmersiveStackSize == -1 ? handStack.getMaxStackSize() : forcedMaxImmersiveStackSize;
        int amountToPlace = Math.min(swapAmount.getNumItemsToSwap(), immersiveMaxStackSize);
        boolean handAndImmersiveStackMatch = Util.stacksEqualBesidesCount(handStack, immersiveStack);
        boolean immersiveStackAtMax = immersiveStack.getCount() == immersiveMaxStackSize;
        // Both stacks are the same item and the immersive stack can hold some more items
        if (handAndImmersiveStackMatch && !handStack.isEmpty() && !immersiveStackAtMax) {
            ItemStack handStackToPlace = handStack.copy();
            handStackToPlace.setCount(amountToPlace);
            int oldImmersiveCount = immersiveStack.getCount();
            Util.ItemStackMergeResult mergeResult = Util.mergeStacks(immersiveStack, handStackToPlace, false);
            toImmersive = immersiveStack;
            toHand = handStack.copy();
            toHand.shrink(amountToPlace);
            // Add anything that wasn't transferred due to stack size back
            toHand.grow(mergeResult.mergedFrom.getCount());
            leftovers = ItemStack.EMPTY;
            // Always place only in last slot. If Player A places, then Player B, then A places again, order is
            // A-B-A, rather than all of A then B.
            int itemsMoved = immersiveStack.getCount() - oldImmersiveCount;
            if (itemCountIncrementer != null) {
                itemCountIncrementer.accept(itemsMoved);
            }
        } else if (handStack.isEmpty()) { // Taking item from Immersive and hand is empty
            // Prioritize leftovers over toHand, since we likely don't care about the items anymore, and we don't
            // want to fill the hotbar with item grabs
            boolean stackToInventory = Util.hasItemInInventoryWithStackSpace(player, immersiveStack);
            toHand = stackToInventory ? ItemStack.EMPTY : immersiveStack.copy();
            toImmersive = ItemStack.EMPTY;
            leftovers = stackToInventory ? immersiveStack.copy() : ItemStack.EMPTY;
            if (itemCountClearer != null) {
                itemCountClearer.accept(null);
            }
        } else if (immersiveStackAtMax && handAndImmersiveStackMatch) { // Taking item from Immersive and hand contains same item as Immersive
            toHand = immersiveStack.copy();
            toImmersive = ItemStack.EMPTY;
            leftovers = handStack.copy();
            if (itemCountClearer != null) {
                itemCountClearer.accept(null);
            }
        } else { // Slots contain different item types and hand isn't air (place new stack in and old items go somewhere)
            toHand = handStack.copy();
            toHand.shrink(amountToPlace);
            toImmersive = handStack.copy();
            toImmersive.setCount(amountToPlace);
            leftovers = immersiveStack.copy();
            if (toHand.isEmpty() && !Util.hasItemInInventoryWithStackSpace(player, leftovers)) {
                toHand = leftovers;
                leftovers = ItemStack.EMPTY;
            }
            if (itemCountClearer != null) {
                itemCountClearer.accept(null);
            }
            if (itemCountIncrementer != null) {
                itemCountIncrementer.accept(amountToPlace);
            }
        }
        return new SwapResultImpl(toHand, toImmersive, leftovers);
    }

    public static boolean doEnchanting(int slot, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        // NOTE: slot is 1-3, depending on which enchantment the player is going for.
        if (slot < 1 || slot > 3) return false;
        ETableWorldStorage storage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        ItemStack toEnchantItem = storage.getItem(0).copy();
        if (toEnchantItem.isEmpty()) return false;
        int lapisInInventory = 0;
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
            if (player.getInventory().getItem(i).getItem() == Items.LAPIS_LAZULI) {
                lapisInInventory += player.getInventory().getItem(i).getCount();
            }
        }
        if (lapisInInventory < slot && !player.getAbilities().instabuild) return false;
        if ((player.experienceLevel < slot) && !player.getAbilities().instabuild) return false;
        boolean doApoth = Apoth.apothImpl.enchantModuleEnabled();

        boolean takeLapis = false;
        if (doApoth) {
            ETableStorage.SlotData[] stats = Apoth.apothImpl.getEnchData(player, pos, toEnchantItem);
            if (player.experienceLevel < stats[slot - 1].xpLevel() && !player.getAbilities().instabuild) return false;
            takeLapis = true;
            ItemStack out = Apoth.apothImpl.doEnchant(player, pos, slot - 1, toEnchantItem);
            if (out == null) out = toEnchantItem; // Happens on Apotheosis compat crash
            Util.giveStackHandFirst(player, hand, out);
            storage.setItem(0, ItemStack.EMPTY);
        } else {
            EnchantmentMenu container = new EnchantmentMenu(-1,
                    player.getInventory(), ContainerLevelAccess.create(player.level(), pos));
            container.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
            container.setItem(0, 0, toEnchantItem);
            if (container.clickMenuButton(player, slot - 1)) {
                takeLapis = true;
                Util.giveStackHandFirst(player, hand, container.getSlot(0).getItem());
                storage.setItem(0, ItemStack.EMPTY);
            }
        }

        if (takeLapis) {
            int lapisToTake = slot;
            for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
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
        }
        return takeLapis;
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
            SwapResult result = ImmersiveLogicHelpers.instance().swapItems(playerItem, tableItem, amount, player);
            itemArray[slot] = result.immersiveStack();
            result.giveToPlayer(player, hand);
            itemArray[4] = getRecipeOutput(player, itemArray);
        } else {
            itemArray = handleDoCraft(player, itemArray, null, amount);
            if (itemArray == null) return;
        }
        for (int i = 0; i <= 4; i++) {
            items.set(i, itemArray[i]);
        }
        ImmersiveMCPlayerStorages.getPlayerStorage(player).setDirty();
    }

    public static boolean handleAnvilCraft(AnvilStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
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
            Util.giveStackHandFirst(player, hand, resAndCost.getFirst());
            return true;
        }
        return false;
    }

    public static boolean handleSmithingTableCraft(SmithingTableStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
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
            Util.giveStackHandFirst(player, hand, output);
            return true;
        }
        return false;
    }

    public static Optional<RecipeHolder<CraftingRecipe>> getRecipe(ServerPlayer player, ItemStack[] stacksIn) {
        int invDim = stacksIn.length >= 9 ? 3 : 2;
        List<ItemStack> stacks = new ArrayList<>(Arrays.asList(stacksIn).subList(0, invDim * invDim));
        CraftingInput inv = CraftingInput.of(invDim, invDim, stacks);

        return player.level().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, player.level());
    }

    public static ItemStack getRecipeOutput(ServerPlayer player, ItemStack[] stacksIn) {
        int invDim = stacksIn.length >= 9 ? 3 : 2;
        List<ItemStack> stacks = new ArrayList<>(Arrays.asList(stacksIn).subList(0, invDim * invDim));
        CraftingInput inv = CraftingInput.of(invDim, invDim, stacks);

        Optional<RecipeHolder<CraftingRecipe>> res = player.level().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                inv, player.level());
        if (res.isPresent()) {
            return res.get().value().assemble(inv, player.level().registryAccess());
        }
        return ItemStack.EMPTY;
    }

    @Nullable // Returns null if no recipe
    public static ItemStack[] handleDoCraft(ServerPlayer player, ItemStack[] stacksIn,
                                     BlockPos tablePos, ItemSwapAmount amount) {
        boolean isBackpack = stacksIn.length == 5;
        AbstractCraftingMenu menu = isBackpack ? new InventoryMenu(player.getInventory(), false, player) : new CraftingMenu(-1, player.getInventory());
        ItemStack stackOut = getRecipeOutput(player, stacksIn);
        ItemStack firstOut = stackOut;
        ItemStack[] newSlotsState = Arrays.copyOf(stacksIn, stacksIn.length);
        List<ItemStack> stacksToGive = new ArrayList<>();
        int itersDone = 0;
        int iters = amount.getSwapMode() == SwapMode.ALL ? Integer.MAX_VALUE : amount.getNumItemsToSwap();
        while (itersDone++ < iters && ItemStack.matches(firstOut, stackOut) && !stackOut.isEmpty() &&
                itemStackArraysMatchBesidesCount(stacksIn, newSlotsState, isBackpack ? 4 : 9)) {
            // Perform the craft in an actual crafting menu
            for (int i = 0; i < newSlotsState.length - 1; i++) { // -1 from length since we skip the last index since it's the output
                // Slot 0 is the output
                menu.setItem(i + 1, 0, newSlotsState[i].copy());
            }
            // Need to use this resultContainer instead of the slots directly due to Fast Workbench changing things up
            ResultContainer resultContainer = (ResultContainer) menu.getSlot(0).container;
            resultContainer.setRecipeUsed(getRecipe(player, newSlotsState).get());
            resultContainer.setItem(0, stackOut);
            menu.getSlot(0).onTake(player, stackOut);
            // Give our item to us, remove items from crafting inventory, and show new recipe
            stackOut.onCraftedBy(player, stackOut.getCount());
            for (int i = 0; i < newSlotsState.length - 1; i++) {
                newSlotsState[i] = menu.getSlot(i + 1).getItem();
            }
            stacksToGive.add(stackOut);
            stackOut = getRecipeOutput(player, newSlotsState);
            newSlotsState[newSlotsState.length - 1] = stackOut;
        }

        // Clear crafting slots to prevent showing output in actual inventory
        if (isBackpack) {
            menu.setItem(InventoryMenu.RESULT_SLOT, 0, ItemStack.EMPTY);
            for (int i = InventoryMenu.CRAFT_SLOT_START; i < InventoryMenu.CRAFT_SLOT_END; i++) {
                menu.setItem(i, 0, ItemStack.EMPTY);
            }
        }

        if (stacksToGive.isEmpty()) {
            return null;
        }

        // Give items
        boolean playedSound = false;
        for (ItemStack toGive : stacksToGive) {
            ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!handStack.isEmpty() && Util.stacksEqualBesidesCount(toGive, handStack)) {
                Util.ItemStackMergeResult itemRes = Util.mergeStacks(handStack, toGive, true);
                player.setItemInHand(InteractionHand.MAIN_HAND, itemRes.mergedInto);
                toGive = itemRes.mergedFrom;
            } else if (handStack.isEmpty() && !Util.hasItemInInventoryWithStackSpace(player, toGive)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, toGive);
                toGive = ItemStack.EMPTY;
            }
            if (!toGive.isEmpty()) {
                BlockPos posBlock = tablePos != null ? tablePos.above() : player.blockPosition();
                Vec3 pos = Vec3.atCenterOf(posBlock);
                ItemEntity entOut = new ItemEntity(player.level(), pos.x, pos.y, pos.z, toGive);
                entOut.setDeltaMovement(0, 0, 0);
                player.level().addFreshEntity(entOut);
            } else if (!playedSound) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, isBackpack ? SoundSource.PLAYERS : SoundSource.BLOCKS,
                        0.2f,
                        ThreadLocalRandom.current().nextFloat() -
                                ThreadLocalRandom.current().nextFloat() * 1.4f + 2f);
                playedSound = true;
            }
        }
        return newSlotsState;
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
        int level = ((AnvilMenuMixin) container).immersiveMC$getCost().get();
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

    public static boolean itemStackArraysMatchBesidesCount(ItemStack[] a, ItemStack[] b, int numElements) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < numElements; i++) {
            ItemStack as = a[i];
            ItemStack bs = b[i];
            if (as != bs && (as == null || bs == null || !Util.stacksEqualBesidesCount(as, bs))) {
                return false;
            }
        }
        return true;
    }
}
