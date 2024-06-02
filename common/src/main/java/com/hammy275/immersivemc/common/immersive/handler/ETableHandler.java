package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.storage.world.impl.ETableWorldStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

import java.util.Arrays;

public class ETableHandler extends ItemWorldStorageHandler {
    @Override
    public NetworkStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ETableWorldStorage worldStorage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        ETableStorage storage = new ETableStorage(Arrays.asList(worldStorage.getItemsRaw()));

        if (worldStorage.getItem(0) != null && !worldStorage.getItem(0).isEmpty()) {
            BlockEntity tileEnt = player.level().getBlockEntity(pos);
            if (tileEnt instanceof EnchantmentTableBlockEntity) {
                EnchantmentMenu container = new EnchantmentMenu(-1,
                        player.getInventory(), ContainerLevelAccess.create(player.level(), pos));
                container.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
                container.setItem(0, 0, worldStorage.getItem(0));

                storage.xpLevels = container.costs;
                storage.enchantHints = container.enchantClue;
                storage.levelHints = container.levelClue;
            }
        }

        return storage;
    }

    @Override
    public NetworkStorage getEmptyNetworkStorage() {
        return new ETableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (player == null) return;
        ETableWorldStorage enchStorage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        if (slot == 0) {
            ItemStack toEnchant = player.getItemInHand(hand);
            if (!toEnchant.isEmpty() && !toEnchant.isEnchantable()) return;
            enchStorage.placeItem(player, hand, 1, slot);
        } else if (player.getItemInHand(hand).isEmpty()) {
            boolean res = Swap.doEnchanting(slot, pos, player, hand);
            if (res) {
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        enchStorage.setDirty(player.serverLevel());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof EnchantmentTableBlock;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useETableImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "enchanting_table");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new ETableWorldStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return ETableWorldStorage.class;
    }

}
