package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothBaseCompatImpl;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothCompat;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingTableTile;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantTile;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZenithCompatImpl extends ApothBaseCompatImpl {

    public static ETableStorage.SlotData[] slotsToSend = new ETableStorage.SlotData[]{ETableStorage.SlotData.DEFAULT, ETableStorage.SlotData.DEFAULT, ETableStorage.SlotData.DEFAULT};

    // Reflection constants. Using reflection instead of Mixins so CompatModule's can catch crashes
    private static Field salvagingMenuInputInventory = null;
    private static Field salvagingTileOutput = null;

    private ZenithCompatImpl() {}

    public static ApothCompat makeCompatImpl() {
        return CompatModule.create(new ZenithCompatImpl(), ApothCompat.class, Apoth.compatData);
    }

    @Override
    public ApothStats getStats(Level level, BlockPos pos, int itemEnchantability) {
        ApothEnchantmentMenu.TableStats stats = ApothEnchantmentMenu.gatherStats(level, pos, itemEnchantability);
        return new ApothStats(stats.eterna(), stats.quanta(), stats.arcana(), stats.rectification(), stats.clues());
    }

    @Override
    public ItemStack doEnchant(Player player, BlockPos pos, int tier, ItemStack item) {
        ApothEnchantmentMenu menu = new ApothEnchantmentMenu(-1, player.getInventory(),
                ContainerLevelAccess.create(player.level(), pos), (ApothEnchantTile) player.level().getBlockEntity(pos));
        ItemStack old = menu.getItems().get(0);
        ItemStack oldLapis = menu.getItems().get(1);
        menu.setItem(0, 0, item.copy());
        menu.setItem(1, 0, new ItemStack(Items.LAPIS_LAZULI, 64));
        menu.clickMenuButton(player, tier);
        ItemStack ret = menu.getItems().get(0).copy();
        menu.setItem(0, 0, old);
        menu.setItem(1, 0, oldLapis);
        return ret;
    }

    @Override
    public ETableStorage.SlotData[] getEnchData(Player player, BlockPos pos, ItemStack item) {
        ApothEnchantmentMenu menu = new ApothEnchantmentMenu(-1, player.getInventory(),
                ContainerLevelAccess.create(player.level(), pos), (ApothEnchantTile) player.level().getBlockEntity(pos));
        ItemStack old = menu.getItems().get(0);
        Arrays.fill(slotsToSend, ETableStorage.SlotData.DEFAULT);
        menu.setItem(0, 0, item.copy());
        // slotsChanged() causes a few packet sends, which a mixin catches and puts values from into slotsToSend
        ETableStorage.SlotData[] res = new ETableStorage.SlotData[]{
                new ETableStorage.SlotData(menu.costs[0], slotsToSend[0].enchantmentHints(), slotsToSend[0].enchantmentHintLevels()),
                new ETableStorage.SlotData(menu.costs[1], slotsToSend[1].enchantmentHints(), slotsToSend[1].enchantmentHintLevels()),
                new ETableStorage.SlotData(menu.costs[2], slotsToSend[2].enchantmentHints(), slotsToSend[2].enchantmentHintLevels())
        };
        menu.setItem(0, 0, old);
        return res;
    }

    @Override
    public boolean enchantModuleEnabled() {
        return Apotheosis.enableEnch;
    }

    @Override
    public boolean isSalvagingTable(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SalvagingTableTile;
    }

    @Override
    public List<ItemStack> doSalvage(Player player, List<ItemStack> input, BlockPos pos) {
        Container items = getOutputSlots(player, pos);
        List<ItemStack> out = new ArrayList<>(items.getContainerSize());
        // Add any outputs that may already be there before the craft
        for (int i = 0; i < items.getContainerSize(); i++) {
            out.add(items.getItem(i));
            items.setItem(i, ItemStack.EMPTY);
        }
        SalvagingMenu menu = new SalvagingMenu(-1, player.getInventory(), pos);
        Container menuInputs = getInputSlots(menu);
        for (int i = 0; i < input.size(); i++) {
            menuInputs.setItem(i, input.get(i));
        }
        menu.clickMenuButton(player, 0);
        // Add outputs from the craft
        for (int i = 0; i < items.getContainerSize(); i++) {
            out.add(items.getItem(i));
            items.setItem(i, ItemStack.EMPTY);
        }
        return out;
    }

    @Override
    public boolean isSalvagable(ItemStack input, Level level) {
        return SalvagingMenu.findMatch(level, input) != null;
    }

    private Container getInputSlots(SalvagingMenu menu) {
        if (salvagingMenuInputInventory == null) {
            try {
                salvagingMenuInputInventory = SalvagingMenu.class.getDeclaredField("inputInventory");
                salvagingMenuInputInventory.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
                throw new RuntimeException("Field inputInventory did not exist in the salvaging table tile!");
            }
        }
        try {
            return (Container) salvagingMenuInputInventory.get(menu);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Container getOutputSlots(Player player, BlockPos pos) {
        if (salvagingTileOutput == null) {
            try {
                salvagingTileOutput = SalvagingTableTile.class.getDeclaredField("output");
                salvagingTileOutput.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
                throw new RuntimeException("Field output did not exist in the salvaging table tile!");
            }
        }
        try {
            return (Container) salvagingTileOutput.get(player.level().getBlockEntity(pos));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
