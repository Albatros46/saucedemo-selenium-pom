package com.servet.saucedemo.listeners;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.servet.saucedemo.driver.DriverManager;

/** O anki thread'in tarayıcısından ekran görüntüsü alır. Listener'lar asla exception fırlatmamalı. */
final class Screenshots {

    private Screenshots() {
    }

    /** @return PNG baytları; tarayıcı yoksa veya alınamazsa null */
    static byte[] capture() {
        if (!DriverManager.isStarted()) {
            return null;
        }
        try {
            return ((TakesScreenshot) DriverManager.get()).getScreenshotAs(OutputType.BYTES);
        } catch (RuntimeException e) {
            System.out.println("[" + Thread.currentThread().getName() + "] Screenshot failed: " + e.getMessage());
            return null;
        }
    }
}
