package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.RGBA;
import net.minecraft.util.Mth;

public final class ClientActiveConfig extends ActiveConfig {

    public static final ClientActiveConfig DISABLED = new ClientActiveConfig();

    public boolean crouchingBypassesImmersives = false;
    public boolean doVRControllerRumble = true;
    public boolean returnItemsWhenLeavingImmersives = true;
    public boolean disableImmersiveMCOutsideVR = false;
    public int bagColor = 11901820;
    public boolean rightClickChestInteractions = false;
    public boolean autoCenterFurnaceImmersive = false;
    public boolean autoCenterBrewingStandImmersive = false;
    public BackpackMode bagMode = BackpackMode.BUNDLE;
    public PlacementGuideMode placementGuideMode = PlacementGuideMode.CUBE;
    public PlacementMode placementMode = PlacementMode.PLACE_ONE;
    public boolean spinSomeImmersiveOutputs = true;
    public boolean rightClickImmersiveInteractionsInVR = false;
    public boolean compatFor3dResourcePacks = false;
    public double itemGuideSize = 1.0;
    public double itemGuideSelectedSize = 1.0;
    public RGBA itemGuideColor = new RGBA(0x3300ffffL);
    public RGBA itemGuideSelectedColor = new RGBA(0x3300ff00L);
    public RGBA rangedGrabColor = new RGBA(0xff00ffffL);
    public boolean disableVanillaInteractionsForSupportedImmersives = false;
    public ReachBehindBackpackMode reachBehindBagMode = ReachBehindBackpackMode.BEHIND_BACK;

    static {
        DISABLED.setDisabled();
    }

    @Override
    public void validateConfig() {
        super.validateConfig();
        // NOTE: RGBAs are validated during deserialization, so no need to cover that here. Only need to null check.
        bagColor = Mth.clamp(bagColor, 0, 0xFFFFFF);
        bagMode = firstEnumIfNull(bagMode, BackpackMode.class);
        placementGuideMode = firstEnumIfNull(placementGuideMode, PlacementGuideMode.class);
        placementMode = firstEnumIfNull(placementMode, PlacementMode.class);
        itemGuideSize = Mth.clamp(itemGuideSize, 0, 1);
        itemGuideSelectedSize = Mth.clamp(itemGuideSelectedSize, 0, 1);
        itemGuideColor = defaultIfNull(itemGuideColor, new RGBA(0x3300ffffL));
        itemGuideSelectedColor = defaultIfNull(itemGuideSelectedColor, new RGBA(0x3300ff00L));
        rangedGrabColor = defaultIfNull(rangedGrabColor, new RGBA(0xff00ffffL));
        reachBehindBagMode = firstEnumIfNull(reachBehindBagMode, ReachBehindBackpackMode.class);
    }

    @Override
    public void setDisabled() {
        super.setDisabled();
        crouchingBypassesImmersives = false;
        doVRControllerRumble = false;
        returnItemsWhenLeavingImmersives = false;
        disableImmersiveMCOutsideVR = false;
        bagColor = 11901820;
        rightClickChestInteractions = false;
        autoCenterFurnaceImmersive = false;
        autoCenterBrewingStandImmersive = false;
        bagMode = BackpackMode.BUNDLE;
        placementGuideMode = PlacementGuideMode.CUBE;
        placementMode = PlacementMode.PLACE_ONE;
        spinSomeImmersiveOutputs = true;
        rightClickImmersiveInteractionsInVR = false;
        compatFor3dResourcePacks = false;
        itemGuideSize = 1.0;
        itemGuideSelectedSize = 1.0;
        itemGuideColor = new RGBA(0x3300ffffL);
        itemGuideSelectedColor = new RGBA(0x3300ff00L);
        rangedGrabColor = new RGBA(0xff00ffffL);
        disableVanillaInteractionsForSupportedImmersives = false;
        reachBehindBagMode = ReachBehindBackpackMode.BEHIND_BACK;
    }
}
