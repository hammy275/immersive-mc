package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TCCraftingStationHandler extends ContainerHandler<ListOfItemsStorage> {
    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>();
        Container inv = (Container) player.level().getBlockEntity(pos);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            items.add(inv.getItem(i));
        }
        items.add(Swap.getRecipeOutput(player, items.toArray(new ItemStack[inv.getContainerSize()])));
        return new ListOfItemsStorage(items, items.size());
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        Container table = (Container) player.level().getBlockEntity(pos);
        ItemStack playerItem = player.getItemInHand(hand).copy();
        ItemStack craftingItem = table.getItem(slot).copy();
        if (slot < 9) {
            // Just place the item in. Recipe result is calculated in makeInventoryContents() to show the client
            // and at actual crafting time (else block below).
            Swap.SwapResult result = Swap.getSwap(playerItem, craftingItem, mode);
            Swap.givePlayerItemSwap(result.toHand, playerItem, player, hand);
            table.setItem(slot, result.toOther);
            Util.placeLeftovers(player, result.leftovers);
        } else {
            // Get the items into an array, do the craft, then put the items back.
            ItemStack[] items = new ItemStack[10];
            for (int i = 0; i <= 8; i++) {
                items[i] = table.getItem(i).copy();
            }
            Swap.handleDoCraft(player, items, pos);
            for (int i = 0; i <= 8; i++) {
                table.setItem(i, items[i]);
            }
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return TinkersConstruct.craftingStation.isInstance(level.getBlockEntity(pos));
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useTinkersConstructCraftingStationImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "tinkers_construct_crafting_station");
    }
}
