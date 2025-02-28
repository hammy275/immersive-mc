package com.hammy275.immersivemc.client.immersive_item;

import java.util.ArrayList;
import java.util.List;

public class HandImmersives {

    public static final List<AbstractHandImmersive<?>> HAND_IMMERSIVES = new ArrayList<>();

    public static final WrittenBookImmersive writtenBookImmersive = new WrittenBookImmersive();

    static {
        HAND_IMMERSIVES.add(writtenBookImmersive);
    }
}
