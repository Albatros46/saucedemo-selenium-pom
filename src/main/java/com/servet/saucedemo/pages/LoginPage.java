package com.servet.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.servet.saucedemo.config.Config;

public class LoginPage extends BasePage {

    // Locator'lar SADECE sayfa sınıfında durur. Site değişirse tek bir yer güncellenir.
    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.baseUrl());
        visible(LOGIN_BUTTON);
        return this;
    }

    /** Başarılı giriş → bir sonraki sayfayı döndürür (fluent page object). */
    public InventoryPage loginAs(String username, String password) {
        submit(username, password);
        return new InventoryPage(driver);
    }

    /** Başarısız giriş → aynı sayfada kalınır. */
    public LoginPage loginExpectingError(String username, String password) {
        submit(username, password);
        return this;
    }

    public String errorMessage() {
        return text(ERROR);
    }

    public boolean isLoaded() {
        return isDisplayed(LOGIN_BUTTON);
    }

    private void submit(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }
}
