package com.servet.saucedemo.tests;

import static com.servet.saucedemo.base.TestUsers.PASSWORD;
import static com.servet.saucedemo.base.TestUsers.STANDARD;
import static org.testng.Assert.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.pages.InventoryPage;
import com.servet.saucedemo.pages.InventoryPage.SortOption;

public class InventoryTest extends BaseTest {

    private InventoryPage inventory;

    /** BaseTest'in @BeforeMethod'u (tarayıcıyı açan) ÖNCE çalışır, sonra bu (giriş yapan). */
    @BeforeMethod(alwaysRun = true)
    public void login() {
        inventory = openLoginPage().loginAs(STANDARD, PASSWORD);
    }

    @Test(groups = { "smoke", "regression" })
    public void showsSixProducts() {
        assertEquals(inventory.productNames().size(), 6);
    }

    @Test(groups = "regression")
    public void sortByPriceLowToHigh() {
        List<Double> prices = inventory.sortBy(SortOption.PRICE_LOW_TO_HIGH).productPrices();

        assertEquals(prices, prices.stream().sorted().toList(), "Fiyatlar artan sırada olmalı");
    }

    @Test(groups = "regression")
    public void sortByNameZToA() {
        List<String> names = inventory.sortBy(SortOption.NAME_Z_TO_A).productNames();

        assertEquals(names, names.stream().sorted(Comparator.reverseOrder()).toList(), "İsimler Z→A olmalı");
    }

    @Test(groups = { "smoke", "regression" })
    public void addingProductsUpdatesCartBadge() {
        assertEquals(inventory.cartCount(), 0);

        inventory.addToCart("Sauce Labs Backpack").addToCart("Sauce Labs Bike Light");

        assertEquals(inventory.cartCount(), 2);
    }
}
