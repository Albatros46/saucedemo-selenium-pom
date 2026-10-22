package com.servet.saucedemo.driver;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.servet.saucedemo.config.Config;

/**
 * Tarayıcıyı oluşturur. Driver dosyası (chromedriver.exe vb.) indirmeye gerek yok:
 * Selenium 4.6+ ile gelen Selenium Manager doğru sürümü otomatik bulur.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create(String browser, boolean headless) {
        WebDriver driver = switch (browser.toLowerCase()) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> throw new IllegalArgumentException("Desteklenmeyen tarayıcı: " + browser);
        };
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.pageLoadTimeoutSeconds()));
        // Implicit wait BİLEREK 0: explicit wait ile karıştırılırsa bekleme süreleri öngörülemez olur.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        // saucedemo'nun şifresi herkese açık olduğu için Chrome "şifren sızdırıldı" penceresi açar; kapatıyoruz.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }
}
