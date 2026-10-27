package com.servet.saucedemo.tests;

import static com.servet.saucedemo.base.TestUsers.LOCKED_OUT;
import static com.servet.saucedemo.base.TestUsers.PASSWORD;
import static com.servet.saucedemo.base.TestUsers.PERFORMANCE_GLITCH;
import static com.servet.saucedemo.base.TestUsers.STANDARD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.pages.InventoryPage;
import com.servet.saucedemo.pages.LoginPage;

@Epic("SauceDemo shop")
@Feature("Login")
public class LoginTest extends BaseTest {

    @Severity(SeverityLevel.BLOCKER)
    @Test(groups = { "smoke", "regression" }, description = "Geçerli kullanıcı ürün sayfasına ulaşır")
    public void validLoginOpensProductsPage() {
        InventoryPage inventory = openLoginPage().loginAs(STANDARD, PASSWORD);

        assertEquals(inventory.title(), "Products");
    }

    /**
     * Bu kullanıcıda giriş ~5 sn sürer. Gecikme tıklama sırasında olur: sitenin JavaScript'i tarayıcıyı
     * meşgul eder ve Selenium'un click() komutu bu iş bitene kadar dönmez. Test bu yüzden bekleme süresi
     * 0 olsa da geçer (ölçüldü). Bu test, girişin YAVAŞ ama ÇALIŞIR olduğunu belgeler.
     */
    @Test(groups = "regression", description = "Yavaş kullanıcı da giriş yapabilir")
    public void slowUserCanStillLogin() {
        InventoryPage inventory = openLoginPage().loginAs(PERFORMANCE_GLITCH, PASSWORD);

        assertEquals(inventory.title(), "Products");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = "regression", description = "Kilitli kullanıcı giriş yapamaz")
    public void lockedOutUserSeesError() {
        LoginPage login = openLoginPage().loginExpectingError(LOCKED_OUT, PASSWORD);

        assertEquals(login.errorMessage(), "Epic sadface: Sorry, this user has been locked out.");
    }

    @DataProvider(name = "invalidLogins")
    public Object[][] invalidLogins() {
        return new Object[][] {
                { STANDARD, "wrong_password", "Username and password do not match any user in this service" },
                { "unknown_user", PASSWORD, "Username and password do not match any user in this service" },
                { "", PASSWORD, "Username is required" },
                { STANDARD, "", "Password is required" }
        };
    }

    @Test(dataProvider = "invalidLogins", groups = "regression", description = "Hatalı girişte doğru mesaj")
    public void invalidLoginShowsError(String username, String password, String expectedMessage) {
        LoginPage login = openLoginPage().loginExpectingError(username, password);

        assertTrue(login.errorMessage().contains(expectedMessage),
                "Beklenen: '" + expectedMessage + "', gelen: '" + login.errorMessage() + "'");
    }

    @Test(groups = "regression", description = "Çıkış yapınca giriş sayfasına dönülür")
    public void logoutReturnsToLoginPage() {
        LoginPage login = openLoginPage().loginAs(STANDARD, PASSWORD).logout();

        assertTrue(login.isLoaded(), "Giriş sayfası görünmeli");
    }
}
