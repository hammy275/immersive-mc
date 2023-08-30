package com.hammy275.immersivemc.client.immersive_item;

import java.util.ArrayList;
import java.util.List;

public class ItemImmersives {

    public static final List<AbstractItemImmersive<?>> ITEM_IMMERSIVES = new ArrayList<>();

    public static final WrittenBookImmersive writtenBookImmersive = new WrittenBookImmersive();

    static {
        ITEM_IMMERSIVES.add(writtenBookImmersive);
    }
}
