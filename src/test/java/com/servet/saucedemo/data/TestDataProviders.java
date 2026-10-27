package com.servet.saucedemo.data;

import java.util.Iterator;
import java.util.Map;

import org.testng.annotations.DataProvider;

/**
 * Üç veri kaynağı, üç DataProvider. parallel = true → her satır ayrı bir thread'de çalışabilir
 * (Hafta 8'deki ThreadLocal DriverManager sayesinde güvenli).
 */
public final class TestDataProviders {

    public static final String LOGIN_EXCEL = "/testdata/login-data.xlsx";
    public static final String CHECKOUT_JSON = "/testdata/checkout-data.json";

    private TestDataProviders() {
    }

    /** Excel: "run" sütunu Y olan satırlar → (case, username, password, expected) */
    @DataProvider(name = "loginFromExcel", parallel = true)
    public static Iterator<Object[]> loginFromExcel() {
        return ExcelReader.readSheet(LOGIN_EXCEL, "login").stream()
                .filter(row -> "Y".equalsIgnoreCase(row.get("run").trim()))
                .map(TestDataProviders::loginRow)
                .iterator();
    }

    /** JSON: her sipariş bir test → (sipariş adı, sipariş) */
    @DataProvider(name = "checkoutFromJson", parallel = true)
    public static Iterator<Object[]> checkoutFromJson() {
        return JsonReader.read(CHECKOUT_JSON, CheckoutData.class).orders().stream()
                .map(order -> new Object[] { order.name(), order })
                .iterator();
    }

    /** Veritabanı: aktif kullanıcılar → (kullanıcı) */
    @DataProvider(name = "usersFromDb", parallel = true)
    public static Iterator<Object[]> usersFromDb() {
        return DbReader.activeUsers().stream()
                .map(user -> new Object[] { user })
                .iterator();
    }

    private static Object[] loginRow(Map<String, String> row) {
        return new Object[] { row.get("case"), row.get("username"), row.get("password"), row.get("expected") };
    }
}
