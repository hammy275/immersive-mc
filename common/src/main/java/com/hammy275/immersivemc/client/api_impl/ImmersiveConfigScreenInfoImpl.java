package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record ImmersiveConfigScreenInfoImpl(String modID, String optionTranslation, Supplier<Set<ItemStack>> optionItem,
                                            @Nullable Component optionTooltip, Supplier<Boolean> isEnabledSupplier,
                                            Consumer<Boolean> setEnabledConsumer) implements ImmersiveConfigScreenInfo {
    @Override
    public String getModID() {
        return modID;
    }

    @Override
    public String getOptionTranslation() {
        return optionTranslation;
    }

    @Override
    public Set<ItemStack> getOptionItems() {
        return optionItem.get();
    }

    @Override
    public @Nullable Component getOptionTooltip() {
        return optionTooltip;
    }

    @Override
    public boolean isEnabled() {
        return isEnabledSupplier.get();
    }

    @Override
    public void setEnabled(boolean newValue) {
        setEnabledConsumer.accept(newValue);
    }
}
