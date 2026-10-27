package com.servet.saucedemo.tests.datadriven;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.servet.saucedemo.base.BaseTest;
import com.servet.saucedemo.base.TestSecrets;
import com.servet.saucedemo.data.TestDataProviders;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Her Excel satırı bir test. Yeni bir giriş senaryosu = Excel'e yeni bir satır, Java'ya dokunmadan.
 * Excel'de gerçek şifre yok, sadece {{password}}: TestSecrets çalışma anında çözer.
 */
@Epic("SauceDemo shop")
@Feature("Data-driven: Excel")
public class LoginExcelTest extends BaseTest {

    private static final String SUCCESS = "SUCCESS";

    @Test(dataProvider = "loginFromExcel", dataProviderClass = TestDataProviders.class,
            groups = "datadriven", description = "Giriş senaryoları login-data.xlsx dosyasından")
    public void loginFromExcel(String caseName, String username, String password, String expected) {
        String realPassword = TestSecrets.resolve(password);
        if (SUCCESS.equals(expected)) {
            assertEquals(openLoginPage().loginAs(username, realPassword).title(), "Products", caseName);
        } else {
            assertEquals(openLoginPage().loginExpectingError(username, realPassword).errorMessage(), expected, caseName);
        }
    }
}
