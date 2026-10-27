package com.servet.saucedemo.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.servet.saucedemo.config.Config;

/**
 * Tek bir ExtentReports (tüm çalıştırma için) + her thread için kendi ExtentTest kaydı.
 * Paralel testlerde iki thread aynı ExtentTest'e yazarsa adımlar yanlış teste düşer;
 * ThreadLocal bunu önler (Hafta 8'deki DriverManager ile aynı fikir).
 */
final class ExtentManager {

    static final String REPORT_PATH = "target/extent-report/index.html";

    private static final ExtentReports EXTENT = create();
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    private ExtentManager() {
    }

    private static ExtentReports create() {
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setDocumentTitle("SauceDemo UI Tests");
        spark.config().setReportName("Selenium 4 + TestNG — saucedemo.com");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setTimeStampFormat("dd.MM.yyyy HH:mm:ss");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Base URL", Config.baseUrl());
        extent.setSystemInfo("Default browser", Config.browser());
        extent.setSystemInfo("Headless", String.valueOf(Config.headless()));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        return extent;
    }

    static synchronized ExtentTest start(String name, String description) {
        ExtentTest test = EXTENT.createTest(name, description);
        CURRENT.set(test);
        return test;
    }

    static ExtentTest current() {
        return CURRENT.get();
    }

    static void end() {
        CURRENT.remove();
    }

    static synchronized void flush() {
        EXTENT.flush();
    }
}
