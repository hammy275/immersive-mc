package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.ImmersiveMCClient;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationEventImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ImmersiveMCClientRegistrationImpl implements ImmersiveMCClientRegistration {

    public static final ImmersiveMCClientRegistration INSTANCE = new ImmersiveMCClientRegistrationImpl();
    private static final Set<Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>>> HANDLERS = new HashSet<>();
    private static boolean didRegistration = false;

    public static void doImmersiveRegistration(Consumer<Immersive<?, ?>> immersiveConsumer) {
        if (didRegistration) {
            throw new IllegalStateException("Already did Immersive registration!");
        }
        ImmersiveMCRegistrationEvent<Immersive<?, ?>> event = new ImmersiveMCRegistrationEventImpl<>(immersiveConsumer);
        for (Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> handler : HANDLERS) {
            handler.accept(event);
        }
        ImmersiveMCClient.immersiveIMCRegistrationHandler.accept(event); // Register ImmersiveMC's Immersives last
        didRegistration = true;
    }

    @Override
    public ImmersiveConfigScreenInfo createConfigScreenInfoOneItem(String modID, String optionTranslation, Supplier<ItemStack> optionItem, @Nullable Component optionTooltip, Supplier<Boolean> isEnabledSupplier, Consumer<Boolean> setEnabledConsumer) {
        return createConfigScreenInfoMultipleItems(modID, optionTranslation, () -> Set.of(optionItem.get()),
                optionTooltip, isEnabledSupplier, setEnabledConsumer);
    }

    @Override
    public ImmersiveConfigScreenInfo createConfigScreenInfoMultipleItems(String modID, String optionTranslation, Supplier<Set<ItemStack>> optionItem,
                                                                         @Nullable Component optionTooltip, Supplier<Boolean> isEnabledSupplier,
                                                                         Consumer<Boolean> setEnabledConsumer) {
        return new ImmersiveConfigScreenInfoImpl(modID, optionTranslation, optionItem, optionTooltip, isEnabledSupplier, setEnabledConsumer);
    }

    @Override
    public void addImmersiveRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> registrationHandler) throws IllegalStateException {
        synchronized (this) {
            if (didRegistration) {
                throw new IllegalStateException("Can't add a registration handler for Immersives after Immersives have been registered.");
            }
            HANDLERS.add(registrationHandler);
        }
    }
}
