package com.hammy275.immersivemc.common.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hammy275.immersivemc.common.util.MemoizedSupplier;
import com.hammy275.immersivemc.common.util.RGBA;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Contains the data for item guide coloring.
 * @param colors Supplier of a list of colors to transition between for item guides.
 * @param selectedColors Supplier of a list of colors to transition between for selected item guides.
 * @param rangedGrabColors Supplier of a list of colors to transition between for ranged grab particles.
 * @param transitionTimeMS Supplier of the time to transition between colors in the list in milliseconds. Should be a constant time!
 */
public record ItemGuideColorData(Supplier<List<RGBA>> colors, Supplier<List<RGBA>> selectedColors, Supplier<List<RGBA>> rangedGrabColors, Supplier<Integer> transitionTimeMS) {

    public static final ItemGuideColorData DEFAULT_CUSTOM = new ItemGuideColorData(
            new MemoizedSupplier<>(() -> ItemGuidePreset.GRAY.colorData.get().colors.get()),
            new MemoizedSupplier<>(() -> ItemGuidePreset.GRAY.colorData.get().selectedColors.get()),
            new MemoizedSupplier<>(() -> ItemGuidePreset.GRAY.colorData.get().rangedGrabColors.get()),
            () -> 5000
    );

    public static ItemGuideColorData of(Supplier<RGBA> color, Supplier<RGBA> selectedColor, Supplier<RGBA> rangedGrabColor) {
        return new ItemGuideColorData(() -> List.of(color.get()), () -> List.of(selectedColor.get()), () -> List.of(rangedGrabColor.get()), () -> 0);
    }

    public static ItemGuideColorData of(List<RGBA> colors, List<RGBA> selectedColors, List<RGBA> rangedGrabColors, int transitionTimeMS) {
        return new ItemGuideColorData(() -> colors, () -> selectedColors, () -> rangedGrabColors, () -> transitionTimeMS);
    }

    public ItemGuideColorData withChangedTransitionTime(int newTransitionTimeMS) {
        return new ItemGuideColorData(colors, selectedColors, rangedGrabColors, () -> newTransitionTimeMS);
    }

    public static class GsonHandler implements JsonSerializer<ItemGuideColorData>, JsonDeserializer<ItemGuideColorData> {

        // Expects suppliers to return constant values.
        @Override
        public JsonElement serialize(ItemGuideColorData src, Type typeOfSrc, JsonSerializationContext context) {
            List<RGBA> colors = src.colors.get();
            List<RGBA> selectedColors = src.selectedColors.get();
            List<RGBA> rangedGrabColors = src.rangedGrabColors.get();

            JsonObject json = new JsonObject();
            JsonArray colorsArray = new JsonArray(colors.size());
            JsonArray selectedColorsArray = new JsonArray(selectedColors.size());
            JsonArray rangedGrabColorsArray = new JsonArray(rangedGrabColors.size());

            colors.forEach(col -> colorsArray.add(context.serialize(col)));
            selectedColors.forEach(col -> selectedColorsArray.add(context.serialize(col)));
            rangedGrabColors.forEach(col -> rangedGrabColorsArray.add(context.serialize(col)));

            json.add("colors", colorsArray);
            json.add("selectedColors", selectedColorsArray);
            json.add("rangedGrabColors", rangedGrabColorsArray);
            json.addProperty("transitionTimeMS", src.transitionTimeMS.get());
            return json;
        }

        @Override
        public ItemGuideColorData deserialize(JsonElement jsonElem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = jsonElem.getAsJsonObject();
            JsonArray colorsArray = json.getAsJsonArray("colors");
            JsonArray selectedColorsArray = json.getAsJsonArray("selectedColors");
            JsonArray rangedGrabColorsArray = json.getAsJsonArray("rangedGrabColors");

            List<RGBA> colors = new ArrayList<>();
            List<RGBA> selectedColors = new ArrayList<>();
            List<RGBA> rangedGrabColors = new ArrayList<>();

            colorsArray.forEach(colElem -> colors.add(context.deserialize(colElem, RGBA.class)));
            selectedColorsArray.forEach(colElem -> selectedColors.add(context.deserialize(colElem, RGBA.class)));
            rangedGrabColorsArray.forEach(colElem -> rangedGrabColors.add(context.deserialize(colElem, RGBA.class)));
            int transitionTimeMS = json.get("transitionTimeMS").getAsInt();

            return ItemGuideColorData.of(colors, selectedColors, rangedGrabColors, transitionTimeMS);
        }
    }
}
