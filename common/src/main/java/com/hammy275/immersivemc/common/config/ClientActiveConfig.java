package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.RGBA;
import net.minecraft.network.FriendlyByteBuf;

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

    /**
     * Decodes a buffer into a ClientActiveConfig instance.
     * @param buffer Buffer to decode from.
     */
    public static ClientActiveConfig decode(FriendlyByteBuf buffer) {
        int hashFromBuffer = buffer.readInt();
        if (hashFromBuffer != fieldsHash) {
            // Version mismatch, return disabled clone.
            return (ClientActiveConfig) DISABLED.clone();
        }
        return GSON.fromJson(buffer.readUtf(), ClientActiveConfig.class);
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

    @Override
    public void loadFromFile() {
        super.loadFromFile();
        crouchBypassImmersion = ImmersiveMCConfig.crouchBypassImmersion.get();
        doRumble = ImmersiveMCConfig.doRumble.get();
        returnItems = ImmersiveMCConfig.returnItems.get();
        disableOutsideVR = ImmersiveMCConfig.disableOutsideVR.get();
        backpackColor = ImmersiveMCConfig.backpackColor.get();
        rightClickChest = ImmersiveMCConfig.rightClickChest.get();
        autoCenterFurnace = ImmersiveMCConfig.autoCenterFurnace.get();
        autoCenterBrewing = ImmersiveMCConfig.autoCenterBrewing.get();
        backpackMode = BackpackMode.values()[ImmersiveMCConfig.backpackMode.get()];
        placementGuideMode = PlacementGuideMode.values()[ImmersiveMCConfig.placementGuideMode.get()];
        placementMode = PlacementMode.fromInt(ImmersiveMCConfig.itemPlacementMode.get());
        spinCraftingOutput = ImmersiveMCConfig.spinCraftingOutput.get();
        rightClickInVR = ImmersiveMCConfig.rightClickInVR.get();
        resourcePack3dCompat = ImmersiveMCConfig.resourcePack3dCompat.get();
        itemGuideSize = ImmersiveMCConfig.itemGuideSize.get();
        itemGuideSelectedSize = ImmersiveMCConfig.itemGuideSelectedSize.get();
        itemGuideColor = new RGBA(ImmersiveMCConfig.itemGuideColor.get());
        itemGuideSelectedColor = new RGBA(ImmersiveMCConfig.itemGuideSelectedColor.get());
        rangedGrabColor = new RGBA(ImmersiveMCConfig.rangedGrabColor.get());
        disableVanillaGUIs = ImmersiveMCConfig.disableVanillaGUIs.get();
        reachBehindBackpackMode = ReachBehindBackpackMode.values()[ImmersiveMCConfig.reachBehindBackpackMode.get()];
    }
}
