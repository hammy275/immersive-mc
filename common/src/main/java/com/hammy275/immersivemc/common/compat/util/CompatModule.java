package com.hammy275.immersivemc.common.compat.util;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.compat.CompatModuleClient;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.server.ServerSubscriber;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Compatibility module to allow us to easily add mod integration that fails gracefully.
 * @param <T> Object type for compatibility module.
 */
public class CompatModule<T> implements InvocationHandler {

    private final T module;
    private final String friendlyName;
    private final BiConsumer<ActiveConfig, Boolean> configSetter;

    private final Map<String, Method> methods = new HashMap<>();

    private CompatModule(T module, String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter) {
        this.module = module;
        this.friendlyName = friendlyName;
        this.configSetter = configSetter;
        for (Method method : module.getClass().getMethods()) {
            methods.put(method.getName(), method);
        }
    }

    /**
     * Creates a compatibility module.
     * @param module The module to wrap.
     * @param interfaceClass The interface that module implements.
     * @param friendlyName A name to use for errors.
     * @param configSetter A function that handles config setting to disable the compatibility on an error.
     * @return The module wrapped in compatibility.
     * @param <T> Object type for compatibility module.
     */
    @SuppressWarnings("unchecked")
    public static <T extends I, I> I create(T module, Class<I> interfaceClass, String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter) {
        return (T) Proxy.newProxyInstance(CompatModule.class.getClassLoader(),
                new Class[]{interfaceClass},
                new CompatModule<>(module, friendlyName, configSetter));
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
                CompatModuleClient.disableClient(friendlyName, configSetter);
            } else {
                // Definitely running on the server. Disable for all players globally.
                handleDisableServer(friendlyName, configSetter, ServerSubscriber.server);
            }
            // Give some sane return type without throwing.
            Class<?> returnType = method.getReturnType();
            if (returnType.isPrimitive()) {
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

    public static void handleDisableServer(String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter, MinecraftServer server) {
        configSetter.accept(ActiveConfig.FILE_SERVER, false);
        Network.INSTANCE.sendToPlayers(server.getPlayerList().getPlayers(), new ConfigSyncPacket(ActiveConfig.FILE_SERVER, null));
        if (!server.getPlayerList().getPlayers().isEmpty()) {
            server.sendMessage(getErrorMessage(friendlyName), server.getPlayerList().getPlayers().get(0).getUUID());
        }
        server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(getErrorMessage(friendlyName), player.getUUID()));
    }

    public static Component getErrorMessage(String friendlyName) {
        return new TranslatableComponent("message.immersivemc.compat_error", friendlyName);
    }
}
