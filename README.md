# SauceDemo · Selenium 4 + TestNG + Page Object Model

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.51-43B02A) ![TestNG](https://img.shields.io/badge/TestNG-7.12-red) ![Browsers](https://img.shields.io/badge/Chrome%20%7C%20Edge%20%7C%20Firefox-tested-blue) ![Tests](https://img.shields.io/badge/tests-14%20passing-brightgreen)

UI test automation for [saucedemo.com](https://www.saucedemo.com) with a clean Page Object Model, explicit waits only, and cross-browser runs from one `testng.xml`.
Part of my 52-week QA Automation roadmap — **Week 6** (Page Object Model) and **Week 7** (listeners, retry, screenshots).

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
| `mvn test -Dsuite=demo` | listener demo: one real bug with screenshot, one flaky test passing on retry (red on purpose) |
| `mvn test -DretryCount=0` | disable retry |

No driver downloads needed: Selenium Manager (Selenium 4.6+) resolves the right driver.
In PowerShell, quote parameters that contain dots or `#`: `mvn test "-Dtest=LoginTest#validLoginOpensProductsPage"`.

## Listeners, retry and screenshots (Week 7)

```
src/test/java/com/servet/saucedemo/listeners/
├── TestListener       ITestListener: log every test event, screenshot on failure
├── RetryAnalyzer      IRetryAnalyzer: retry a failed test up to retryCount times (default 1)
└── RetryTransformer   IAnnotationTransformer: attaches RetryAnalyzer to every @Test
```

All three are registered once in each suite's `<listeners>` block — no test class needs to change.

**Screenshot on failure.** `onTestFailure` saves `target/screenshots/<Class>_<method>_<timestamp>.png` and logs the path to the TestNG report. I measured the call order first: TestNG 7.12 calls `onTestFailure` **before** `@AfterMethod`, so the browser is still open when the screenshot is taken.

**Retry.** `retryCount=1` in `config.properties`; disable with `-DretryCount=0`. Retry is a safety belt for temporary problems, not a way to hide bugs: a test that passes only on the second attempt is logged as `RETRY` and should be investigated.

**Demo suite** (intentionally red): `mvn test -Dsuite=demo`

```
▶ START   FailureDemoTest.everyProductHasItsOwnImage
↻ RETRY   FailureDemoTest.everyProductHasItsOwnImage (1/1)
✘ FAIL    FailureDemoTest.everyProductHasItsOwnImage → expected [6] but found [1]
Screenshot: target/screenshots/FailureDemoTest_everyProductHasItsOwnImage_….png
▶ START   FailureDemoTest.flakyTestPassesOnRetry
↻ RETRY   FailureDemoTest.flakyTestPassesOnRetry (1/1)
✔ PASS    FailureDemoTest.flakyTestPassesOnRetry
```

| Demo test | What happens |
|---|---|
| `everyProductHasItsOwnImage` | A real bug in saucedemo: `problem_user` sees the **same image for all 6 products**. Fails twice (retry does not hide a real bug), screenshot proves it. |
| `flakyTestPassesOnRetry` | Simulated flaky test: fails on attempt 1, passes on attempt 2. |

Note: Maven Surefire's summary reports `Skipped: 0` even though TestNG marked the two retried attempts as skipped. The listener log is the place to see retries.

## A finding: where the "slow user" is actually slow

I expected `performance_glitch_user` to need the explicit wait. Measurements said otherwise:

| Explicit wait | `performance_glitch_user` | `standard_user` |
|---|---|---|
| 0 s | ✅ 9.0 s | ✅ 4.1 s |
| 10 s | ✅ 9.0 s | ✅ 4.2 s |

The 5-second delay happens **inside the login click**: the site's JavaScript keeps the browser busy, and Selenium's `click()` returns only when that work is done. By then the page is ready. Lesson: measure before explaining a flaky or slow test.

## What I learned
- _TODO: 2–3 sentences in your own words_
