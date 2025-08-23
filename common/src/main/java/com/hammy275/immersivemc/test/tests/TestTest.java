package com.hammy275.immersivemc.test.tests;

import com.hammy275.immersivemc.test.Test;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Meta test to ensure a good amount of the test framework works
 */
public class TestTest implements Test {

    boolean didSetup = false;

    @Override
    public void setup(ServerPlayer player) {
        didSetup = true;
        player.sendMessage(new TextComponent("Did setup!"), player.getUUID());
    }

    public String testShouldRun(ServerPlayer player) {
        return didSetup ? null : "Did not do setup!";
    }

    private String testShouldNotBeCalledPrivate(ServerPlayer player) {
        return "Tests should not call private methods!";
    }

    protected String testShouldNotBeCalledProtected(ServerPlayer player) {
        return "Tests should not call protected methods!";
    }

    String testShouldNotBeCalledPackagePrivate(ServerPlayer player) {
        return "Tests should not call package-private methods!";
    }

    public void testShouldAlsoRun(ServerPlayer player) {

    }

    @Override
    public void teardown(ServerPlayer player) {
        didSetup = false;
        player.sendMessage(new TextComponent("Did teardown!"), player.getUUID());
    }
}
