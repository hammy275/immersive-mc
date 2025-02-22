package com.hammy275.immersivemc.common.config;

import com.hammy275.immersivemc.common.util.MemoizedSupplier;
import com.hammy275.immersivemc.common.util.RGBA;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public enum ItemGuidePreset {
    GRAY(new ItemGuideColorData(new MemoizedSupplier<>(List.of(new RGBA(0x638b8b8b))), new MemoizedSupplier<>(List.of(new RGBA(0x7fc5c5c5L))), new MemoizedSupplier<>(List.of(new RGBA(0x7fc5c5c5L))), () -> ClientActiveConfig.FILE_CLIENT.colorPresetRangedGrabSize)),
    CLASSIC(new ItemGuideColorData(new MemoizedSupplier<>(List.of(new RGBA(0x3300ffffL))), new MemoizedSupplier<>(List.of(new RGBA(0x3300ff00L))), new MemoizedSupplier<>(List.of(new RGBA(0x00ffff))), () -> ClientActiveConfig.FILE_CLIENT.colorPresetRangedGrabSize)),
    PRIDE_FLAG(() -> ClientActiveConfig.FILE_CLIENT.itemGuidePrideFlag.multiColorPresetHolder.colorData),
    CUSTOM(() -> ClientActiveConfig.FILE_CLIENT.itemGuideCustomColorData);

    public final Supplier<ItemGuideColorData> colorData;

    ItemGuidePreset(ItemGuideColorData colorData) {
        this(() -> colorData);
    }

    ItemGuidePreset(Supplier<ItemGuideColorData> colorData) {
        this.colorData = colorData;
    }

    public boolean showTransitionConfig() {
        return this == PRIDE_FLAG || this == CUSTOM;
    }

    public boolean isCustomizablePreset() {
        return this == PRIDE_FLAG;
    }

    /**
     * Holds data for multi-color presets to not constantly create lists every time colors are retrieved.
     */
    public static class MultiColorPresetHolder {

        private final List<RGBA> colors;
        private final List<RGBA> selectedColors;
        private final List<RGBA> rangedGrabColors;

        public final ItemGuideColorData colorData;

        private int lastAlpha = -1;
        private int lastSelectedAlpha = -1;
        private int lastRangedGrabAlpha = -1;

        public MultiColorPresetHolder(List<RGBA> colors) {
            this.colors = new ArrayList<>(colors);
            this.selectedColors = new ArrayList<>(colors);
            this.rangedGrabColors = new ArrayList<>(colors);
            this.colorData = new ItemGuideColorData(
                    () -> {
                        int newAlpha = ActiveConfig.FILE_CLIENT.colorPresetAlpha;
                        if (lastAlpha != newAlpha) {
                            this.colors.replaceAll(color -> new RGBA(color.getRGB(), newAlpha));
                            lastAlpha = newAlpha;
                        }
                        return this.colors;
                    },
                    () -> {
                        int newAlpha = ActiveConfig.FILE_CLIENT.colorPresetSelectedAlpha;
                        if (lastSelectedAlpha != newAlpha) {
                            this.selectedColors.replaceAll(color -> new RGBA(color.getRGB(), newAlpha));
                            lastSelectedAlpha = newAlpha;
                        }
                        return this.selectedColors;
                    },
                    () -> {
                        int newAlpha = ActiveConfig.FILE_CLIENT.colorPresetRangedGrabSize;
                        if (lastRangedGrabAlpha != newAlpha) {
                            this.rangedGrabColors.replaceAll(color -> new RGBA(color.getRGB(), newAlpha));
                            lastRangedGrabAlpha = newAlpha;
                        }
                        return this.rangedGrabColors;
                    },
                    () -> ActiveConfig.FILE_CLIENT.multiColorPresetTransitionTimeMS
            );
        }
    }

    public enum PrideFlag {
        PRIDE(List.of(0xE40303, 0xFF8C00, 0xFFED00, 0x008026, 0x24408E, 0x732982)),
        ASEXUAL(List.of(0x000000, 0xA3A3A3, 0xFFFFFF, 0x800080)),
        BISEXUAL(List.of(0xD60270, 0xD60270, 0x9B4F96, 0x0038A8, 0x0038A8)), // Colors repeated to match proportions of the flag
        GAY(List.of(0x078D70, 0x26CEAA, 0x98E8C1, 0xFFFFFF, 0x7BADE2, 0x5049CC, 0x3D1A78)),
        LESBIAN(List.of(0xD52D00, 0xEF7627, 0xFF9A56, 0xFFFFFF, 0xD162A4, 0xB55690, 0xA30262)),
        NONBINARY(List.of(0xFCF434, 0xFFFFFF, 0x9C59D1, 0x2C2C2C)),
        PANSEXUAL(List.of(0xFF218C, 0xFFD800, 0x21B1FF)),
        TRANSGENDER(List.of(0x5BCEFA, 0xF5A9B8, 0xFFFFFF, 0xF5A9B8, 0x5BCEFA));

        public final MultiColorPresetHolder multiColorPresetHolder;

        PrideFlag(List<Integer> colors) {
            this.multiColorPresetHolder = new MultiColorPresetHolder(colors.stream().map(RGBA::new).toList());
        }
    }
}
