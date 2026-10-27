package com.servet.saucedemo.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import io.qameta.allure.Step;

/** Ödeme adım 2: özet, ara toplam, vergi, toplam. */
public class CheckoutOverviewPage extends BasePage {

    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SUBTOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL = By.cssSelector("[data-test='total-label']");
    private static final By FINISH = By.id("finish");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
    }

    public List<Double> itemPrices() {
        return texts(ITEM_PRICES).stream().map(BasePage::parsePrice).toList();
    }

    public double itemTotal() {
        return parsePrice(text(SUBTOTAL));
    }

    public double tax() {
        return parsePrice(text(TAX));
    }

    public double total() {
        return parsePrice(text(TOTAL));
    }

    @Step("Finish order")
    public CheckoutCompletePage finish() {
        click(FINISH);
        return new CheckoutCompletePage(driver);
    }
}
