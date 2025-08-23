package com.hammy275.immersivemc.test;

import com.hammy275.immersivemc.test.tests.ChestTest;
import com.hammy275.immersivemc.test.tests.CraftingTest;
import com.hammy275.immersivemc.test.tests.ETableTest;
import com.hammy275.immersivemc.test.tests.FurnaceTest;
import com.hammy275.immersivemc.test.tests.TestTest;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Tests {

    private static final String SETUP_NAME = "setup";
    private static final String TEARDOWN_NAME = "teardown";
    private static final List<String> RESERVED_NAMES = List.of(SETUP_NAME, TEARDOWN_NAME);
    private static final String BAD_METHOD_MESSAGE = "Method does not take the correct test arguments and/or does not return a String type!";
    private static final List<Test> TESTS = List.of(
            new TestTest(), new FurnaceTest(), new ChestTest(), new CraftingTest(), new ETableTest()
    );
    private static final Logger log = LoggerFactory.getLogger(Tests.class);

    private final ServerPlayer player;

    private int passed = 0;
    private int failed = 0;

    public Tests(ServerPlayer player) {
        this.player = player;
    }

    public void runTests() {
        for (Test test : TESTS) {
            List<Method> methods = Arrays.stream(test.getClass().getDeclaredMethods())
                    .filter(method -> method.canAccess(test) && method.getName().startsWith("test"))
                    .toList();
            player.sendSystemMessage(Component.literal(test.getClass().getSimpleName()).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE));
            for (Method method : methods) {
                if (!RESERVED_NAMES.contains(method.getName())) {
                    String message = null;
                    boolean loggedResult = false;

                    // Run setup
                    try {
                        test.setup(player);
                    } catch (Exception e) {
                        logTestResult(method, new TestResult("Threw an exception during setup!", e));
                        continue;
                    }

                    // Run main test
                    try {
                        if (method.getReturnType() != String.class && method.getGenericReturnType() != Void.TYPE) {
                            // Not really the right exception type to throw, but gets caught below and handled correctly
                            throw new IllegalAccessException("Invalid return type!");
                        }
                        Object result = method.invoke(test, player);
                        if (result instanceof String str) {
                            message = str;
                        }
                    } catch (IllegalAccessException e) {
                        logTestResult(method, new TestResult(BAD_METHOD_MESSAGE, null));
                        loggedResult = true;
                    } catch (InvocationTargetException e) {
                        logTestResult(method, new TestResult(e.getCause().getMessage(), (Exception) e.getCause()));
                        loggedResult = true;
                    } catch (Exception e) {
                        logTestResult(method, new TestResult("Threw an exception!", e));
                        loggedResult = true;
                    }

                    // Run teardown, making sure to only ever log a test once
                    try {
                        test.teardown(player);
                    } catch (Exception e) {
                        if (!loggedResult) {
                            logTestResult(method, new TestResult("Threw an exception during teardown!", e));
                            loggedResult = true;
                        }
                    }
                    if (!loggedResult) {
                        logTestResult(method, new TestResult(message, null));
                    }
                }
            }
        }
        if (passed == 0 && failed == 0) {
            player.sendSystemMessage(Component.literal("[!] No tests ran!").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD));
        } else if (failed == 0) {
            player.sendSystemMessage(Component.literal("[✓] All tests passed!").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
        } else {
            player.sendSystemMessage(Component.literal("[✘] %d tests passed and %d tests failed!".formatted(passed, failed)).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        }
    }

    private void logTestResult(Method testMethod, TestResult result) {
        ChatFormatting style = result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED;
        player.sendSystemMessage(Component.literal(result.makeOutputMessage(testMethod.getName())).withStyle(style));
        if (result.passed()) {
            passed++;
        } else {
            failed++;
        }
    }
}
