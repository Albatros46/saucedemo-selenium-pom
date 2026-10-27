package com.servet.saucedemo.base;

/**
 * Test verisi dosyalarında (Excel, veritabanı) gerçek şifre yazmaz, sadece {{password}} yer tutucusu.
 * Değer test çalışırken çözülür: önce SAUCE_PASSWORD ortam değişkeni (CI'da secret olarak verilir),
 * yoksa saucedemo'nun herkese açık demo şifresi.
 *
 * Böylece şifre ne veri dosyasına, ne DataProvider parametresine, ne de herhangi bir rapora girer.
 */
public final class TestSecrets {

    public static final String PASSWORD_PLACEHOLDER = "{{password}}";

    private TestSecrets() {
    }

    public static String resolve(String value) {
        if (!PASSWORD_PLACEHOLDER.equals(value)) {
            return value; // ör. "wrong_password" veya "" — bunlar sır değil, test verisi
        }
        String fromEnv = System.getenv("SAUCE_PASSWORD");
        return fromEnv != null && !fromEnv.isBlank() ? fromEnv : TestUsers.PASSWORD;
    }
}
