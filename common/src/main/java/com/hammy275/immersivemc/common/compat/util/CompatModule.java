package com.hammy275.immersivemc.common.compat.util;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.compat.CompatModuleClient;
import com.hammy275.immersivemc.common.compat.CompatData;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.server.ServerSubscriber;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Compatibility module to allow us to easily add mod integration that fails gracefully.
 * @param <T> Object type for compatibility module.
 */
public class CompatModule<T> implements InvocationHandler {

    private final T module;
    private final CompatData compatData;

    private final Map<String, Method> methods = new HashMap<>();

    private CompatModule(T module, CompatData compatData) {
        this.module = module;
        this.compatData = compatData;
        for (Method method : module.getClass().getMethods()) {
            methods.put(method.getName(), method);
        }
    }

    /**
     * Creates a compatibility module.
     * @param module The module to wrap.
     * @param interfaceClass The interface that module implements.
     * @param compatData Compatibility data.
     * @return The module wrapped in compatibility.
     * @param <T> Object type for compatibility module.
     */
    @SuppressWarnings("unchecked")
    public static <T extends I, I> I create(T module, Class<I> interfaceClass, CompatData compatData) {
        return (T) Proxy.newProxyInstance(CompatModule.class.getClassLoader(),
                new Class[]{interfaceClass},
                new CompatModule<>(module, compatData));
    }

    /**
     * Creates a compatibility module for an {@link ImmersiveHandler}.
     * @param handler ImmersiveHandler to wrap.
     * @param compatData Compatibility data.
     * @return The ImmersiveHandler wrapped in compatibility.
     * @param <S> The NetworkStorage type of the ImmersiveHandler.
     */
    @SuppressWarnings("unchecked")
    public static <S extends NetworkStorage> ImmersiveHandler<S> create(ImmersiveHandler<S> handler, CompatData compatData) {
        return create(handler, ImmersiveHandler.class, compatData);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        method = methods.get(method.getName());
        try {
            return method.invoke(module, args);
        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println(sw);
            if (Platform.isClient()) {
                // Running on the client. Could be singleplayer/LAN host or could be on a multiplayer server.
                CompatModuleClient.disableClient(compatData);
            } else {
                // Definitely running on the server. Disable for all players globally.
                handleDisableServer(compatData, ServerSubscriber.server);
            }
            // Give some sane return type without throwing.
            Class<?> returnType = method.getReturnType();
            if (returnType == Void.TYPE) {
                return null;
            } else if (returnType.isPrimitive()) {
                // Get the "default" primitive value (false for boolean, etc.)
                return Array.get(Array.newInstance(returnType, 1), 0);
            } else if (returnType.isEnum()) {
                // Functions that accept enums tend to not like non-null values
                return returnType.getEnumConstants()[0];
            } else if (returnType == String.class) {
                // Strings are common. Let's give an empty string instead of null here.
                return "";
            } else {
                // Handled enough cases. Should assume that we can handle null
                return null;
            }
        }
    }

    public static void handleDisableServer(CompatData compatData, MinecraftServer server) {
        compatData.configSetter().accept(ActiveConfig.FILE_SERVER, false);
        ActiveConfig.remergeAllConfigs();
        server.getPlayerList().getPlayers().forEach(ConfigSyncPacket::syncConfigToPlayer);
        server.sendSystemMessage(getErrorMessage(compatData.friendlyName()));
        server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(getErrorMessage(compatData.friendlyName())));
    }

    public static Component getErrorMessage(String friendlyName) {
        return Component.translatable("message.immersivemc.compat_error", friendlyName);
    }
}
