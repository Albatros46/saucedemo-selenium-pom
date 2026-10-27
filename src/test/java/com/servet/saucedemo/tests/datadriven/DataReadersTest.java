package com.servet.saucedemo.tests.datadriven;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.servet.saucedemo.data.CheckoutData;
import com.servet.saucedemo.data.DbReader;
import com.servet.saucedemo.data.DbReader.DbUser;
import com.servet.saucedemo.data.ExcelReader;
import com.servet.saucedemo.data.JsonReader;
import com.servet.saucedemo.data.TestDataProviders;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Tarayıcı açmayan, saniyeden kısa testler: okuyucular doğru mu, ve test verisinin KENDİSİ tutarlı mı?
 * Yanlış veriyle yazılmış bir UI testi, yanlış bir şeyi "doğrular".
 */
@Epic("Test data")
@Feature("Data readers")
public class DataReadersTest {

    @Test(groups = "unit", description = "Excel: 8 satır okunur, boş hücre \"\" olarak gelir")
    public void excelRowsAndBlankCells() {
        List<Map<String, String>> rows = ExcelReader.readSheet(TestDataProviders.LOGIN_EXCEL, "login");

        assertEquals(rows.size(), 8);
        Map<String, String> emptyUsername = rows.stream()
                .filter(r -> r.get("case").equals("empty username")).findFirst().orElseThrow();
        assertEquals(emptyUsername.get("username"), "");
    }

    @Test(groups = "unit", description = "Excel: run = N olan satır DataProvider'a girmez")
    public void excelDisabledRowIsSkipped() {
        int active = 0;
        for (var it = TestDataProviders.loginFromExcel(); it.hasNext(); it.next()) {
            active++;
        }
        assertEquals(active, 7);
    }

    @Test(groups = "unit", description = "JSON: her siparişte beklenen değerler fiyatlarla ve %8 vergiyle tutarlı")
    public void jsonExpectedTotalsAreConsistent() {
        CheckoutData data = JsonReader.read(TestDataProviders.CHECKOUT_JSON, CheckoutData.class);

        assertEquals(data.orders().size(), 3);
        for (CheckoutData.Order order : data.orders()) {
            double sum = order.productPrices().stream().mapToDouble(Double::doubleValue).sum();
            double tax = Math.round(sum * data.taxRate() * 100) / 100.0;
            assertEquals(order.expected().itemTotal(), sum, 0.001, order.name() + " itemTotal");
            assertEquals(order.expected().tax(), tax, 0.001, order.name() + " tax");
            assertEquals(order.expected().total(), sum + tax, 0.001, order.name() + " total");
        }
    }

    @Test(groups = "unit", description = "DB: 6 aktif kullanıcı, pasif kayıt gelmez")
    public void dbReturnsOnlyActiveUsers() {
        List<DbUser> users = DbReader.activeUsers();

        assertEquals(users.size(), 6);
        assertFalse(users.stream().anyMatch(u -> u.username().equals("retired_user")));
        DbUser locked = users.stream().filter(u -> u.username().equals("locked_out_user")).findFirst().orElseThrow();
        assertFalse(locked.canLogin());
        assertEquals(locked.expectedError(), "Epic sadface: Sorry, this user has been locked out.");
        assertNull(users.stream().filter(u -> u.username().equals("standard_user")).findFirst().orElseThrow().expectedError());
    }
}
