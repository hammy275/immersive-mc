package net.blf02.immersivemc.client.config;


import net.blf02.immersivemc.client.config.screen.ConfigScreen;
import net.blf02.immersivemc.client.model.BackpackCraftingModel;
import net.blf02.immersivemc.client.model.BackpackLowDetailModel;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoadingContext;

public class ClientInit {
    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }

    public static void registerLayers(EntityRenderersEvent .RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        event.registerLayerDefinition(BackpackLowDetailModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
    }
}
