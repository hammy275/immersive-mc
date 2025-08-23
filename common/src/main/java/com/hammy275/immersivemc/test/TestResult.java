package com.hammy275.immersivemc.test;

import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public record TestResult(String resultMessage, @Nullable Exception exception) {

    public String makeOutputMessage(String name) {
        if (this.passed()) {
            return "[✓] %s: Test passed!".formatted(name);
        } else if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String stacktraceString = sw.toString();
            if (exception instanceof AssertionFailure) {
                stacktraceString = stacktraceString.split("\n")[2];
            }
            return "[✘] %s: Test failed! %s\n%s".formatted(name, resultMessage, stacktraceString.replace("\r", "").replace("\t", "    "));
        } else {
            return "[✘] %s: Test failed! %s".formatted(name, resultMessage);
        }
    }

    public boolean passed() {
        return resultMessage == null || resultMessage.isEmpty();
    }
}
