package com.servet.saucedemo.base;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Parameter;

/**
 * Allure'daki bir TEST parametresini rapordan gizler (ör. uzun bir veri nesnesi).
 *
 * allure-testng, DataProvider parametrelerini rapora olduğu gibi yazar ve test metodundaki @Param'ı
 * dikkate almaz (denendi). Bu yüzden test başında, çalışan test kaydını doğrudan güncelliyoruz.
 * Şifreler bu yolla değil, kaynağında korunur: test verisinde sadece {{password}} vardır (TestSecrets).
 */
public final class ReportParameters {

    private ReportParameters() {
    }

    /** Parametre raporda hiç görünmez. */
    public static void hide(String name) {
        setMode(name, Parameter.Mode.HIDDEN);
    }

    private static void setMode(String name, Parameter.Mode mode) {
        Allure.getLifecycle().updateTestCase(testCase -> testCase.getParameters().stream()
                .filter(p -> p.getName().equals(name))
                .forEach(p -> p.setMode(mode)));
    }
}
