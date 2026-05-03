package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

public class EnchantingTableExtraDataRenderState {

    public ItemStack firstItem;
    public BookDataRenderState bookDataRenderState = new BookDataRenderState();
    public boolean isTableOpen;
    public float rot;

    public static void extractRenderState(BuiltImmersiveInfo<EnchantingData> info, EnchantingTableExtraDataRenderState target) {
        target.firstItem = info.getItem(0);
        info.getExtraData().getBookData(target.firstItem).extractRenderState(target.bookDataRenderState);
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof EnchantingTableBlockEntity eTable) {
            target.isTableOpen = eTable.open == 1f;
            target.rot = eTable.rot;
        }
    }

}
