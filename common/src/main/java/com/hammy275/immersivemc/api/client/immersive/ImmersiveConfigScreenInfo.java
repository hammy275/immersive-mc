package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface ImmersiveConfigScreenInfo {

    /**
     * Get the Component for the button label, not including the enabled/disabled state.
     * <br>
     * For example, as of writing, the Furnace Immersive returns a Component that becomes "Use Furnace Immersion" when
     * the language is set to U.S. English.
     * @return The Component for the button for this config option.
     */
    public Component getOptionName();

    /**
     * Get the Component representing the description of this option.
     * @return The description of this option, or null if no description should be shown.
     */
    @Nullable
    public Component getOptionTooltip();

    /**
     * @return Whether the config file currently has this Immersive enabled.
     */
    public boolean isEnabled();

    /**
     * The method called to set the config value for enabling/disabling this Immersive.
     * @param newValue Whether the Immersive is now enabled.
     */
    public void setEnabled(boolean newValue);
}
