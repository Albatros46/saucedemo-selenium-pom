package com.servet.saucedemo.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import io.qameta.allure.Step;

public class CartPage extends BasePage {

    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT = By.id("checkout");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public List<String> itemNames() {
        return texts(ITEM_NAMES);
    }

    @Step("Start checkout")
    public CheckoutInfoPage checkout() {
        click(CHECKOUT);
        return new CheckoutInfoPage(driver);
    }
}
