package com.hammy275.immersivemc.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VaultBlockEntity.Server.class)
public interface VaultBlockEntityServerInvoker {

    @Invoker("isValidToInsert")
    public static boolean immersiveMC$isValidToInsert(VaultConfig vaultConfig, ItemStack key) {
        throw new AssertionError("Method body should not exist.");
    }
}
