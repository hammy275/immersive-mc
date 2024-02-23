package com.hammy275.immersivemc.common.immersive.handler;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler> HANDLERS = new ArrayList<>();


    public static final ImmersiveHandler anvilHandler = new AnvilHandler();
    public static final ImmersiveHandler barrelHandler = new BarrelHandler();
    public static final ImmersiveHandler beaconHandler = new BeaconHandler();
    public static final ImmersiveHandler brewingStandHandler = new BrewingStandHandler();
    public static final ImmersiveHandler chiseledBookshelfHandler = new ChiseledBookshelfHandler();
    public static final ImmersiveHandler craftingHandler = new CraftingHandler();
    public static final ImmersiveHandler enchantingTableHandler = new ETableHandler();
    public static final ImmersiveHandler furnaceHandler = new FurnaceHandler();
    public static final ImmersiveHandler hopperHandler = new HopperHandler();
    public static final ImmersiveHandler jukeboxHandler = new JukeboxHandler();
    public static final ImmersiveHandler shulkerBoxHandler = new ShulkerBoxHandler();
    public static final ImmersiveHandler smithingTableHandler = new SmithingTableHandler();

    static {
        HANDLERS.add(anvilHandler);
        HANDLERS.add(barrelHandler);
        HANDLERS.add(beaconHandler);
        HANDLERS.add(brewingStandHandler);
        HANDLERS.add(chiseledBookshelfHandler);
        HANDLERS.add(craftingHandler);
        HANDLERS.add(enchantingTableHandler);
        HANDLERS.add(furnaceHandler);
        HANDLERS.add(hopperHandler);
        HANDLERS.add(jukeboxHandler);
        HANDLERS.add(shulkerBoxHandler);
        HANDLERS.add(smithingTableHandler);
    }
}
