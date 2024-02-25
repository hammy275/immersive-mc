package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantingData {

    public final ETableData weakData = new ETableData();
    public final ETableData midData = new ETableData();
    public final ETableData strongData = new ETableData();

    public static class ETableData {
        public int levelsNeeded;
        public Component textPreview = null;

        public boolean isPresent() {
            return this.textPreview != null;
        }

        public void set(int xpLevels, int enchHint, int levelHint) {
            Enchantment ench = Enchantment.byId(enchHint);
            if (ench != null) {
                this.levelsNeeded = xpLevels;
                this.textPreview = new TextComponent(ench.getFullname(levelHint).getString() + "...?");
            } else {
                this.textPreview = null;
            }
        }
    }
}
