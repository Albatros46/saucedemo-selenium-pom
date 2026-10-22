package com.servet.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutCompletePage extends BasePage {

    private static final By HEADER = By.cssSelector("[data-test='complete-header']");

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
    }

    public String header() {
        return text(HEADER);
    }
}
