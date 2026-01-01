package com.hammy275.immersivemc.common.config;

import net.minecraft.util.Mth;

public final class ClientActiveConfig extends ActiveConfig {

    public static final ClientActiveConfig DISABLED = new ClientActiveConfig();
    public static final ClientActiveConfig DEFAULT = new ClientActiveConfig();

    public CrouchMode crouchMode = CrouchMode.SWAP_ALL;
    public boolean doVRControllerRumble = true;
    public boolean returnItemsWhenLeavingImmersives = true;
    public boolean disableImmersiveMCOutsideVR = false;
    public int bagColor = 11901820;
    public boolean rightClickChestInteractions = false;
    public boolean autoCenterFurnaceImmersive = false;
    public boolean autoCenterBrewingStandImmersive = false;
    public BackpackMode bagMode = BackpackMode.BUNDLE;
    public PlacementGuideMode placementGuideMode = PlacementGuideMode.CUBE;
    public boolean spinSomeImmersiveOutputs = true;
    public boolean rightClickImmersiveInteractionsInVR = false;
    public boolean compatFor3dResourcePacks = false;
    public double itemGuideSize = 1.0;
    public double itemGuideSelectedSize = 1.0;
    public transient ItemGuidePreset itemGuidePreset = ItemGuidePreset.GRAY;
    public transient ItemGuidePreset.PrideFlag itemGuidePrideFlag = ItemGuidePreset.PrideFlag.PRIDE;
    public transient int colorPresetAlpha = 0x7F;
    public transient int colorPresetSelectedAlpha = 0xCF;
    public transient int colorPresetRangedGrabSize = 0xFF;
    public transient int multiColorPresetTransitionTimeMS = 5000;
    public transient ItemGuideColorData itemGuideCustomColorData = ItemGuideColorData.DEFAULT_CUSTOM;
    public boolean disableVanillaInteractionsForSupportedImmersives = false;
    public ReachBehindBackpackMode reachBehindBagMode = ReachBehindBackpackMode.BEHIND_BACK;
    public float textScale = 1f;
    public boolean swapBagHand = false;
    public boolean useGrabBeaconInVR = true;
    public boolean useGrindMotionGrindstoneInVR = true;
    public boolean dontAutoStepOnImmersiveBlocksInVR = true;
    public boolean requireTriggerForBagOpen = true;

    public int clientConfigVersion = 4;
    public static final String CLIENT_CONFIG_VERSION = "clientConfigVersion";

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
        itemGuideSize = Mth.clamp(itemGuideSize, 0, 1);
        itemGuideSelectedSize = Mth.clamp(itemGuideSelectedSize, 0, 1);
        reachBehindBagMode = firstEnumIfNull(reachBehindBagMode, ReachBehindBackpackMode.class);
        textScale = Mth.clamp(textScale, 0.5f, 2f);
        itemGuidePreset = firstEnumIfNull(itemGuidePreset, ItemGuidePreset.class);
        itemGuidePrideFlag = firstEnumIfNull(itemGuidePrideFlag, ItemGuidePreset.PrideFlag.class);
        itemGuideCustomColorData = defaultIfNull(itemGuideCustomColorData, ItemGuideColorData.DEFAULT_CUSTOM);
        multiColorPresetTransitionTimeMS = Mth.clamp(multiColorPresetTransitionTimeMS, 500, 60000);
        colorPresetAlpha = Mth.clamp(colorPresetAlpha, 0, 0xFF);
        colorPresetSelectedAlpha = Mth.clamp(colorPresetSelectedAlpha, 0, 0xFF);
        colorPresetRangedGrabSize = Mth.clamp(colorPresetRangedGrabSize, 0, 0xFF);
    }

    @Override
    public void setDisabled() {
        super.setDisabled();
        crouchMode = CrouchMode.NONE;
        doVRControllerRumble = false;
        returnItemsWhenLeavingImmersives = false;
        disableImmersiveMCOutsideVR = false;
        bagColor = 11901820;
        rightClickChestInteractions = false;
        autoCenterFurnaceImmersive = false;
        autoCenterBrewingStandImmersive = false;
        bagMode = BackpackMode.BUNDLE;
        placementGuideMode = PlacementGuideMode.CUBE;
        spinSomeImmersiveOutputs = true;
        rightClickImmersiveInteractionsInVR = false;
        compatFor3dResourcePacks = false;
        itemGuideSize = 1.0;
        itemGuideSelectedSize = 1.0;
        reachBehindBagMode = ReachBehindBackpackMode.BEHIND_BACK;
        textScale = 1f;
        swapBagHand = false;
        dontAutoStepOnImmersiveBlocksInVR = false;
        disableVanillaInteractionsForSupportedImmersives = false;
        requireTriggerForBagOpen = true;
    }


}
