package net.blf02.immersivemc.client.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ClientStorage {

    // ETable
    public static final ETableInfo weakInfo = new ETableInfo();
    public static final ETableInfo midInfo = new ETableInfo();
    public static final ETableInfo strongInfo = new ETableInfo();
    public static ItemStack eTableItem = ItemStack.EMPTY;
    public static ItemStack eTableEnchCopy = ItemStack.EMPTY;

    public static void resetEnchs() {
        weakInfo.textPreview = null;
        midInfo.textPreview = null;
        strongInfo.textPreview = null;
        eTableItem = ItemStack.EMPTY;
        eTableEnchCopy = ItemStack.EMPTY;
    }

    public static class ETableInfo {
        public int levelsNeeded;
        public ITextComponent textPreview = null;

        public boolean isPresent() {
            return this.textPreview != null;
        }
    }

}
