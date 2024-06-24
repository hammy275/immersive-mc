package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ImmersiveMCClientRegistrationImpl implements ImmersiveMCClientRegistration {

    public static final ImmersiveMCClientRegistration INSTANCE = new ImmersiveMCClientRegistrationImpl();

    @Override
    public ImmersiveConfigScreenInfo createConfigScreenInfo(String modID, Component optionName, ItemStack optionItem,
                                                            @Nullable Component optionTooltip, Supplier<Boolean> isEnabledSupplier,
                                                            Consumer<Boolean> setEnabledConsumer) {
        return new ImmersiveConfigScreenInfoImpl(modID, optionName, optionItem, optionTooltip, isEnabledSupplier, setEnabledConsumer);
    }

    @Override
    public void registerImmersive(Immersive<?, ?> immersive) throws IllegalArgumentException {

    }
}
