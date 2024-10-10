package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.SwapResult;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.ServerUtil;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

/**
 * Functions both as WorldStorage for saving server side and as a NetworkStorage for sending items
 * to the client. Note that only the items array is sent to the client, no other fields are!
 */
public abstract class ItemStorage implements WorldStorage, NetworkStorage {

    /**
     * A list of items. Usually contains inputs and outputs.
     */
    protected ItemStack[] items;

    /**
     * Item counts from each player for each slot. Used for item returns.
     */
    protected List<PlayerItemCounts>[] itemCounts;
    /**
     * Whether this storage has changed since the last sync to the client.
     */
    private boolean isDirtyForClientSync = false;
    /**
     * The index of the last item in the items array that is an input item.
     */
    public final int maxInputIndex;

    public ItemStorage(int numItems, int maxInputIndex) {
        items = new ItemStack[numItems];
        Arrays.fill(items, ItemStack.EMPTY);
        itemCounts = new LinkedList[numItems];
        for (int i = 0; i < numItems; i++) {
            itemCounts[i] = new LinkedList<>();
        }
        this.maxInputIndex = maxInputIndex;
    }

    public boolean isDirtyForClientSync() {
        return this.isDirtyForClientSync;
    }

    public void setDirty(ServerLevel level) {
        WorldStoragesImpl.markDirtyS(level);
        this.isDirtyForClientSync = true;
    }

    public void setNoLongerDirtyForClientSync() {
        this.isDirtyForClientSync = false;
    }

    /**
     * Directly set stack into slot.
     * @param slot Slot to place into
     * @param stack Stack to place
     */
    public void setItem(int slot, ItemStack stack) {
        this.items[slot] = stack;
        this.itemCounts[slot].clear();
    }

    /**
     * Shrink slot, adjusting the leftovers along the way.
     * @param slot Slot to shrink from
     * @param amount Amount to shrink by
     */
    public void shrinkSlot(int slot, int amount) {
        this.items[slot].shrink(amount);
        this.shrinkCountsOnly(slot, amount);
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
     * @param amount Amount of items to swap
     *
     */
    public void placeItem(Player player, InteractionHand hand, int slot, ItemSwapAmount amount) {
        ItemStack playerStack = player.getItemInHand(hand);
        ItemStack otherStack = this.getItem(slot);
        SwapResult result = Swap.swapItems(playerStack, otherStack, amount,
                incrementAmount -> incrementCountForPlayer(player, incrementAmount, slot),
                ignored -> this.itemCounts[slot].clear());
        result.giveToPlayer(player, hand);
        this.items[slot] = result.immersiveStack(); // Set without clearing item counts, since those are updated above
        if (player instanceof ServerPlayer sp) {
            setDirty(sp.serverLevel());
        }
    }

    /**
     * Increments the return item count for the provided player.
     * @param player Player to increment for.
     * @param amount Amount to increment.
     * @param slot Slot to increment in.
     */
    public void incrementCountForPlayer(Player player, int amount, int slot) {
        boolean shouldReturnItems = ActiveConfig.getConfigForPlayer(player).returnItemsWhenLeavingImmersives;
        ItemStorage.PlayerItemCounts last = this.itemCounts[slot].isEmpty() ? null : this.itemCounts[slot].get(this.itemCounts[slot].size() - 1);
        if (last != null && shouldReturnItems && last.uuid.isPresent() && last.uuid.get().equals(player.getUUID())) {
            last.count += amount;
        } else if (shouldReturnItems) {
            this.itemCounts[slot].add(new ItemStorage.PlayerItemCounts(Optional.of(player.getUUID()), amount));
        } else if (last != null && last.uuid.isEmpty()) {
            last.count += amount;
        } else {
            this.itemCounts[slot].add(new ItemStorage.PlayerItemCounts(Optional.empty(), amount));
        }
    }

    public ItemStack getItem(int slot) {
        return this.items[slot];
    }

    public int getNumItems() {return this.items.length;}

    /**
     * Get item array directly. Only use for immersives that don't utilize the queue.
     * @return Items array.
     */
    public ItemStack[] getItemsRaw() {
        return this.items;
    }

    public void copyFromOld(ItemStorage oldStorage) {
        this.items = oldStorage.items;
        this.itemCounts = oldStorage.itemCounts;
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
    }
    
    @Override
    public void load(CompoundTag nbt, int lastVanillaDataVersion) {
        int length = nbt.getInt("numOfItems");
        this.items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            this.items[i] = ServerUtil.parseItem(nbt.getCompound("item" + i), lastVanillaDataVersion);
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

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("numOfItems", items.length);
        for (int i = 0; i < items.length; i++) {
            nbt.put("item" + i, items[i].save(new CompoundTag()));
        }
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
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        for (ItemStack item : this.items) {
            buffer.writeItem(item);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = buffer.readItem();
        }
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
