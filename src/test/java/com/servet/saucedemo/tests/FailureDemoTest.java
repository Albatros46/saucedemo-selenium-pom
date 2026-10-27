package com.servet.saucedemo.tests;

import static com.servet.saucedemo.base.TestUsers.PASSWORD;
import static com.servet.saucedemo.base.TestUsers.PROBLEM;
import static com.servet.saucedemo.base.TestUsers.STANDARD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.pages.InventoryPage;

/**
 * Listener'ları görmek için yazılmış testler. Normal suite'lerde ÇALIŞMAZ, sadece:
 *   mvn test -Dsuite=demo
 */
@Epic("SauceDemo shop")
@Feature("Listener demo")
public class FailureDemoTest extends BaseTest {

    private static final AtomicInteger FLAKY_ATTEMPTS = new AtomicInteger();

    /** Gerçek bir hata: problem_user tüm ürünlerde aynı resmi görüyor → kırmızı + ekran görüntüsü. */
    @Test(groups = "demo", description = "Her ürünün kendi resmi olmalı")
    public void everyProductHasItsOwnImage() {
        InventoryPage inventory = openLoginPage().loginAs(PROBLEM, PASSWORD);

        List<String> images = inventory.productImageSources();

        assertEquals(new HashSet<>(images).size(), images.size(),
                "Farklı resim sayısı ürün sayısına eşit olmalı. Resimler: " + images);
    }

    /** Flaky test simülasyonu: 1. deneme patlar, RetryAnalyzer tekrar dener, 2. deneme geçer. */
    @Test(groups = "demo", description = "İlk denemede başarısız, tekrar denemede başarılı")
    public void flakyTestPassesOnRetry() {
        InventoryPage inventory = openLoginPage().loginAs(STANDARD, PASSWORD);

        if (FLAKY_ATTEMPTS.incrementAndGet() == 1) {
            fail("Simüle edilmiş geçici hata (deneme 1)");
        }
        assertEquals(inventory.title(), "Products");
    }
}
