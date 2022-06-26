package net.blf02.immersivemc.client.immersive;

import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;

import java.util.LinkedList;
import java.util.List;

public class Immersives {

    public static final List<AbstractImmersive<? extends AbstractImmersiveInfo>> IMMERSIVES =
            new LinkedList<>();

    public static final List<AbstractWorldStorageImmersive<? extends AbstractWorldStorageInfo>> WS_IMMERSIVES =
            new LinkedList<>();
}
