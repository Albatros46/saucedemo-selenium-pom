package com.servet.saucedemo.driver;

import org.openqa.selenium.WebDriver;

/**
 * Her thread'e KENDİ tarayıcısını verir. Paralel testlerin temel taşı.
 *
 * ThreadLocal: aynı değişken, her thread için ayrı bir değer tutar. 4 thread aynı anda
 * DriverManager.get() çağırsa bile her biri yalnızca kendi açtığı tarayıcıyı görür.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void start(String browser, boolean headless) {
        if (DRIVER.get() != null) {
            throw new IllegalStateException("Bu thread'de zaten açık bir tarayıcı var: " + threadName());
        }
        DRIVER.set(DriverFactory.create(browser, headless));
    }

    public static WebDriver get() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Bu thread için tarayıcı başlatılmadı: " + threadName());
        }
        return driver;
    }

    public static boolean isStarted() {
        return DRIVER.get() != null;
    }

    /** Tarayıcıyı kapatır ve ThreadLocal'ı temizler. remove() unutulursa thread havuzunda eski driver kalır. */
    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }
}
