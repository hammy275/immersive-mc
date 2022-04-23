package net.blf02.immersivemc.server.swap;

import com.mojang.datafixers.util.Pair;
import net.blf02.immersivemc.common.storage.NullContainer;
import net.blf02.immersivemc.common.util.Util;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.Tags;

import java.util.AbstractList;
import java.util.Optional;

public class Swap {

    public static void handleFurnaceSwap(AbstractFurnaceTileEntity furnace, PlayerEntity player,
                                         Hand hand, int slot) {
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            if (!furnace.canPlaceItem(slot, playerItem) && !playerItem.isEmpty()) return;
            if (playerItem.isEmpty() || furnaceItem.isEmpty() || !Util.stacksEqualBesidesCount(furnaceItem, playerItem)) {
                player.setItemInHand(hand, furnaceItem);
                furnace.setItem(slot, playerItem);
            } else {
                Util.ItemStackMergeResult result = Util.mergeStacks(furnaceItem, playerItem, false);
                player.setItemInHand(hand, result.mergedFrom);
                furnace.setItem(slot, result.mergedInto);
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
        }
    }

    public static void handleBrewingSwap(BrewingStandTileEntity stand, PlayerEntity player,
                                         Hand hand, int slot) {
        ItemStack standItem = stand.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot < 3) { // Potions
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY
            && !(standItem.getItem() instanceof PotionItem)) return;
            player.setItemInHand(hand, standItem);
            stand.setItem(slot, playerItem);
        } else { // Ingredient and Fuel
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY) return;
            if (playerItem.isEmpty() || standItem.isEmpty() || !Util.stacksEqualBesidesCount(standItem, playerItem)) {
                player.setItemInHand(hand, standItem);
                stand.setItem(slot, playerItem);
            } else {
                Util.ItemStackMergeResult result = Util.mergeStacks(standItem, playerItem, false);
                player.setItemInHand(hand, result.mergedFrom);
                stand.setItem(slot, result.mergedInto);
            }
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

    public static void handleCrafting(ServerPlayerEntity player, ItemStack[] stacksIn,
                                      BlockPos tablePos) {
        CraftingInventory inv = new CraftingInventory(new NullContainer(), 3, 3);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, stacksIn[i]);
        }
        Optional<ICraftingRecipe> res = player.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING,
                inv, player.level);
        if (res.isPresent()) {
            if (removeNeededIngredients(player, inv)) {
                // Give our item to us
                ItemStack stackOut = res.get().assemble(inv);
                BlockPos posBlock = tablePos.above();
                Vector3d pos = Vector3d.atCenterOf(posBlock);
                ItemEntity entOut = new ItemEntity(player.level, pos.x, pos.y, pos.z);
                entOut.setItem(stackOut);
                entOut.setDeltaMovement(0, 0, 0);
                player.level.addFreshEntity(entOut);
            }
        }
    }

    public static void handleETable(int slot, BlockPos pos, ServerPlayerEntity player, Hand hand, int power) {
        if (!player.getItemInHand(hand).isEmpty()) return;
        if (power < 1 || power > 3) return;
        int lapisInInventory = 0;
        for (int i = 0; i < player.inventory.items.size(); i++) {
            if (Tags.Items.GEMS_LAPIS.contains(player.inventory.getItem(i).getItem())) {
                lapisInInventory += player.inventory.getItem(i).getCount();
            }
        }
        if (lapisInInventory < power && !player.abilities.instabuild) return;
        ItemStack enchantedItem = player.inventory.getItem(slot).copy();

        EnchantmentContainer container = new EnchantmentContainer(-1,
                player.inventory, IWorldPosCallable.create(player.level, pos));
        container.setItem(1, new ItemStack(Items.LAPIS_LAZULI, 64));
        container.setItem(0, enchantedItem);
        if (container.clickMenuButton(player, power - 1)) {
            player.inventory.getItem(slot).shrink(1);
            int lapisToTake = power;
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
            player.setItemInHand(hand, enchantedItem);
        }
    }
    
    public static void handleAnvil(int leftSlot, int midSlot, BlockPos pos, ServerPlayerEntity sender, Hand hand) {
        if (!sender.getItemInHand(hand).isEmpty()) return;
        ItemStack left = sender.inventory.getItem(leftSlot);
        ItemStack mid = sender.inventory.getItem(midSlot);
        boolean isReallyAnvil = sender.level.getBlockState(pos).getBlock() instanceof AnvilBlock;
        boolean isSmithingTable = sender.level.getBlockState(pos).getBlock() instanceof SmithingTableBlock;
        if (!isReallyAnvil && !isSmithingTable) return; // Bail if we aren't an anvil or a smithing table!
        Pair<ItemStack, Integer> resAndCost = Swap.getAnvilOutput(left, mid, isReallyAnvil, sender);
        if ((sender.experienceLevel >= resAndCost.getSecond() || sender.abilities.instabuild)
                && !resAndCost.getFirst().isEmpty()) {
            AbstractRepairContainer container;
            if (isReallyAnvil) {
                container = new RepairContainer(-1, sender.inventory,
                        IWorldPosCallable.create(sender.level, pos));

            } else {
                container = new SmithingTableContainer(-1, sender.inventory,
                        IWorldPosCallable.create(sender.level, pos));
            }
                    /* Note: Since we create a fresh container here with only the output
                     (used mainly for causing the anvil to make sounds and possibly break),
                     we never subtract XP levels from it. Instead, we just subtract them
                     ourselves here. */
            container.getSlot(2).onTake(sender, resAndCost.getFirst());
            if (!sender.abilities.instabuild) {
                sender.giveExperienceLevels(-resAndCost.getSecond());
            }
            left.shrink(1);
            mid.shrink(1);
            sender.setItemInHand(hand, resAndCost.getFirst());
        }
    }

    protected static boolean removeNeededIngredients(ServerPlayerEntity player, CraftingInventory inv) {
        if (player.isCreative()) return true; // Always succeed if in creative mode

        NonNullList<ItemStack> toRemoves = NonNullList.create();
        for (int i = 0; i < 9; i++) {
            toRemoves.add(inv.getItem(i));
        }

        if (!doAndSimulateRemove(false, toRemoves, player.inventory.items)) {
            return false;
        }
        doAndSimulateRemove(true, toRemoves, player.inventory.items);
        return true;
    }

    /**
     * Checks/actually removes items from an inventory
     * @param doRemoval Whether to actually remove the items from the inventory or not
     * @param toRemoves List with items to remove/check. The "recipe".
     * @param inventory List of items to remove from/check. The "inventory"
     * @return true if all removals can be done or were done. false otherwise
     */
    private static boolean doAndSimulateRemove(boolean doRemoval, AbstractList<ItemStack> toRemoves,
                                               AbstractList<ItemStack> inventory) {
        if (!doRemoval) {
            NonNullList<ItemStack> inventoryClone = NonNullList.create();
            for (ItemStack s : inventory) {
                inventoryClone.add(s.copy());
            }
            inventory = inventoryClone;
        }

        for (ItemStack toRemove : toRemoves) {
            boolean didRemoval = false;
            if (toRemove.isEmpty()) continue; // We can always remove nothingness, and we don't actually remove it

            // Error out if item count isn't exactly 1 (this function depends on it)
            if (toRemove.getCount() != 1) throw new IllegalArgumentException("Must remove exactly 1 item.");

            for (ItemStack invItem : inventory) {
                if (invItem.isEmpty()) continue; // No way we can equal an empty item
                boolean tagsBothNullOrNot = invItem.hasTag() == toRemove.hasTag();
                boolean sameBaseItem = invItem.getItem() == toRemove.getItem();
                boolean equalTags;
                if (tagsBothNullOrNot) {
                    if (invItem.hasTag()) {
                        equalTags = invItem.getTag().equals(toRemove.getTag()); // Actually compare tags
                    } else {
                        equalTags = true; // Both items have no tags
                    }
                } else {
                    equalTags = false; // One item tag is null, the other isn't
                }
                if (tagsBothNullOrNot && sameBaseItem && equalTags) {
                    invItem.shrink(1);
                    didRemoval = true;
                    break;
                }
            }
            if (!didRemoval) {
                return false;
            }
        }
        return true;
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
            level = ((RepairContainer) container).getCost();
        }
        return new Pair<>(res, level);
    }
}
