package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TCCraftingStationHandler extends ContainerHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>();
        Container inv = (Container) player.level().getBlockEntity(pos);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            items.add(inv.getItem(i));
        }
        items.add(Swap.getRecipeOutput(player, items.toArray(new ItemStack[inv.getContainerSize()])));
        return new ListOfItemsStorage(items, items.size());
    }

    @Override
    public HandlerStorage getEmptyHandlerStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        Container table = (Container) player.level().getBlockEntity(pos);
        ImmersiveStorage storage = GetStorage.getCraftingStorage(player, pos);
        ItemStack playerItem = player.getItemInHand(hand).copy();
        ItemStack craftingItem = table.getItem(slot).copy();
        if (slot < 9) {
            // Only set the output item into our storage since everything else is rendered by TC
            Swap.SwapResult result = Swap.getSwap(playerItem, craftingItem, mode);
            Swap.givePlayerItemSwap(result.toHand, playerItem, player, hand);
            table.setItem(slot, result.toOther);
            Util.placeLeftovers(player, result.leftovers);
            ItemStack[] ins = new ItemStack[10];
            for (int i = 0; i <= 8; i++) {
                ins[i] = table.getItem(i);
                storage.getItemsRaw()[i] = ItemStack.EMPTY;
            }
            ins[9] = ItemStack.EMPTY;
            ItemStack output = Swap.getRecipeOutput(player, ins);
            storage.getItemsRaw()[9] = output;
        } else {
            // At crafting time, make our storage match the table contents, craft like a vanilla table,
            // then put our storage back to empty after cloning our crafting results back over
            for (int i = 0; i <= 8; i++) {
                storage.getItemsRaw()[i] = table.getItem(i).copy();
            }
            Swap.handleDoCraft(player, storage.getItemsRaw(), pos);
            for (int i = 0; i <= 8; i++) {
                // setItem here instead of using non-copies so setItem can sync stuff back
                table.setItem(i, storage.getItemsRaw()[i]);
                storage.getItemsRaw()[i] = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return TinkersConstruct.craftingStation.isInstance(level.getBlockEntity(pos));
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useTinkersConstructCraftingStationImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "tinkers_construct_crafting_station");
    }
}
