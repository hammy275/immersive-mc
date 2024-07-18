package com.hammy275.immersivemc.api.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An object that contains info for placing information on ImmersiveMC's config screen. You may return this from an
 * Immersive if you want config buttons to be placed on ImmersiveMC's config screen, or you may choose not to, and
 * instead handle configuration screens through some other method, or simply not have any.
 * <br>
 * It's recommended to get an instance of this interface using
 * {@link ImmersiveMCClientRegistration#createConfigScreenInfoOneItem(String, String, Supplier, Component, Supplier, Consumer)}
 * as it's easier to use. If you do implement this interface though, all methods should return the same value every
 * time it is called.
 */
public interface ImmersiveConfigScreenInfo {

    /**
     * Get the mod ID for the mod this Immersive is from. The mod name should be the name of the mod
     * that provides the Immersive, not the mod that provides the Immersive itself. For example, for the support
     * for Lootr blocks that ImmersiveMC provides, the mod returned here should be ImmersiveMC, NOT Lootr.
     * @return The mod name of the mod that implemented this Immersive.
     */
    public String getModID();

    /**
     * Get the translation string for the button label, not including the enabled/disabled state.
     * <br>
     * For example, as of writing, the Furnace Immersive returns "config.immersivemc.furnace", which becomes
     * "Use Furnace Immersion" when the language is set to U.S. English.
     * @return The String for a translation key for the title of this option.
     */
    public String getOptionTranslation();

    /**
     * Gets the ItemStacks representing this config. As of writing, this currently goes unused in ImmersiveMC, but is
     * planned to be used in the future. If there are no good items representing the Immersive, you may return
     * an empty set here.
     * @return A set of ItemStacks that best represents this Immersive, or an empty set if none such ItemStack exists.
     */
    public Set<ItemStack> getOptionItems();

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
