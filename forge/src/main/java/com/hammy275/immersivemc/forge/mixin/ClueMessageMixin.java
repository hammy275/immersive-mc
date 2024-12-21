package com.hammy275.immersivemc.forge.mixin;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.forge.ApothCompatImpl;
import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadows.apotheosis.ench.table.ClueMessage;

import java.util.List;

@Mixin(ClueMessage.class)
public class ClueMessageMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void immersiveMC$captureClueData(int slot, List<EnchantmentInstance> clues, boolean all, CallbackInfo ci) {
        if (clues != null) {
            ApothCompatImpl.slotsToSend[slot] = new ETableStorage.SlotData(-1,
                    clues.stream().map(clue -> Registry.ENCHANTMENT.getId(clue.enchantment)).toList(),
                    clues.stream().map(clue -> clue.level).toList());
        }
    }
}
