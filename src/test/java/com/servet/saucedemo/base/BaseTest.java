package com.servet.saucedemo.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.servet.saucedemo.config.Config;
import com.servet.saucedemo.driver.DriverManager;
import com.servet.saucedemo.pages.LoginPage;

/**
 * Her test kendi temiz tarayıcısıyla başlar ve tarayıcı test bitince kapanır.
 *
 * Tarayıcı bir alanda DEĞİL, DriverManager'daki ThreadLocal'da tutulur. TestNG bir sınıfın tüm
 * testleri için tek bir nesne kullandığından, alanda tutulan bir driver paralel testlerde
 * thread'ler arasında paylaşılır ve testler birbirinin tarayıcısını kullanır.
 */
public abstract class BaseTest {

    /** Tarayıcı önceliği: testng.xml parametresi → -Dbrowser → config.properties */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void startBrowser(@Optional String browser) {
        String chosen = (browser == null || browser.isBlank()) ? Config.browser() : browser;
        DriverManager.start(chosen, Config.headless());
    }

    @AfterMethod(alwaysRun = true)
    public void quitBrowser() {
        DriverManager.quit();
    }

    protected WebDriver driver() {
        return DriverManager.get();
    }

    protected LoginPage openLoginPage() {
        return new LoginPage(driver()).open();
    }
}
