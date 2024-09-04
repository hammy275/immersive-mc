package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.RGBA;
import net.minecraft.util.Mth;

public final class ClientActiveConfig extends ActiveConfig {

    public static final ClientActiveConfig DISABLED = new ClientActiveConfig();

    public boolean crouchBypassImmersion = false;
    public boolean doRumble = true;
    public boolean returnItems = true;
    public boolean disableOutsideVR = false;
    public int backpackColor = 11901820;
    public boolean rightClickChest = false;
    public boolean autoCenterFurnace = false;
    public boolean autoCenterBrewing = false;
    public BackpackMode backpackMode = BackpackMode.BUNDLE;
    public PlacementGuideMode placementGuideMode = PlacementGuideMode.CUBE;
    public PlacementMode placementMode = PlacementMode.PLACE_ONE;
    public boolean spinCraftingOutput = true;
    public boolean rightClickInVR = false;
    public boolean resourcePack3dCompat = false;
    public double itemGuideSize = 1.0;
    public double itemGuideSelectedSize = 1.0;
    public RGBA itemGuideColor = new RGBA(0x3300ffffL);
    public RGBA itemGuideSelectedColor = new RGBA(0x3300ff00L);
    public RGBA rangedGrabColor = new RGBA(0xff00ffffL);
    public boolean disableVanillaGUIs = false;
    public ReachBehindBackpackMode reachBehindBackpackMode = ReachBehindBackpackMode.BEHIND_BACK;

    static {
        DISABLED.setDisabled();
    }

    @Override
    public void validateConfig() {
        super.validateConfig();
        // NOTE: RGBAs are validated during deserialization, so no need to cover that here. Only need to null check.
        backpackColor = Mth.clamp(backpackColor, 0, 0xFFFFFF);
        backpackMode = firstEnumIfNull(backpackMode, BackpackMode.class);
        placementGuideMode = firstEnumIfNull(placementGuideMode, PlacementGuideMode.class);
        placementMode = firstEnumIfNull(placementMode, PlacementMode.class);
        itemGuideSize = Mth.clamp(itemGuideSize, 0, 1);
        itemGuideSelectedSize = Mth.clamp(itemGuideSelectedSize, 0, 1);
        itemGuideColor = defaultIfNull(itemGuideColor, new RGBA(0x3300ffffL));
        itemGuideSelectedColor = defaultIfNull(itemGuideSelectedColor, new RGBA(0x3300ff00L));
        rangedGrabColor = defaultIfNull(rangedGrabColor, new RGBA(0xff00ffffL));
        reachBehindBackpackMode = firstEnumIfNull(reachBehindBackpackMode, ReachBehindBackpackMode.class);
    }

    @Override
    public void setDisabled() {
        super.setDisabled();
        crouchBypassImmersion = false;
        doRumble = false;
        returnItems = false;
        disableOutsideVR = false;
        backpackColor = 11901820;
        rightClickChest = false;
        autoCenterFurnace = false;
        autoCenterBrewing = false;
        backpackMode = BackpackMode.BUNDLE;
        placementGuideMode = PlacementGuideMode.CUBE;
        placementMode = PlacementMode.PLACE_ONE;
        spinCraftingOutput = true;
        rightClickInVR = false;
        resourcePack3dCompat = false;
        itemGuideSize = 1.0;
        itemGuideSelectedSize = 1.0;
        itemGuideColor = new RGBA(0x3300ffffL);
        itemGuideSelectedColor = new RGBA(0x3300ff00L);
        rangedGrabColor = new RGBA(0xff00ffffL);
        disableVanillaGUIs = false;
        reachBehindBackpackMode = ReachBehindBackpackMode.BEHIND_BACK;
    }
}
