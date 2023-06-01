package com.hammy275.immersivemc.server.swap;

import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.storage.AnvilStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.storage.workarounds.NullContainer;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPluginProxy;
import com.hammy275.immersivemc.mixin.AnvilMenuMixin;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Swap {

    public static void beaconSwap(ServerPlayer player, InteractionHand hand, BlockPos pos) {
        if (!player.getItemInHand(hand).is(ItemTags.BEACON_PAYMENT_ITEMS) && !player.getItemInHand(hand).isEmpty()) return;
        ImmersiveStorage beaconStorage = GetStorage.getBeaconStorage(player, pos);
        ItemStack playerItem = player.getItemInHand(hand).copy();
        ItemStack beaconItem = beaconStorage.items[0].copy();
        if (!beaconItem.isEmpty()) {
            placeLeftovers(player, beaconItem);
            beaconStorage.items[0] = ItemStack.EMPTY;
        }
        if (!playerItem.isEmpty()) {
            beaconStorage.items[0] = playerItem.copy();
            beaconStorage.items[0].setCount(1);
            playerItem.shrink(1);
            if (playerItem.isEmpty()) {
                playerItem = ItemStack.EMPTY;
            }
            player.setItemInHand(hand, playerItem);
        }
        beaconStorage.wStorage.setDirty();
    }

    public static void shulkerBoxSwap(ServerPlayer player, int slot, InteractionHand hand, BlockPos pos) {
        if (player.level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity shulkerBox) {
            ItemStack shulkerItem = shulkerBox.getItem(slot).copy();
            ItemStack playerItem = player.getItemInHand(hand);
            if (playerItem.isEmpty() || shulkerItem.isEmpty() || !Util.stacksEqualBesidesCount(shulkerItem, playerItem)) {
                if (shulkerBox.canPlaceItemThroughFace(slot, playerItem, null)) {
                    player.setItemInHand(hand, shulkerItem);
                    shulkerBox.setItem(slot, playerItem);
                }

            } else {
                Util.ItemStackMergeResult result = Util.mergeStacks(shulkerItem, playerItem, false);
                player.setItemInHand(hand, result.mergedFrom);
                shulkerBox.setItem(slot, result.mergedInto);
            }
            shulkerBox.setChanged();
        }
    }

    public static void enchantingTableSwap(ServerPlayer player, int slot, InteractionHand hand, BlockPos pos) {
        if (player == null) return;
        ImmersiveStorage enchStorage = GetStorage.getEnchantingStorage(player, pos);
        if (slot == 0) {
            ItemStack toEnchant = player.getItemInHand(hand).copy();
            ItemStack toPlayer = enchStorage.items[0].copy();
            if (!toEnchant.isEmpty() && !toEnchant.isEnchantable()) return;
            player.setItemInHand(hand, toPlayer);
            enchStorage.items[0] = toEnchant;
        } else if (player.getItemInHand(hand).isEmpty()) {
            boolean res = doEnchanting(slot, pos, player, hand);
            if (res) {
                VRPluginProxy.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        enchStorage.wStorage.setDirty();
    }

    public static boolean doEnchanting(int slot, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        // NOTE: slot is 1-3, depending on which enchantment the player is going for.
        if (!player.getItemInHand(hand).isEmpty()) return false;
        if (slot < 1 || slot > 3) return false;
        ImmersiveStorage storage = GetStorage.getEnchantingStorage(player, pos);
        ItemStack toEnchantItem = storage.items[0].copy();
        if (toEnchantItem.isEmpty()) return false;
        int lapisInInventory = 0;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().getItem(i).getItem() == Items.LAPIS_LAZULI) {
                lapisInInventory += player.getInventory().getItem(i).getCount();
            }
        }
        if (lapisInInventory < slot && !player.getAbilities().instabuild) return false;

        EnchantmentMenu container = new EnchantmentMenu(-1,
                player.getInventory(), ContainerLevelAccess.create(player.level, pos));
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
            storage.items[0] = ItemStack.EMPTY;
            return true;
        }
        return false;
    }

    public static void handleBackpackCraftingSwap(int slot, InteractionHand hand, ImmersiveStorage storage,
                                                  ServerPlayer player, PlacementMode mode) {
        if (slot < 4) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack tableItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, tableItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            storage.items[4] = getRecipeOutput(player, storage.items);
        } else {
            handleDoCraft(player, storage.items, null);
        }
        storage.wStorage.setDirty();
    }

    public static void anvilSwap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player,
                                 PlacementMode mode) {
        AnvilStorage storage = GetStorage.getAnvilStorage(player, pos);
        if (slot != 2) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack anvilItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, anvilItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            storage.items[2] = ItemStack.EMPTY; // Clear output if we change something
            storage.xpLevels = 0;
            if (!storage.items[0].isEmpty() && !storage.items[1].isEmpty()) {
                Pair<ItemStack, Integer> output = Swap.getAnvilOutput(storage.items[0], storage.items[1], player);
                storage.items[2] = output.getFirst();
                storage.xpLevels = output.getSecond();
            }
        } else if (!storage.items[2].isEmpty()) { // Craft our result!
            if (!player.getItemInHand(hand).isEmpty()) return;
            boolean res = Swap.handleAnvilCraft(storage, pos, player, hand);
            if (res) {
                VRPluginProxy.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.wStorage.setDirty();
    }

    public static void smithingTableSwap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player,
                                 PlacementMode mode) {
        ImmersiveStorage storage = GetStorage.getSmithingTableStorage(player, pos);
        if (slot != 2) {
            ItemStack playerItem = player.getItemInHand(hand);
            ItemStack anvilItem = storage.items[slot];
            SwapResult result = getSwap(playerItem, anvilItem, mode);
            storage.items[slot] = result.toOther;
            givePlayerItemSwap(result.toHand, playerItem, player, hand);
            placeLeftovers(player, result.leftovers);
            storage.items[2] = ItemStack.EMPTY; // Clear output if we change something
            if (!storage.items[0].isEmpty() && !storage.items[1].isEmpty()) {
                ItemStack output = Swap.getSmithingTableOutput(storage.items[0], storage.items[1], player);
                storage.items[2] = output;
            }
        } else if (!storage.items[2].isEmpty()) { // Craft our result!
            if (!player.getItemInHand(hand).isEmpty()) return;
            boolean res = Swap.handleSmithingTableCraft(storage, pos, player, hand);
            if (res) {
                VRPluginProxy.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.wStorage.setDirty();
    }

    public static boolean handleAnvilCraft(AnvilStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack[] items = storage.items;
        ItemStack left = items[0];
        ItemStack mid = items[1];
        Pair<ItemStack, Integer> resAndCost = Swap.getAnvilOutput(left, mid, player);
        if ((player.experienceLevel >= resAndCost.getSecond() || player.getAbilities().instabuild)
                && !resAndCost.getFirst().isEmpty()) {
            ItemCombinerMenu container = new AnvilMenu(-1, player.getInventory(),
                    ContainerLevelAccess.create(player.level, pos));
                    /* Note: Since we create a fresh container here with only the output
                     (used mainly for causing the anvil to make sounds and possibly break),
                     we never subtract XP levels from it. Instead, we just subtract them
                     ourselves here. */
            container.getSlot(2).onTake(player, resAndCost.getFirst());
            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-resAndCost.getSecond());
            }
            left.shrink(1);
            mid.shrink(1);
            items[2] = ItemStack.EMPTY;
            storage.xpLevels = 0;
            player.setItemInHand(hand, resAndCost.getFirst());
            return true;
        }
        return false;
    }

    public static boolean handleSmithingTableCraft(ImmersiveStorage storage, BlockPos pos, ServerPlayer player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack[] items = storage.items;
        ItemStack left = items[0];
        ItemStack mid = items[1];
        ItemStack output = Swap.getSmithingTableOutput(left, mid, player);
        if (!output.isEmpty()) {
            ItemCombinerMenu container = new SmithingMenu(-1, player.getInventory(),
                    ContainerLevelAccess.create(player.level, pos));
            container.getSlot(2).onTake(player, output);
            left.shrink(1);
            mid.shrink(1);
            items[2] = ItemStack.EMPTY;
            player.setItemInHand(hand, output);
            return true;
        }
        return false;
    }

    public static void handleCraftingSwap(ServerPlayer player, int slot, InteractionHand hand, BlockPos tablePos,
                                          PlacementMode mode) {
        ImmersiveStorage storage = GetStorage.getCraftingStorage(player, tablePos);
        if (player.level.getBlockEntity(tablePos) instanceof Container table) { // Tinker's Construct Table
            ItemStack playerItem = player.getItemInHand(hand).copy();
            ItemStack craftingItem = table.getItem(slot).copy();
            if (slot < 9) {
                // Only set the output item into our storage since everything else is rendered by TC
                SwapResult result = getSwap(playerItem, craftingItem, mode);
                givePlayerItemSwap(result.toHand, playerItem, player, hand);
                table.setItem(slot, result.toOther);
                placeLeftovers(player, result.leftovers);
                ItemStack[] ins = new ItemStack[10];
                for (int i = 0; i <= 8; i++) {
                    ins[i] = table.getItem(i);
                    storage.items[i] = ItemStack.EMPTY;
                }
                ins[9] = ItemStack.EMPTY;
                ItemStack output = getRecipeOutput(player, ins);
                storage.items[9] = output;
            } else {
                // At crafting time, make our storage match the table contents, craft like a vanilla table,
                // then put our storage back to empty after cloning our crafting results back over
                for (int i = 0; i <= 8; i++) {
                    storage.items[i] = table.getItem(i).copy();
                }
                handleDoCraft(player, storage.items, tablePos);
                for (int i = 0; i <= 8; i++) {
                    // setItem here instead of using non-copies so setItem can sync stuff back
                    table.setItem(i, storage.items[i]);
                    storage.items[i] = ItemStack.EMPTY;
                }
            }
        } else {
            if (slot < 9) {
                ItemStack playerItem = player.getItemInHand(hand);
                ItemStack craftingItem = storage.items[slot];
                SwapResult result = getSwap(playerItem, craftingItem, mode);
                storage.items[slot] = result.toOther;
                givePlayerItemSwap(result.toHand, playerItem, player, hand);
                placeLeftovers(player, result.leftovers);
                storage.items[9] = getRecipeOutput(player, storage.items);
            } else {
                handleDoCraft(player, storage.items, tablePos);
            }
            storage.wStorage.setDirty();
        }
    }

    public static ItemStack getRecipeOutput(ServerPlayer player, ItemStack[] stacksIn) {
        int invDim = stacksIn.length == 10 ? 3 : 2; // 10 since stacksIn includes the output slot
        CraftingContainer inv = new CraftingContainer(new NullContainer(), invDim, invDim);
        for (int i = 0; i < stacksIn.length - 1; i++) {
            inv.setItem(i, stacksIn[i]);
        }
        Optional<CraftingRecipe> res = player.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                inv, player.level);
        if (res.isPresent()) {
            return res.get().assemble(inv);
        }
        return ItemStack.EMPTY;
    }

    public static void handleDoCraft(ServerPlayer player, ItemStack[] stacksIn,
                                     BlockPos tablePos) {
        boolean isBackpack = stacksIn.length == 5;
        int invDim = isBackpack ? 2 : 3;
        CraftingContainer inv = new CraftingContainer(new NullContainer(), invDim, invDim);
        for (int i = 0; i < stacksIn.length - 1; i++) { // -1 from length since we skip the last index since it's the output
            inv.setItem(i, stacksIn[i]);
        }
        ItemStack stackOut = getRecipeOutput(player, stacksIn);
        if (!stackOut.isEmpty()) {
            // Give our item to us, remove items from crafting inventory, and show new recipe
            for (int i = 0; i < stacksIn.length - 1; i++) {
                if (stacksIn[i].getItem().hasCraftingRemainingItem()) {
                    if (stacksIn[i].getCount() == 1) {
                        stacksIn[i] = new ItemStack(stacksIn[i].getItem().getCraftingRemainingItem());
                    } else {
                        placeLeftovers(player, new ItemStack(stacksIn[i].getItem().getCraftingRemainingItem()));
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
                ItemEntity entOut = new ItemEntity(player.level, pos.x, pos.y, pos.z, toGive);
                entOut.setDeltaMovement(0, 0, 0);
                player.level.addFreshEntity(entOut);
            } else {
                player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
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
    public static void handleFurnaceSwap(WorldlyContainer furnace, Player player,
                                         InteractionHand hand, int slot, PlacementMode mode) {
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            if (slot != 1 || furnace.canPlaceItem(1, playerItem) || playerItem.isEmpty()) {
                SwapResult result = getSwap(playerItem, furnaceItem, mode);
                givePlayerItemSwap(result.toHand, playerItem, player, hand);
                furnace.setItem(slot, result.toOther);
                placeLeftovers(player, result.leftovers);
            }
        } else {
            if (playerItem.isEmpty()) {
                player.setItemInHand(hand, furnaceItem);
                furnace.setItem(2, playerItem);
            } else if (Util.stacksEqualBesidesCount(furnaceItem, playerItem)) {
                Util.ItemStackMergeResult result = Util.mergeStacks(playerItem, furnaceItem, false);
                player.setItemInHand(hand, result.mergedInto);
                furnace.setItem(slot, result.mergedFrom);
            }

            // Experience and recipes reward
            if (player instanceof ServerPlayer sp && furnace instanceof AbstractFurnaceBlockEntity furnaceBE) {
                furnaceBE.awardUsedRecipesAndPopExperience(sp);
            }
        }
        furnace.setChanged();
    }

    public static void handleBrewingSwap(BrewingStandBlockEntity stand, Player player,
                                         InteractionHand hand, int slot, PlacementMode mode) {
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
        stand.setChanged();
    }

    public static void handleJukebox(JukeboxBlockEntity jukebox,
                                     ServerPlayer player, InteractionHand hand) {
        ItemStack playerItem = player.getItemInHand(hand);
        if (jukebox.getRecord() == ItemStack.EMPTY &&
                playerItem.getItem() instanceof RecordItem) {
            // Code from vanilla jukebox
            ((JukeboxBlock) Blocks.JUKEBOX).setRecord(player.level, jukebox.getBlockPos(), jukebox.getBlockState(),
                    playerItem);
            player.level.levelEvent((Player)null, 1010, jukebox.getBlockPos(), Item.getId(playerItem.getItem()));
            playerItem.shrink(1);
            player.awardStat(Stats.PLAY_RECORD);
            jukebox.setChanged();
            VRPluginProxy.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
        }
    }

    public static void handleChest(ChestBlockEntity chestIn,
                                   Player player, InteractionHand hand,
                                   int slot) {
        ChestBlockEntity chest = slot > 26 ? Util.getOtherChest(chestIn) : chestIn;
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

    public static void handleBarrel(BarrelBlockEntity barrel,
                                    Player player, InteractionHand hand,
                                    int slot) {
        ItemStack barrelItem = barrel.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand);
        if (playerItem.isEmpty() || barrelItem.isEmpty() || !Util.stacksEqualBesidesCount(barrelItem, playerItem)) {
            player.setItemInHand(hand, barrelItem);
            barrel.setItem(slot, playerItem);
        } else {
            Util.ItemStackMergeResult result = Util.mergeStacks(barrelItem, playerItem, false);
            player.setItemInHand(hand, result.mergedFrom);
            barrel.setItem(slot, result.mergedInto);
        }
        barrel.setChanged();
    }

    public static void handleHopper(HopperBlockEntity hopper,
                                    Player player, InteractionHand hand,
                                    int slot) {
        ItemStack hopperItem = hopper.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand);
        if (playerItem.isEmpty() || hopperItem.isEmpty() || !Util.stacksEqualBesidesCount(hopperItem, playerItem)) {
            player.setItemInHand(hand, hopperItem);
            hopper.setItem(slot, playerItem);
        } else {
            Util.ItemStackMergeResult result = Util.mergeStacks(hopperItem, playerItem, false);
            player.setItemInHand(hand, result.mergedFrom);
            hopper.setItem(slot, result.mergedInto);
        }
        hopper.setChanged();
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

    public static ItemStack getSmithingTableOutput(ItemStack left, ItemStack mid, ServerPlayer player) {
        ItemCombinerMenu container = new SmithingMenu(-1, player.getInventory());
        container.setItem(0, 0, left);
        container.setItem(1, 0, mid);
        container.createResult();
        ItemStack res = container.getSlot(2).getItem();
        return res;
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

    public static void placeLeftovers(Player player, ItemStack leftovers) {
        if (!leftovers.isEmpty()) {
            ItemEntity item = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), leftovers);
            player.level.addFreshEntity(item);
        }
    }

    public static void givePlayerItemSwap(ItemStack toPlayer, ItemStack fromPlayer, Player player, InteractionHand hand) {
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
