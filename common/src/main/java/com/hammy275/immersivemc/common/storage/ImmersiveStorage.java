package com.hammy275.immersivemc.common.storage;

import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.PlayerConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

/**
 * Holds all info for a given block's storage.
 */
public class ImmersiveStorage {

    /**
     * Instance of WorldStorage. Will always exist server-side and NEVER exist client-side.
     * Do not use this in a constructor! Clients also use the constructor!
     */
    public final SavedData wStorage;

    /**
     * A unique String representing the type of storage this is. Used in WorldStorage when saving/loading NBT.
     */
    public static final String TYPE = "basic_item_store";

    /**
     * A list of items. Usually contains inputs and outputs.
     */
    protected ItemStack[] items;

    /**
     * Item counts from each player for each slot. Used for item returns.
     */
    protected List<PlayerItemCounts>[] itemCounts;

    public String identifier = "world";

    public ImmersiveStorage(SavedData storage) {
        this.wStorage = storage;
    }

    /**
     * Initializes this storage ONLY IF IT ISN'T ALREADY!!!
     * @param numOfItems Number of items to store
     * @return This object.
     */
    public ImmersiveStorage initIfNotAlready(int numOfItems) {
        if (items == null) {
            items = new ItemStack[numOfItems];
            Arrays.fill(items, ItemStack.EMPTY);
            itemCounts = new LinkedList[numOfItems];
            for (int i = 0; i < numOfItems; i++) {
                itemCounts[i] = new LinkedList<>();
            }
            this.wStorage.setDirty();
        }
        return this;
    }

    /**
     * Used to determine which storage type is being loaded from disk. MUST BE CHANGED FOR ANYTHING THAT
     * EXTENDS THIS CLASS, AND IT MUST BE UNIQUE!!!
     * @return A String ID of what type of storage instance this is
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Directly set stack into slot.
     * @param slot Slot to place into
     * @param stack Stack to place
     */
    public void setItem(int slot, ItemStack stack) {
        this.items[slot] = stack;
        this.itemCounts[slot].clear();
        this.wStorage.setDirty();
    }

    /**
     * Shrink slot, adjusting the leftovers along the way.
     * @param slot Slot to shrink from
     * @param amount Amount to shrink by
     */
    public void shrinkSlot(int slot, int amount) {
        this.items[slot].shrink(amount);
        this.shrinkCountsOnly(slot, amount);
        this.wStorage.setDirty();
    }

    /**
     * Shrink a slot, but only for the itemCounts, not the actual stored items!
     * @param slot Slot to shrink from
     * @param amount Amount to shrink by
     */
    public void shrinkCountsOnly(int slot, int amount) {
        while (amount > 0 && !this.itemCounts[slot].isEmpty()) {
            PlayerItemCounts counts = this.itemCounts[slot].get(0);
            int toSubtract = Math.min(amount, counts.count);
            amount -= toSubtract;
            counts.count -= toSubtract;
            if (counts.count == 0) {
                this.itemCounts[slot].remove(0);
            }
        }
    }

    /**
     * Attempts to merge contents of hand into the given slot, returning any leftovers or a new handStack.
     * @param player Player to get items from
     * @param hand Hand to get item from
     * @param slot Slot to merge into
     *
     */
    public void placeItem(Player player, InteractionHand hand, int amountToPlace, int slot) {
        boolean shouldReturnItems = PlayerConfigs.getConfig(player).returnItems;
        ItemStack toHand;
        ItemStack toImmersive;
        ItemStack leftovers;
        ItemStack handStack = player.getItemInHand(hand);
        ItemStack immersiveStack = this.items[slot];
        if (Util.stacksEqualBesidesCount(handStack, this.items[slot])) {
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
            PlayerItemCounts last = this.itemCounts[slot].get(this.itemCounts[slot].size() - 1);
            int itemsMoved = immersiveStack.getCount() - oldImmersiveCount;
            if (shouldReturnItems && last.uuid.isPresent() && last.uuid.get().equals(player.getUUID())) {
                last.count += itemsMoved;
            } else if (shouldReturnItems) {
                this.itemCounts[slot].add(new PlayerItemCounts(Optional.of(player.getUUID()), itemsMoved));
            } else if (last.uuid.isEmpty()) {
                last.count += itemsMoved;
            } else {
                this.itemCounts[slot].add(new PlayerItemCounts(Optional.empty(), itemsMoved));
            }
        } else if (handStack.isEmpty()) {
            toHand = immersiveStack;
            toImmersive = ItemStack.EMPTY;
            leftovers = ItemStack.EMPTY;
            this.itemCounts[slot].clear();
        } else { // Slots contain different item types and hand isn't air (place new stack in and old items go somewhere)
            toHand = handStack.copy();
            toHand.shrink(amountToPlace);
            toImmersive = handStack.copy();
            toImmersive.setCount(amountToPlace);
            leftovers = immersiveStack.copy();
            this.itemCounts[slot].add(
                    new PlayerItemCounts(Optional.ofNullable(shouldReturnItems ? player.getUUID() : null), amountToPlace));
        }
        this.items[slot] = toImmersive;
        player.setItemInHand(hand, toHand);
        Util.placeLeftovers(player, leftovers);
        this.wStorage.setDirty();
    }

    public ItemStack getItem(int slot) {
        return this.items[slot];
    }

    /**
     * Get item array directly. Only use for immersives that don't utilize the queue.
     * @return Items array.
     */
    public ItemStack[] getItemsRaw() {
        return this.items;
    }

    /**
     * Return items to player when leaving radius
     * @param player Player to return items to
     */
    public void returnItems(Player player) {
        for (int slot = 0; slot < this.itemCounts.length; slot++) {
            Stack<Integer> countsToRemove = new Stack<>();
            for (int countIndex = 0; countIndex < this.itemCounts[slot].size(); countIndex++) {
                PlayerItemCounts counts = this.itemCounts[slot].get(countIndex);
                if (counts.uuid.isPresent() && counts.uuid.get().equals(player.getUUID())) {
                    countsToRemove.add(countIndex);
                    ItemStack ret = this.items[slot].copy();
                    ret.setCount(counts.count);
                    Util.placeLeftovers(player, ret);
                    this.items[slot].shrink(counts.count);
                }
            }
            while (!countsToRemove.isEmpty()) {
                this.itemCounts[slot].remove((int) countsToRemove.pop());
            }
        }
        this.wStorage.setDirty();
    }


    public void load(CompoundTag nbt) {
        int length = nbt.getInt("numOfItems");
        this.items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            this.items[i] = ItemStack.of(nbt.getCompound("item" + i));
        }
        this.identifier = nbt.getString("identifier");
        if (this.identifier.equals("")) {
            this.identifier = "world"; // Safe default if string isn't there
        }
        itemCounts = new LinkedList[length];
        for (int i = 0; i < length; i++) {
            itemCounts[i] = new LinkedList<>();
        }
        if (nbt.contains("itemCounts")) {
            CompoundTag rootCounts = nbt.getCompound("itemCounts");
            for (int i = 0; i < length; i++) {
                CompoundTag slotTag = rootCounts.getCompound("slot" + i);
                int numOfCounts = slotTag.getInt("numOfItems");
                for (int j = 0; j < numOfCounts; j++) {
                    itemCounts[i].add(PlayerItemCounts.load(slotTag.getCompound(String.valueOf(j))));
                }
            }
        }
    }

    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("numOfItems", items.length);
        for (int i = 0; i < items.length; i++) {
            nbt.put("item" + i, items[i].save(new CompoundTag()));
        }
        nbt.putString("identifier", identifier);
        CompoundTag rootCounts = new CompoundTag();
        for (int slot = 0; slot < itemCounts.length; slot++) {
            CompoundTag countSlot = new CompoundTag();
            countSlot.putInt("numOfItems", itemCounts[slot].size());
            for (int countIndex = 0; countIndex < itemCounts[slot].size(); countIndex++) {
                PlayerItemCounts count = itemCounts[slot].get(countIndex);
                countSlot.put(String.valueOf(countIndex), count.save());
            }
            rootCounts.put("slot" + slot, countSlot);
        }
        nbt.put("itemCounts", rootCounts);
        return nbt;
    }

    /**
     * Moves the slot at position oldSlot to position newSlot. Only be used for version conversion, such as the
     * addition of smithing templates in 1.20.
     * The old slot will be made into air and have its item counts wiped.
     * @param oldSlot Old slot number
     * @param newSlot New slot number
     */
    public void moveSlot(int oldSlot, int newSlot) {
        this.items[newSlot] = this.items[oldSlot];
        this.itemCounts[newSlot] = this.itemCounts[oldSlot];
        this.items[oldSlot] = ItemStack.EMPTY;
        this.itemCounts[oldSlot] = new LinkedList<>();
        this.wStorage.setDirty();
    }

    /**
     * Add slotsToAdd number of slots. Really should only be used for version conversion.
     * @param slotsToAdd Number of slots to add. Will be added at the end of arrays.
     */
    public void addSlotsToEnd(int slotsToAdd) {
        ItemStack[] oldItems = this.items;
        List<PlayerItemCounts>[] oldItemCounts = this.itemCounts;
        this.items = new ItemStack[oldItems.length + slotsToAdd];
        Arrays.fill(this.items, ItemStack.EMPTY);
        this.itemCounts = new LinkedList[oldItemCounts.length + slotsToAdd];
        for (int i = 0; i < oldItems.length; i++) {
            this.items[i] = oldItems[i];
        }
        for (int i = 0; i < oldItemCounts.length; i++) {
            this.itemCounts[i] = oldItemCounts[i];
        }
        for (int i = oldItemCounts.length; i < this.itemCounts.length; i++) {
            this.itemCounts[i] = new LinkedList<>();
        }
        this.wStorage.setDirty();
    }

    public static class PlayerItemCounts {
        public final Optional<UUID> uuid;
        public int count;

        public PlayerItemCounts(Optional<UUID> uuid, int count) {
            this.uuid = uuid;
            this.count = count;
        }

        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("uuid", this.uuid.isEmpty() ? "null" : this.uuid.get().toString());
            nbt.putInt("count", this.count);
            return nbt;
        }

        public static PlayerItemCounts load(CompoundTag nbt) {
            String uuidString = nbt.getString("uuid");
            UUID uuid = uuidString.equals("null") || uuidString.isEmpty() ? null : UUID.fromString(uuidString);
            int count = nbt.getInt("count");
            return new PlayerItemCounts(Optional.ofNullable(uuid), count);
        }
    }
}
