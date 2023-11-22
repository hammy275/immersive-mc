package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.network.chat.Component;

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
    }
}
