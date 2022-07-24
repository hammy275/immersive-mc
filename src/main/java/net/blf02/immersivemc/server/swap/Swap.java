package net.blf02.immersivemc.server.swap;

import com.mojang.datafixers.util.Pair;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.common.storage.AnvilStorage;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.common.storage.workarounds.NullContainer;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.AbstractRepairContainer;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.item.*;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Swap {

    public static void enchantingTableSwap(ServerPlayerEntity player, int slot, Hand hand, BlockPos pos) {
        if (player == null) return;
        ImmersiveStorage enchStorage = GetStorage.getEnchantingStorage(player, pos);
        if (slot == 0) {
            ItemStack toEnchant = player.getItemInHand(hand).copy();
            ItemStack toPlayer = enchStorage.items[0].copy();
            if (!toEnchant.isEmpty() && !toEnchant.isEnchantable()) return;
            player.setItemInHand(hand, toPlayer);
            enchStorage.items[0] = toEnchant;
        } else if (player.getItemInHand(hand).isEmpty()) {
            doEnchanting(slot, pos, player, hand);
        }
        enchStorage.wStorage.setDirty();
    }

    public static void doEnchanting(int slot, BlockPos pos, ServerPlayerEntity player, Hand hand) {
        // NOTE: slot is 1-3, depending on which enchantment the player is going for.
        if (!player.getItemInHand(hand).isEmpty()) return;
        if (slot < 1 || slot > 3) return;
        ImmersiveStorage storage = GetStorage.getEnchantingStorage(player, pos);
        ItemStack toEnchantItem = storage.items[0].copy();
        if (toEnchantItem.isEmpty()) return;
        int lapisInInventory = 0;
        for (int i = 0; i < player.inventory.items.size(); i++) {
            if (Tags.Items.GEMS_LAPIS.contains(player.inventory.getItem(i).getItem())) {
                lapisInInventory += player.inventory.getItem(i).getCount();
            }
        }
        if (lapisInInventory < slot && !player.abilities.instabuild) return;

        EnchantmentContainer container = new EnchantmentContainer(-1,
                player.inventory, IWorldPosCallable.create(player.level, pos));
        container.setItem(1, new ItemStack(Items.LAPIS_LAZULI, 64));
        container.setItem(0, toEnchantItem);
        if (container.clickMenuButton(player, slot - 1)) {
            int lapisToTake = slot;
            for (int i = 0; i < player.inventory.items.size(); i++) {
                if (Tags.Items.GEMS_LAPIS.contains(player.inventory.getItem(i).getItem())) {
                    ItemStack stack = player.inventory.getItem(i);
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
            storage.items[0] = ItemStack.EMPTY;
        }
    }

    public static void handleBackpackCraftingSwap(int slot, Hand hand, ImmersiveStorage storage,
                                                  ServerPlayerEntity player, PlacementMode mode) {
        if (slot < 4) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack tableItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, tableItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            ICraftingRecipe recipe = getRecipe(player, storage.items);
            if (recipe != null) {
                storage.items[4] = recipe.getResultItem();
            } else {
                storage.items[4] = ItemStack.EMPTY;
            }
        } else {
            handleDoCraft(player, storage.items, null);
        }
        storage.wStorage.setDirty();
    }

    public static void anvilSwap(int slot, Hand hand, BlockPos pos, ServerPlayerEntity player,
                                 PlacementMode mode) {
        World level = player.level;
        boolean isReallyAnvil = level.getBlockState(pos).getBlock() instanceof AnvilBlock;
        AnvilStorage storage = GetStorage.getAnvilStorage(player, pos);
        if (slot != 2) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack anvilItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, anvilItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            storage.items[2] = ItemStack.EMPTY; // Clear output if we change something
            if (isReallyAnvil) storage.xpLevels = 0;
            if (!storage.items[0].isEmpty() && !storage.items[1].isEmpty()) {
                Pair<ItemStack, Integer> output = Swap.getAnvilOutput(storage.items[0], storage.items[1], isReallyAnvil, player);
                storage.items[2] = output.getFirst();
                storage.xpLevels = output.getSecond();
            }
        } else if (!storage.items[2].isEmpty()) { // Craft our result!
            if (!player.getItemInHand(hand).isEmpty()) return;
            Swap.handleAnvilCraft(storage, pos, player, hand);
        }
        storage.wStorage.setDirty();
    }

    public static void handleAnvilCraft(AnvilStorage storage, BlockPos pos, ServerPlayerEntity player, Hand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return;
        ItemStack[] items = storage.items;
        ItemStack left = items[0];
        ItemStack mid = items[1];
        boolean isReallyAnvil = player.level.getBlockState(pos).getBlock() instanceof AnvilBlock;
        boolean isSmithingTable = player.level.getBlockState(pos).getBlock() instanceof SmithingTableBlock;
        if (!isReallyAnvil && !isSmithingTable) return; // Bail if we aren't an anvil or a smithing table!
        Pair<ItemStack, Integer> resAndCost = Swap.getAnvilOutput(left, mid, isReallyAnvil, player);
        if ((player.experienceLevel >= resAndCost.getSecond() || player.abilities.instabuild)
                && !resAndCost.getFirst().isEmpty()) {
            AbstractRepairContainer container;
            if (isReallyAnvil) {
                container = new RepairContainer(-1, player.inventory,
                        IWorldPosCallable.create(player.level, pos));

            } else {
                container = new SmithingTableContainer(-1, player.inventory,
                        IWorldPosCallable.create(player.level, pos));
            }
                    /* Note: Since we create a fresh container here with only the output
                     (used mainly for causing the anvil to make sounds and possibly break),
                     we never subtract XP levels from it. Instead, we just subtract them
                     ourselves here. */
            container.getSlot(2).onTake(player, resAndCost.getFirst());
            if (!player.abilities.instabuild) {
                player.giveExperienceLevels(-resAndCost.getSecond());
            }
            left.shrink(1);
            mid.shrink(1);
            items[2] = ItemStack.EMPTY;
            storage.xpLevels = 0;
            player.setItemInHand(hand, resAndCost.getFirst());
        }
    }

    public static void handleCraftingSwap(ServerPlayerEntity player, int slot, Hand hand, BlockPos tablePos,
                                          PlacementMode mode) {
        ImmersiveStorage storage = GetStorage.getCraftingStorage(player, tablePos);
        if (slot < 9) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack anvilItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, anvilItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            ICraftingRecipe recipe = getRecipe(player, storage.items);
            storage.items[9] = recipe != null ? recipe.getResultItem() : ItemStack.EMPTY;
        } else {
            handleDoCraft(player, storage.items, tablePos);
        }
        storage.wStorage.setDirty();
    }

    public static ICraftingRecipe getRecipe(ServerPlayerEntity player, ItemStack[] stacksIn) {
        int invDim = stacksIn.length == 10 ? 3 : 2; // 10 since stacksIn includes the output slot
        CraftingInventory inv = new CraftingInventory(new NullContainer(), invDim, invDim);
        for (int i = 0; i < stacksIn.length - 1; i++) {
            inv.setItem(i, stacksIn[i]);
        }
        Optional<ICraftingRecipe> res = player.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING,
                inv, player.level);
        return res.orElse(null);
    }

    public static void handleDoCraft(ServerPlayerEntity player, ItemStack[] stacksIn,
                                     BlockPos tablePos) {
        boolean isBackpack = stacksIn.length == 5;
        int invDim = isBackpack ? 2 : 3;
        CraftingInventory inv = new CraftingInventory(new NullContainer(), invDim, invDim);
        for (int i = 0; i < stacksIn.length - 1; i++) { // -1 from length since we skip the last index since it's the output
            inv.setItem(i, stacksIn[i]);
        }
        ICraftingRecipe res = getRecipe(player, stacksIn);
        if (res != null) {
            // Give our item to us, remove items from crafting inventory, and show new recipe
            for (int i = 0; i < stacksIn.length - 1; i++) {
                stacksIn[i].shrink(1);
            }
            ICraftingRecipe newRecipe = getRecipe(player, stacksIn);
            stacksIn[stacksIn.length - 1] = newRecipe != null ? newRecipe.getResultItem() : ItemStack.EMPTY;
            ItemStack stackOut = res.assemble(inv);
            ItemStack handStack = player.getItemInHand(Hand.MAIN_HAND);
            ItemStack toGive = ItemStack.EMPTY;
            if (!handStack.isEmpty() && Util.stacksEqualBesidesCount(stackOut, handStack)) {
                Util.ItemStackMergeResult itemRes = Util.mergeStacks(handStack, stackOut, true);
                player.setItemInHand(Hand.MAIN_HAND, itemRes.mergedInto);
                toGive = itemRes.mergedFrom;
            } else if (handStack.isEmpty()) {
                player.setItemInHand(Hand.MAIN_HAND, stackOut);
            } else {
                toGive = stackOut;
            }
            if (!toGive.isEmpty()) {
                BlockPos posBlock = tablePos != null ? tablePos.above() : player.blockPosition();
                Vector3d pos = Vector3d.atCenterOf(posBlock);
                ItemEntity entOut = new ItemEntity(player.level, pos.x, pos.y, pos.z);
                entOut.setItem(toGive);
                entOut.setDeltaMovement(0, 0, 0);
                player.level.addFreshEntity(entOut);
            } else {
                player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, isBackpack ? SoundCategory.PLAYERS : SoundCategory.BLOCKS,
                        0.2f,
                        ThreadLocalRandom.current().nextFloat() -
                                ThreadLocalRandom.current().nextFloat() * 1.4f + 2f);
            }
        }
    }

    public static void handleInventorySwap(PlayerEntity player, int slot, Hand hand) {
        // Always do full swap since splitting stacks is done when interacting with immersives instead
        ItemStack handStack = player.getItemInHand(hand).copy();
        ItemStack invStack = player.inventory.getItem(slot).copy();
        if (handStack.isEmpty() || invStack.isEmpty() || !Util.stacksEqualBesidesCount(handStack, invStack)) {
            player.setItemInHand(hand, invStack);
            player.inventory.setItem(slot, handStack);
        } else {
            Util.ItemStackMergeResult res = Util.mergeStacks(invStack, handStack, false);
            player.setItemInHand(hand, res.mergedFrom);
            player.inventory.setItem(slot, res.mergedInto);
        }

    }
    public static void handleFurnaceSwap(AbstractFurnaceTileEntity furnace, PlayerEntity player,
                                         Hand hand, int slot, PlacementMode mode) {
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            SwapResult result = getSwap(playerItem, furnaceItem, mode);
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            furnace.setItem(slot, result.toOther);
            placeLeftovers(player, result.leftovers);
        } else {
            if (playerItem.isEmpty()) {
                player.setItemInHand(hand, furnaceItem);
                furnace.setItem(2, playerItem);
            } else if (Util.stacksEqualBesidesCount(furnaceItem, playerItem)) {
                Util.ItemStackMergeResult result = Util.mergeStacks(playerItem, furnaceItem, false);
                player.setItemInHand(hand, result.mergedInto);
                furnace.setItem(slot, result.mergedFrom);
            }
        }
    }

    public static void handleBrewingSwap(BrewingStandTileEntity stand, PlayerEntity player,
                                         Hand hand, int slot, PlacementMode mode) {
        ItemStack standItem = stand.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot < 3) { // Potions
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY
            && !(standItem.getItem() instanceof PotionItem)) return;
            player.setItemInHand(hand, standItem);
            stand.setItem(slot, playerItem);
        } else { // Ingredient and Fuel
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY) return;
            SwapResult result = getSwap(playerItem, standItem, mode);
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            stand.setItem(slot, result.toOther);
            placeLeftovers(player, result.leftovers);
        }
    }

    public static void handleJukebox(JukeboxTileEntity jukebox,
                                     ServerPlayerEntity player, Hand hand) {
        ItemStack playerItem = player.getItemInHand(hand);
        if (jukebox.getRecord() == ItemStack.EMPTY &&
                playerItem.getItem() instanceof MusicDiscItem) {
            // Code from vanilla jukebox
            ((JukeboxBlock) Blocks.JUKEBOX).setRecord(player.level, jukebox.getBlockPos(), jukebox.getBlockState(),
                    playerItem);
            player.level.levelEvent((PlayerEntity)null, 1010, jukebox.getBlockPos(), Item.getId(playerItem.getItem()));
            playerItem.shrink(1);
            player.awardStat(Stats.PLAY_RECORD);
        }
    }

    public static void handleChest(ChestTileEntity chestIn,
                                   PlayerEntity player, Hand hand,
                                   int slot) {
        ChestTileEntity chest = slot > 26 ? Util.getOtherChest(chestIn) : chestIn;
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
        }
    }

    public static void handleEnderChest(PlayerEntity player, Hand hand, int slot) {
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

    public static Pair<ItemStack, Integer> getAnvilOutput(ItemStack left, ItemStack mid, boolean isReallyAnvil, ServerPlayerEntity player) {
        AbstractRepairContainer container;
        if (isReallyAnvil) {
            container = new RepairContainer(-1, player.inventory);
        } else {
            container = new SmithingTableContainer(-1, player.inventory);
        }
        container.setItem(0, left);
        container.setItem(1, mid);
        container.createResult();
        ItemStack res = container.getSlot(2).getItem();
        int level = 0;
        if (isReallyAnvil) {
            level = ((RepairContainer) container).cost.get();
        }
        return new Pair<>(res, level);
    }

    /**
     * Get Swap Information.
     *
     * Will handle swapping the handIn stack to the otherIn stack using PlacementMode mode.
     *
     * This function DOES NOT modify the stacks coming in!
     *
     * @param handIn The stack in the player's hand. This is what's subtracted from.
     * @param otherIn The other stack. This is what's being placed into.
     * @param mode The placement mode as configured by the player.
     * @return A SwapResult representing the new items to give to the player, the object, and any leftovers to
     * give to the player some other way (or to alert to failing the swap entirely).
     */
    public static SwapResult getSwap(ItemStack handIn, ItemStack otherIn, PlacementMode mode) {
        int toPlace;
        switch (mode) {
            case PLACE_ONE:
                toPlace = 1;
                break;
            case PLACE_QUARTER:
                toPlace = (int) Math.max(handIn.getCount() / 4d, 1);
                break;
            case PLACE_HALF:
                toPlace = (int) Math.max(handIn.getCount() / 2d, 1);
                break;
            case PLACE_ALL:
                toPlace = handIn.getCount();
                break;
            default:
                throw new IllegalArgumentException("Unhandled placement mode " + mode);
        }

        // Swap toPlace from handIn to otherIn
        ItemStack toHand;
        ItemStack toOther;
        ItemStack leftovers;
        if (Util.stacksEqualBesidesCount(handIn, otherIn) && !handIn.isEmpty() && !otherIn.isEmpty()) {
            ItemStack handInCountAdjusted = handIn.copy();
            handInCountAdjusted.setCount(toPlace);
            Util.ItemStackMergeResult mergeResult = Util.mergeStacks(otherIn.copy(), handInCountAdjusted, false);
            toOther = mergeResult.mergedInto;
            // Take our original hand, shrink by all of the amount to be moved, then grow by the amount
            // that didn't get moved
            toHand = handIn.copy();
            toHand.shrink(toPlace);
            toHand.grow(mergeResult.mergedFrom.getCount());
            leftovers = ItemStack.EMPTY;
        } else if (handIn.isEmpty()) { // We grab the items from the immersive into our hand
            return new SwapResult(otherIn.copy(), ItemStack.EMPTY, ItemStack.EMPTY);
        } else { // We're placing into a slot of air OR the other slot contains something that isn't what we have
            toOther = handIn.copy();
            toOther.setCount(toPlace);
            toHand = handIn.copy();
            toHand.shrink(toPlace);
            leftovers = otherIn.copy();
        }
        return new SwapResult(toHand, toOther, leftovers);
    }

    public static void placeLeftovers(PlayerEntity player, ItemStack leftovers) {
        if (!leftovers.isEmpty()) {
            ItemEntity item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), leftovers);
            player.level.addFreshEntity(item);
        }
    }

    public static void givePlayerItemSwap(ItemStack toPlayer, ItemStack fromPlayer, PlayerEntity player, Hand hand) {
        if (fromPlayer.isEmpty() && toPlayer.getMaxStackSize() > 1) {
            Util.addStackToInventory(player, toPlayer);
        } else {
            player.setItemInHand(hand, toPlayer);
        }
    }


    public static class SwapResult {
        public final ItemStack toHand;
        public final ItemStack toOther;
        public final ItemStack leftovers;
        public SwapResult(ItemStack toHand, ItemStack toOther, ItemStack leftovers) {
            this.toHand = toHand;
            this.toOther = toOther;
            this.leftovers = leftovers;
        }
    }

}
