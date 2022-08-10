package net.blf02.immersivemc.client.config;


import net.blf02.immersivemc.client.config.screen.ConfigScreen;
import net.blf02.immersivemc.client.model.BackpackCraftingModel;
import net.blf02.immersivemc.client.model.BackpackLowDetailModel;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.client.model.Cube1x1;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientInit {
    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::registerLayers);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        event.registerLayerDefinition(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        event.registerLayerDefinition(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        event.registerLayerDefinition(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);
    }
}
