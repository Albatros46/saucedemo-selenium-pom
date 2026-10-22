# SauceDemo · Selenium 4 + TestNG + Page Object Model

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.51-43B02A) ![TestNG](https://img.shields.io/badge/TestNG-7.12-red) ![Browsers](https://img.shields.io/badge/Chrome%20%7C%20Edge%20%7C%20Firefox-tested-blue) ![Tests](https://img.shields.io/badge/tests-14%20passing-brightgreen)

UI test automation for [saucedemo.com](https://www.saucedemo.com) with a clean Page Object Model, explicit waits only, and cross-browser runs from one `testng.xml`.
Part of my 52-week QA Automation roadmap — **Week 6**.

## Architecture

```
src/main/java/com/servet/saucedemo/      FRAMEWORK
├── config/Config.java                   config.properties, overridable with -D
├── driver/DriverFactory.java            Chrome / Edge / Firefox, headless
└── pages/                               page objects
    ├── BasePage                         wait → click / type / read
    ├── LoginPage · InventoryPage · CartPage
    └── CheckoutInfoPage · CheckoutOverviewPage · CheckoutCompletePage

src/test/java/com/servet/saucedemo/      TESTS
├── base/BaseTest                        fresh browser per test
└── tests/  LoginTest · InventoryTest · CheckoutTest
```

## Design rules

| Rule | Why |
|---|---|
| Locators live only in page classes | When the UI changes, one place is updated. Tests contain no `By` and no `driver` calls. |
| Fluent page objects | Every action returns the next page: `loginAs()` → `InventoryPage`, `checkout()` → `CheckoutInfoPage`. Calling a step in the wrong order does not compile. |
| Explicit waits only, implicit wait = 0 | Mixing both makes wait times unpredictable. No `Thread.sleep()`. |
| Fresh browser per test | A cart or cookie left by one test cannot break the next one. |
| Products found by name, not by position | `addToCart("Sauce Labs Backpack")` keeps working if the list order changes. |

```java
CheckoutOverviewPage overview = openLoginPage()
        .loginAs(STANDARD, PASSWORD)
        .addToCart(BACKPACK)
        .addToCart(BIKE_LIGHT)
        .openCart()
        .checkout()
        .continueWith("Servet", "Akcadag", "10115");

assertEquals(overview.total(), overview.itemTotal() + overview.tax(), 0.001);
```

## Tests (14)

| Class | Scenarios |
|---|---|
| `LoginTest` | valid login · slow `performance_glitch_user` · locked-out user · 4 invalid logins (`@DataProvider`) · logout |
| `InventoryTest` | 6 products · sort by price low→high · sort by name Z→A · cart badge |
| `CheckoutTest` | end-to-end purchase with subtotal and tax calculation checked · first name required |

## Run

| Command | What runs |
|---|---|
| `mvn test` | all 14 tests in Chrome (window visible) |
| `mvn test -Dsuite=smoke` | 4 critical flows |
| `mvn test -Dsuite=cross-browser` | smoke tests in Chrome, Edge and Firefox |
| `mvn test -Dbrowser=firefox -Dheadless=true` | any browser, headless |

No driver downloads needed: Selenium Manager (Selenium 4.6+) resolves the right driver.
In PowerShell, quote parameters that contain dots or `#`: `mvn test "-Dtest=LoginTest#validLoginOpensProductsPage"`.

## A finding: where the "slow user" is actually slow

I expected `performance_glitch_user` to need the explicit wait. Measurements said otherwise:

| Explicit wait | `performance_glitch_user` | `standard_user` |
|---|---|---|
| 0 s | ✅ 9.0 s | ✅ 4.1 s |
| 10 s | ✅ 9.0 s | ✅ 4.2 s |

The 5-second delay happens **inside the login click**: the site's JavaScript keeps the browser busy, and Selenium's `click()` returns only when that work is done. By then the page is ready. Lesson: measure before explaining a flaky or slow test.

## What I learned
- _TODO: 2–3 sentences in your own words_
