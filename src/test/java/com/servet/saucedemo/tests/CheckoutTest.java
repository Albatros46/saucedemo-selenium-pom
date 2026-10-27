package com.servet.saucedemo.tests;

import static com.servet.saucedemo.base.TestUsers.PASSWORD;
import static com.servet.saucedemo.base.TestUsers.STANDARD;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.pages.CartPage;
import com.servet.saucedemo.pages.CheckoutInfoPage;
import com.servet.saucedemo.pages.CheckoutOverviewPage;

@Epic("SauceDemo shop")
@Feature("Checkout")
public class CheckoutTest extends BaseTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    @Severity(SeverityLevel.BLOCKER)
    @Test(groups = { "smoke", "regression" }, description = "Uçtan uca: giriş → sepet → ödeme → onay")
    public void completePurchase() {
        CartPage cart = openLoginPage()
                .loginAs(STANDARD, PASSWORD)
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT)
                .openCart();
        assertEquals(cart.itemNames(), List.of(BACKPACK, BIKE_LIGHT));

        CheckoutOverviewPage overview = cart.checkout().continueWith("Servet", "Akcadag", "10115");

        // Sayfadaki hesaplamayı doğrula: ara toplam = ürün fiyatları toplamı, toplam = ara toplam + vergi
        double sumOfPrices = overview.itemPrices().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(overview.itemTotal(), sumOfPrices, 0.001, "Ara toplam");
        assertEquals(overview.total(), overview.itemTotal() + overview.tax(), 0.001, "Toplam");

        assertEquals(overview.finish().header(), "Thank you for your order!");
    }

    @Test(groups = "regression", description = "Ad boş bırakılırsa ödeme devam etmez")
    public void checkoutRequiresFirstName() {
        CheckoutInfoPage info = openLoginPage()
                .loginAs(STANDARD, PASSWORD)
                .addToCart(BACKPACK)
                .openCart()
                .checkout()
                .continueExpectingError("", "Akcadag", "10115");

        assertEquals(info.errorMessage(), "Error: First Name is required");
    }
}
