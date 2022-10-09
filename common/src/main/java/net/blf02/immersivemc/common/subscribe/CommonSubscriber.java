package net.blf02.immersivemc.common.subscribe;

import net.blf02.immersivemc.common.util.ShieldUtil;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.mixin.ProjectileMixin;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommonSubscriber {

    public static Map<UUID, Integer> reflected = new HashMap<>();

    public static void globalTick(Object ignored) {
        // Note: Unsure if this is run double on the hosts of singleplayer worlds.
        // At least for now, it only removes from reflected, which is ok to do twice, even if not ideal.
        Map<UUID, Integer> newMap = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : reflected.entrySet()) {
            int val = entry.getValue() - 1;
            if (val > 0) {
                newMap.put(entry.getKey(), val);
            }
        }
        reflected = newMap;
    }

    public static void onPlayerTick(Player player) {
        if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)) {
            for (InteractionHand iHand : InteractionHand.values()) {
                IVRData hand = VRPlugin.API.getVRPlayer(player).getController(iHand.ordinal());
                AABB shieldBox = ShieldUtil.getShieldHitbox(hand, iHand);
                List<Entity> ents = player.level.getEntities(player, shieldBox);
                for (Entity e : ents) {
                    if (e instanceof Projectile proj) {
                        if (!reflected.containsKey(proj.getUUID())) {
                            reflected.put(proj.getUUID(), 100);
                            // "Hit" player ahead of time. We should reflect it from immersive shield, though!
                            // Note that this will effectively have things that bypass shields hurt us "early",
                            // but I can't seem to work around that, since we need a damage source, and we don't
                            // know our damage source until we're hit.
                            ((ProjectileMixin) proj).onHit(new EntityHitResult(player));
                            proj.setPos(ShieldUtil.getShieldPos(hand, iHand));
                        }
                    }
                }
            }
        }
    }
}
