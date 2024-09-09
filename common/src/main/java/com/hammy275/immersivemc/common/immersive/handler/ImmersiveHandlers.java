package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.CraftingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHandlers {

    public static final List<ImmersiveHandler<?>> HANDLERS = new ArrayList<>();


    public static final WorldStorageHandler<AnvilStorage> anvilHandler = new AnvilHandler();
    public static final ImmersiveHandler<?> barrelHandler = new BarrelHandler();
    public static final WorldStorageHandler<BeaconStorage> beaconHandler = new BeaconHandler();
    public static final ImmersiveHandler<?> brewingStandHandler = new BrewingStandHandler();
    public static final ImmersiveHandler<ListOfItemsStorage> chestHandler = new ChestHandler();
    public static final ImmersiveHandler<?> chiseledBookshelfHandler = new ChiseledBookshelfHandler();
    public static final WorldStorageHandler<CraftingTableStorage> craftingHandler = new CraftingHandler();
    public static final WorldStorageHandler<ETableStorage> enchantingTableHandler = new ETableHandler();
    public static final ImmersiveHandler<?> furnaceHandler = new FurnaceHandler();
    public static final ImmersiveHandler<?> hopperHandler = new HopperHandler();
    public static final ImmersiveHandler<?> ironFurnacesFurnaceHandler = new IronFurnacesFurnaceHandler();
    public static final ImmersiveHandler<?> jukeboxHandler = new JukeboxHandler();
    public static final WorldStorageHandler<BookData> lecternHandler = new LecternHandler();
    public static final ImmersiveHandler<NullStorage> leverHandler = new LeverHandler();
    public static final ImmersiveHandler<NullStorage> repeaterHandler = new RepeaterHandler();
    public static final ImmersiveHandler<?> shulkerBoxHandler = new ShulkerBoxHandler();
    public static final WorldStorageHandler<SmithingTableStorage> smithingTableHandler = new SmithingTableHandler();
    public static final ImmersiveHandler<NullStorage> trapdoorHandler = new TrapdoorHandler();
    public static final ImmersiveHandler<?> tcCraftingStationHandler = new TCCraftingStationHandler();

    static {
        ImmersiveMCRegistrationImpl.doImmersiveRegistration((handler) -> {
            if (!HANDLERS.contains(handler)) {
                HANDLERS.add(handler);
            }
        });
    }
}
