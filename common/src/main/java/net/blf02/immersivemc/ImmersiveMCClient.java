package net.blf02.immersivemc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.blf02.immersivemc.client.config.screen.ConfigScreen;
import net.blf02.immersivemc.client.model.BackpackCraftingModel;
import net.blf02.immersivemc.client.model.BackpackLowDetailModel;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.client.model.Cube1x1;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ImmersiveMCClient {

    public static void init() {
        // Map to a very obscure key, so it has no conflicts for VR users
        ImmersiveMC.SUMMON_BACKPACK = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".backpack",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F23, ImmersiveMC.vrKeyCategory);
        ImmersiveMC.OPEN_SETTINGS = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".config",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, ImmersiveMC.globalKeyCategory);
        KeyMappingRegistry.register(ImmersiveMC.SUMMON_BACKPACK);
        KeyMappingRegistry.register(ImmersiveMC.OPEN_SETTINGS);

        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));

        EntityModelLayerRegistry.register(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        EntityModelLayerRegistry.register(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        EntityModelLayerRegistry.register(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        EntityModelLayerRegistry.register(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);
    }
}
