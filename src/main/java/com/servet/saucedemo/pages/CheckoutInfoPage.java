package com.servet.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Ödeme adım 1: ad, soyad, posta kodu. */
public class CheckoutInfoPage extends BasePage {

    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL_CODE = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public CheckoutInfoPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutOverviewPage continueWith(String firstName, String lastName, String postalCode) {
        fill(firstName, lastName, postalCode);
        return new CheckoutOverviewPage(driver);
    }

    public CheckoutInfoPage continueExpectingError(String firstName, String lastName, String postalCode) {
        fill(firstName, lastName, postalCode);
        return this;
    }

    public String errorMessage() {
        return text(ERROR);
    }

    private void fill(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        click(CONTINUE);
    }
}
