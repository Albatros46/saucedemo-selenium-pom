package com.servet.saucedemo.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/** Ürün listesi sayfası ("Products"). */
public class InventoryPage extends BasePage {

    public enum SortOption {
        NAME_A_TO_Z("az"), NAME_Z_TO_A("za"), PRICE_LOW_TO_HIGH("lohi"), PRICE_HIGH_TO_LOW("hilo");

        private final String value;

        SortOption(String value) {
            this.value = value;
        }
    }

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By ITEM_IMAGES = By.cssSelector("img.inventory_item_img");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product-sort-container']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public String title() {
        return text(TITLE);
    }

    public List<String> productNames() {
        return texts(ITEM_NAMES);
    }

    public List<Double> productPrices() {
        return texts(ITEM_PRICES).stream().map(BasePage::parsePrice).toList();
    }

    public List<String> productImageSources() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ITEM_IMAGES));
        return driver.findElements(ITEM_IMAGES).stream().map(img -> img.getAttribute("src")).toList();
    }

    public InventoryPage sortBy(SortOption option) {
        new Select(visible(SORT_DROPDOWN)).selectByValue(option.value);
        return this;
    }

    /** Ürünü ADINA göre bulup sepete ekler; listedeki sırası değişse de test kırılmaz. */
    public InventoryPage addToCart(String productName) {
        click(By.xpath("//div[@data-test='inventory-item'][.//div[@data-test='inventory-item-name' and normalize-space()='"
                + productName + "']]//button[starts-with(@id,'add-to-cart')]"));
        return this;
    }

    public int cartCount() {
        return isDisplayed(CART_BADGE) ? Integer.parseInt(text(CART_BADGE)) : 0;
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver);
    }

    public LoginPage logout() {
        click(MENU_BUTTON);
        // Menü kayarak açılır: link görünür ve tıklanabilir olana kadar bekle.
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        return new LoginPage(driver);
    }
}
