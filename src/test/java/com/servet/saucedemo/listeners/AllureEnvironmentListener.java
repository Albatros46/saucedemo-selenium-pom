package com.servet.saucedemo.listeners;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.IExecutionListener;

import com.servet.saucedemo.config.Config;

/** Allure raporunun "Environment" kutusunu doldurur: allure-results/environment.properties */
public class AllureEnvironmentListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        Path dir = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        String content = String.join(System.lineSeparator(),
                "Base.URL=" + Config.baseUrl(),
                "Default.browser=" + Config.browser(),
                "Headless=" + Config.headless(),
                "Retry.count=" + Config.retryCount(),
                "Java=" + System.getProperty("java.version"),
                "OS=" + System.getProperty("os.name"));
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("environment.properties"), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
