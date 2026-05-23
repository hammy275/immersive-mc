package com.hammy275.immersivemc.common.util;

import com.hammy275.immersivemc.server.storage.world.ImmersiveMCLevelStorage;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;

public class CommonMixinProxy {

    /** ImmersiveMC 1.6.0 Alpha 3 for 26.1.x used the 'minecraft' namespace. */
    private static final Set<Identifier> V_1_6_0_ALPHA3 = Set.of(
            ImmersiveMCPlayerStorages.V1_6_0_ALPHA3, ImmersiveMCLevelStorage.V1_6_0_ALPHA3
    );

    @Nullable
    public static Path getSavedDataOldPath(Identifier id, Path dataFolder, Operation<Path> original) {
        if (id.equals(ImmersiveMCPlayerStorages.MC1_21_11_BELOW)) {
            // worldFolder\dimensions\minecraft\overworld\data\immersivemc_(player_)data.dat becomes
            // worldFolder\data\immersivemc_player_data.data
            return dataFolder.getParent().getParent().getParent().getParent().resolve("data").resolve("immersivemc_player_data.dat");
        } else if (id.equals(ImmersiveMCLevelStorage.MC1_21_11_BELOW)) {
            // Depends on the dimension
            if (dataFolder.endsWith(Path.of("overworld", "data"))) {
                // If the overworld:
                // worldFolder\dimensions\minecraft\overworld\data\immersivemc_data.dat becomes
                // worldFolder\data\immersivemc_data.dat
                return dataFolder.getParent().getParent().getParent().getParent().resolve("data").resolve("immersivemc_data.dat");
            } else {
                // Otherwise:
                // worldFolder\dimensions\minecraft\(the_nether/the_end)\data\immersivemc_data.dat becomes
                // worldFolder\DIM(-1)\data\immersivemc_data.dat
                String dim = dataFolder.endsWith(Path.of("the_nether", "data")) ? "DIM-1" : "DIM1";
                return dataFolder.getParent().getParent().getParent().getParent().resolve(dim).resolve("data").resolve("immersivemc_data.dat");
            }
        } else if (V_1_6_0_ALPHA3.contains(id)) {
          // Just replace the namespace, that's all!
          return original.call(Util.mcId(id.getPath().substring(0, id.getPath().length() - "_v1_6_0_alpha3".length())));
        } else {
            return original.call(id);
        }
    }

}
