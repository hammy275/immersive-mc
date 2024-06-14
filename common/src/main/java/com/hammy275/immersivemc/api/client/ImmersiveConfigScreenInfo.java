package com.hammy275.immersivemc.api.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ImmersiveConfigScreenInfo {

    /**
     * Get the Component for the name of the mod this Immersive is from. The mod name should be the name of the mod
     * that provides the Immersive, not the mod that provides the Immersive itself. For example, for the support
     * for Lootr blocks that ImmersiveMC provides, the mod returned here should be ImmersiveMC, NOT Lootr.
     * @return The mod name of the mod that implemented this Immersive.
     */
    public Component getModName();

    /**
     * Get the Component for the button label, not including the enabled/disabled state.
     * <br>
     * For example, as of writing, the Furnace Immersive returns a Component that becomes "Use Furnace Immersion" when
     * the language is set to U.S. English.
     * @return The Component for the button for this config option.
     */
    public Component getOptionName();

    /**
     * Get an ItemStack representing this config. As of writing, this currently goes unused in ImmersiveMC, but is
     * planned to be used in the future. If there is no good item representing the Immersive, you may return
     * an empty ItemStack here.
     * @return An ItemStack that best represents this Immersive, or an empty ItemStack if none such ItemStack exists.
     */
    public ItemStack getOptionItem();

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
