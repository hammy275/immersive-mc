package com.hammy275.immersivemc.common.config;

import dev.architectury.platform.Platform;

import java.io.File;
import java.nio.file.Paths;

public enum ConfigType {
    CLIENT(Paths.get(Platform.getConfigFolder().toString(), "immersive_mc-client.json").toFile(), ClientActiveConfig.class),
    SERVER(Paths.get(Platform.getConfigFolder().toString(), "immersive_mc-server.json").toFile(), ActiveConfig.class);

    public final File configFile;
    public final Class<? extends ActiveConfig> configClass;

    ConfigType(File configFile, Class<? extends ActiveConfig> configClass) {
        this.configFile = configFile;
        this.configClass = configClass;
    }
}
