package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.api_impl.ConstantItemSwapAmount;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.storage.world.impl.ETableWorldStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

import java.util.Arrays;
import java.util.List;

public class ETableHandler extends ItemWorldStorageHandler<ETableStorage> {
    @Override
    public ETableStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ETableWorldStorage worldStorage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        ETableStorage storage = new ETableStorage(Arrays.asList(worldStorage.getItemsRaw()));

        if (worldStorage.getItem(0) != null && !worldStorage.getItem(0).isEmpty()) {
            BlockEntity tileEnt = player.level().getBlockEntity(pos);
            if (tileEnt instanceof EnchantmentTableBlockEntity) {
                if (Apoth.apothImpl.enchantModuleEnabled()) {
                    // Null checking is done here in case of an exception causing CompatModule to give us a null value
                    ETableStorage.SlotData[] slots = Apoth.apothImpl.getEnchData(player, pos, worldStorage.getItem(0));
                    if (slots != null) storage.slots = slots;
                    ApothStats stats = Apoth.apothImpl.getStats(player.level(), pos, worldStorage.getItem(0).getItem().getEnchantmentValue());
                    if (stats != null) storage.apothStats = stats;
                } else {
                    EnchantmentMenu container = new EnchantmentMenu(-1,
                            player.getInventory(), ContainerLevelAccess.create(player.level(), pos));
                    container.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
                    container.setItem(0, 0, worldStorage.getItem(0));
                    for (int i = 0; i <= 2; i++) {
                        storage.slots[i] = new ETableStorage.SlotData(container.costs[i], List.of(container.enchantClue[i]), List.of(container.levelClue[i]));
                    }
                }
            }
        }

        return storage;
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        if (Apoth.apothImpl.enchantModuleEnabled()) {
            WorldStorage storage = WorldStoragesImpl.getS(pos, player.serverLevel());
            if (storage instanceof ETableWorldStorage ews) {
                ews.setDirtyFromApothStats(Apoth.apothImpl.getStats(player.level(), pos, 1));
            }
        }
        return super.isDirtyForClientSync(player, pos);
    }

    @Override
    public ETableStorage getEmptyNetworkStorage() {
        return new ETableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        if (player == null) return;
        ETableWorldStorage enchStorage = (ETableWorldStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        if (slot == 0) {
            ItemStack toEnchant = player.getItemInHand(hand);
            // Apotheosis allows placing any item in
            if (!toEnchant.isEmpty() && (!toEnchant.isEnchantable() && !Apoth.apothImpl.enchantModuleEnabled())) return;
            if (enchStorage.getItem(0).isEmpty()) {
                enchStorage.placeItem(player, hand, slot, new ConstantItemSwapAmount(1));
            } else {
                Util.placeLeftovers(player, enchStorage.getItem(0));
                enchStorage.setItem(0, ItemStack.EMPTY);
            }
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
    public boolean enabledInConfig(Player player) {
        return (ActiveConfig.getActiveConfigCommon(player).useEnchantingTableImmersive && !Apoth.apothImpl.suppressVanillaEnchanting()) ||
                (ActiveConfig.getActiveConfigCommon(player).useApotheosisEnchantmentTableImmersive && Apoth.apothImpl.suppressVanillaEnchanting());
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
