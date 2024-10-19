package com.hammy275.immersivemc.common.config;


import com.hammy275.immersivemc.Platform;

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

    /**
     * @return The default config for this ConfigType.
     */
    public ActiveConfig getDefaultConfig() {
        try {
            return configClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Whether this ConfigType is used on the current Platform side (client or dedicated server)
     */
    public boolean neededOnSide() {
        return Platform.isClient() || this == SERVER;
    }
}
