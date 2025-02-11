package com.hammy275.immersivemc.neoforge.mixin;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.neoforge.ApothCompatImpl;
import com.hammy275.immersivemc.server.ServerSubscriber;
import dev.shadowsoffire.apothic_enchanting.payloads.CluePayload;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CluePayload.class)
public class CluePayloadMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureClueData(int slot, List<EnchantmentInstance> clues, boolean all, CallbackInfo ci) {
        // Instances are also constructed by the netty thread, which gets invalid enchantments when accessing
        // the registry.
        if (ServerSubscriber.server != null && ServerSubscriber.server.isSameThread()) {
            Registry<Enchantment> registry = ServerSubscriber.server.registryAccess().registry(Registries.ENCHANTMENT).get();
            ApothCompatImpl.slotsToSend[slot] = new ETableStorage.SlotData(-1,
                    clues.stream().map(clue -> registry.getId(clue.enchantment.value())).toList(),
                    clues.stream().map(clue -> clue.level).toList());
        }
    }
}
