package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.client.api_impl.ImmersiveMCClientRegistrationImpl;
import com.hammy275.immersivemc.client.immersive.Immersives;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Contains methods for registering Immersives to ImmersiveMC.
 */
public interface ImmersiveMCClientRegistration {

    /**
     * @return An ImmersiveMCClientRegistration instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveMCClientRegistration instance() {
        return ImmersiveMCClientRegistrationImpl.INSTANCE;
    }

    /**
     * Creates an {@link ImmersiveConfigScreenInfo} instance. See the aforementioned interface for more information.
     * @param modID The mod ID as described in the javadoc for {@link ImmersiveConfigScreenInfo#getModID()}.
     * @param optionTranslation The translation string for the button label as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionTranslation()}.
     * @param optionItem A supplier for the item representing this Immersive as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionItems()}.
     * @param optionTooltip An optional tooltip describing this Immersive and this option as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionTooltip()}.
     * @param isEnabledSupplier A supplier that, when called, returns whether this Immersive is enabled. This method should NOT ever provide null as a value.
     * @param setEnabledConsumer A consumer that will be provided a non-null Boolean denoting a new enabled/disabled state for the Immersive.
     * @return An {@link ImmersiveConfigScreenInfo} instance.
     */
    public ImmersiveConfigScreenInfo createConfigScreenInfoOneItem(String modID, String optionTranslation,
                                                                   Supplier<ItemStack> optionItem, @Nullable Component optionTooltip,
                                                                   Supplier<Boolean> isEnabledSupplier,
                                                                   Consumer<Boolean> setEnabledConsumer);

    /**
     * Creates an {@link ImmersiveConfigScreenInfo} instance. See the aforementioned interface for more information.
     * @param modID The mod ID as described in the javadoc for {@link ImmersiveConfigScreenInfo#getModID()}.
     * @param optionTranslation The translation string for the button label as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionTranslation()}.
     * @param optionItems A supplier for the items representing this Immersive as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionItems()}.
     * @param optionTooltip An optional tooltip describing this Immersive and this option as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionTooltip()}.
     * @param isEnabledSupplier A supplier that, when called, returns whether this Immersive is enabled. This method should NOT ever provide null as a value.
     * @param setEnabledConsumer A consumer that will be provided a non-null Boolean denoting a new enabled/disabled state for the Immersive.
     * @return An {@link ImmersiveConfigScreenInfo} instance.
     */
    public ImmersiveConfigScreenInfo createConfigScreenInfoMultipleItems(String modID, String optionTranslation,
                                                                         Supplier<Set<ItemStack>> optionItems, @Nullable Component optionTooltip,
                                                                         Supplier<Boolean> isEnabledSupplier,
                                                                         Consumer<Boolean> setEnabledConsumer);

    /**
     * Registers an object which, at some point, ImmersiveMC will call to register your {@link Immersives}s.
     * The time at which registration occurs is only guaranteed to be some time after mods are initially constructed, so
     * handlers should be added here as early as possible, and be prepared for a lack of registry availability.
     * @param registrationHandler Your object that will register Immersives when called.
     * @throws IllegalStateException This method was called after registration.
     */
    public void addImmersiveRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> registrationHandler) throws IllegalStateException;

}
