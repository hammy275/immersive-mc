package com.hammy275.immersivemc.forge.mixin;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.forge.ApothCompatImpl;
import dev.shadowsoffire.apotheosis.ench.table.ClueMessage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClueMessage.class)
public class ClueMessageMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureClueData(int slot, List<EnchantmentInstance> clues, boolean all, CallbackInfo ci) {
        ApothCompatImpl.slotsToSend[slot] = new ETableStorage.SlotData(-1,
                clues.stream().map(clue -> BuiltInRegistries.ENCHANTMENT.getId(clue.enchantment)).toList(),
                clues.stream().map(clue -> clue.level).toList());
    }
}
