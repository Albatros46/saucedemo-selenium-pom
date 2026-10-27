package com.servet.saucedemo.tests.datadriven;

import static com.servet.saucedemo.base.TestUsers.PASSWORD;
import static com.servet.saucedemo.base.TestUsers.STANDARD;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.base.ReportParameters;
import com.servet.saucedemo.data.CheckoutData.Customer;
import com.servet.saucedemo.data.CheckoutData.Order;
import com.servet.saucedemo.data.TestDataProviders;
import com.servet.saucedemo.pages.CartPage;
import com.servet.saucedemo.pages.CheckoutOverviewPage;
import com.servet.saucedemo.pages.InventoryPage;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Her JSON siparişi bir uçtan uca satın alma. Beklenen fiyat ve toplamlar KODDA değil, JSON'da:
 * sitedeki bir fiyat değişirse test, hangi ürünün hangi siparişte saptığını söyler.
 */
@Epic("SauceDemo shop")
@Feature("Data-driven: JSON")
public class CheckoutJsonTest extends BaseTest {

    @Test(dataProvider = "checkoutFromJson", dataProviderClass = TestDataProviders.class,
            groups = "datadriven", description = "Sipariş senaryoları checkout-data.json dosyasından")
    public void checkoutFromJson(String orderName, Order order) {
        ReportParameters.hide("order");
        InventoryPage inventory = openLoginPage().loginAs(STANDARD, PASSWORD);
        order.productNames().forEach(inventory::addToCart);

        CartPage cart = inventory.openCart();
        assertEquals(cart.itemNames(), order.productNames(), orderName + ": sepetteki ürünler");

        Customer customer = order.customer();
        CheckoutOverviewPage overview = cart.checkout()
                .continueWith(customer.firstName(), customer.lastName(), customer.postalCode());

        assertEquals(overview.itemPrices(), order.productPrices(), orderName + ": ürün fiyatları");
        assertEquals(overview.itemTotal(), order.expected().itemTotal(), 0.001, orderName + ": ara toplam");
        assertEquals(overview.tax(), order.expected().tax(), 0.001, orderName + ": vergi");
        assertEquals(overview.total(), order.expected().total(), 0.001, orderName + ": toplam");
        assertEquals(overview.finish().header(), "Thank you for your order!");
    }
}
