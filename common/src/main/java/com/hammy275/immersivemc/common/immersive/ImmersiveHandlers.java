package com.hammy275.immersivemc.common.immersive;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler> HANDLERS = new ArrayList<>();


    public static final ImmersiveHandler anvilHandler = new AnvilHandler();
    public static final ImmersiveHandler brewingStandHandler = new BrewingStandHandler();
    public static final ImmersiveHandler furnaceHandler = new FurnaceHandler();

    static {
        HANDLERS.add(anvilHandler);
        HANDLERS.add(brewingStandHandler);
        HANDLERS.add(furnaceHandler);
    }
}
