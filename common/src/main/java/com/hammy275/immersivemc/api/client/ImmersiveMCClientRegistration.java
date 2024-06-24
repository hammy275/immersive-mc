package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.api_impl.ImmersiveMCClientRegistrationImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
     * @param optionName The component for the button label as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionName()}.
     * @param optionItem The item representing this Immersive as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionItem()}.
     * @param optionTooltip An optional tooltip describing this Immersive and this option as described in the javadoc for {@link ImmersiveConfigScreenInfo#getOptionTooltip()}.
     * @param isEnabledSupplier A supplier that, when called, returns whether this Immersive is enabled. This method should NOT ever provide null as a value.
     * @param setEnabledConsumer A consumer that will be provided a non-null Boolean denoting a new enabled/disabled state for the Immersive.
     * @return An {@link ImmersiveConfigScreenInfo} instance.
     */
    public ImmersiveConfigScreenInfo createConfigScreenInfo(String modID, Component optionName, ItemStack optionItem,
                                                            @Nullable Component optionTooltip,
                                                            Supplier<Boolean> isEnabledSupplier,
                                                            Consumer<Boolean> setEnabledConsumer);

    /**
     * Register a block Immersive.
     * @param immersive Immersive to register.
     * @throws IllegalArgumentException If the Immersive is already registered.
     */
    public void registerImmersive(Immersive<?, ?> immersive) throws IllegalArgumentException;

}
