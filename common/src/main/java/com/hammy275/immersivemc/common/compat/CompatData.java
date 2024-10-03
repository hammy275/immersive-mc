package com.hammy275.immersivemc.common.compat;

import com.hammy275.immersivemc.common.config.ActiveConfig;

import java.util.function.BiConsumer;

/**
 * Contains data for {@link com.hammy275.immersivemc.common.compat.util.CompatModule}s.
 * @param friendlyName A name to use for errors.
 * @param configSetter A function that handles config setting to disable the compatibility on an error.
 */
public record CompatData(String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter) {
}
