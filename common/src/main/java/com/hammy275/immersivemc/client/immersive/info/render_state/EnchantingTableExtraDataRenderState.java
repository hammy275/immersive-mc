package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.info.EnchantingData;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableExtraDataRenderState {

    public ItemStack firstItem;
    public @Nullable BookDataRenderState bookDataRenderState = null;
    public boolean isTableOpen;
    public float rot;
    public boolean hasPlayerNearby;

    public static void extractRenderState(BuiltImmersiveInfo<EnchantingData> info, EnchantingTableExtraDataRenderState target) {
        target.firstItem = info.getItem(0);
        ClientBookData bookData = info.getExtraData().getBookData(target.firstItem);
        if (bookData == null) {
            target.bookDataRenderState = null;
        } else {
            target.bookDataRenderState = new BookDataRenderState();
            bookData.extractRenderState(target.bookDataRenderState);
        }
        if (Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition()) instanceof EnchantmentTableBlockEntity eTable) {
            target.isTableOpen = eTable.open == 1f;
            target.rot = eTable.rot;
        }
        if (Apoth.apothImpl.enchantModuleEnabled()) {
            BlockPos pos = info.getBlockPosition();
            target.hasPlayerNearby = Minecraft.getInstance().level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false) != null;
        } else {
            target.hasPlayerNearby = false;
        }
    }

}
