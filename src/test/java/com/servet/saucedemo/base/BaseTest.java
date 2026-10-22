package com.servet.saucedemo.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.servet.saucedemo.config.Config;
import com.servet.saucedemo.driver.DriverFactory;
import com.servet.saucedemo.pages.LoginPage;

/**
 * Her test kendi temiz tarayıcısıyla başlar ve tarayıcı test bitince kapanır.
 * Böylece bir testin bıraktığı çerez veya sepet, sonraki testi etkilemez.
 *
 * Not: 'driver' sıradan bir alan. Testleri paralel çalıştırmak için Hafta 8'de ThreadLocal'a taşıyacağız.
 */
public abstract class BaseTest {

    protected WebDriver driver;

    /** Tarayıcı önceliği: testng.xml parametresi → -Dbrowser → config.properties */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void startBrowser(@Optional String browser) {
        String chosen = (browser == null || browser.isBlank()) ? Config.browser() : browser;
        driver = DriverFactory.create(chosen, Config.headless());
    }

    @AfterMethod(alwaysRun = true)
    public void quitBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected LoginPage openLoginPage() {
        return new LoginPage(driver).open();
    }
}
