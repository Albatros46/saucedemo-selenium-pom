package com.servet.saucedemo.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.servet.saucedemo.config.Config;

/**
 * Tüm sayfaların ortak davranışı. Her etkileşim önce elementi BEKLER, sonra kullanır.
 * Thread.sleep() yok: sabit beklemeler testleri yavaşlatır ve yine de flaky bırakır.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.explicitWaitSeconds()));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String text(By locator) {
        return visible(locator).getText();
    }

    protected List<String> texts(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator).stream().map(WebElement::getText).toList();
    }

    protected boolean isDisplayed(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    /** Fiyat metnini sayıya çevirir: "$29.99" veya "Item total: $39.98" → 29.99 / 39.98 */
    protected static double parsePrice(String text) {
        return Double.parseDouble(text.substring(text.indexOf('$') + 1).trim());
    }
}
