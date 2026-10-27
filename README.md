# SauceDemo · Selenium 4 + TestNG + Page Object Model

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.51-43B02A) ![TestNG](https://img.shields.io/badge/TestNG-7.12-red) ![Browsers](https://img.shields.io/badge/Chrome%20%7C%20Edge%20%7C%20Firefox-tested-blue) ![Tests](https://img.shields.io/badge/tests-14%20passing-brightgreen)

UI test automation for [saucedemo.com](https://www.saucedemo.com) with a clean Page Object Model, explicit waits only, and cross-browser runs from one `testng.xml`.
Part of my 52-week QA Automation roadmap — **Week 6** (Page Object Model), **Week 7** (listeners, retry, screenshots) and **Week 8** (thread-safe parallel execution).

## Architecture

```
src/main/java/com/servet/saucedemo/      FRAMEWORK
├── config/Config.java                   config.properties, overridable with -D
├── driver/DriverFactory.java            Chrome / Edge / Firefox, headless, start retry
├── driver/DriverManager.java            ThreadLocal<WebDriver>: one browser per thread
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
| `mvn test -Dsuite=parallel` | all 14 tests in 4 threads at the same time |
| `mvn test -Dsuite=cross-browser` | smoke tests in Chrome, Edge and Firefox at the same time |
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

## Parallel execution (Week 8)

### 1 · Red first: the Week 6 code run in parallel

I ran the existing tests with `parallel="methods"` and 4 threads before changing anything (retry off):

| Run | Result | Example failure |
|---|---|---|
| 1 | 6 failures, 5 skipped | `lockedOutUserSeesError` received **another test's** error message |
| 2 | 5 failures, 4 skipped | `sortByPriceLowToHigh`: *"Error communicating with the remote browser. It may have died."* |

Different tests failed in each run — the signature of shared state between threads.

### 2 · Two sources of shared state, two fixes

TestNG creates **one instance per test class**, so every instance field is shared by all threads running that class's methods.

| Shared state | Fix |
|---|---|
| `protected WebDriver driver` field in `BaseTest` | `DriverManager` with `ThreadLocal<WebDriver>` — each thread gets its own browser; `quit()` also calls `remove()` |
| `private InventoryPage inventory` field set in `@BeforeMethod` | removed; each test gets its page from a local `loggedIn()` call |

The screenshot listener uses `DriverManager.get()`: TestNG calls `onTestFailure` on the same thread as the failed test, so it captures the right browser.

### 3 · Green: 14/14 in 4 threads, in every run

| Measurement | Sequential (`testng.xml`) | Parallel, 4 threads |
|---|---|---|
| Quiet machine | 37.5 s · 38.9 s | 23.6 s · 24.7 s (**~37 % faster**) |
| Busy machine (many browser windows open) | 63.1 s · 74.5 s | 61.2 s · 40.4 s |

Not 4× faster: every thread starts its own browser, the CPU is shared, and the longest test (end-to-end checkout) sets a lower bound. On a busy machine the gain shrinks or disappears.

### 4 · Cross-browser at the same time — and a Firefox start problem

`cross-browser.xml` now runs Chrome, Edge and Firefox in parallel (`parallel="tests"`). In 3 of the first 4 runs Firefox failed to start:

```
SessionNotCreated: Could not start a new session ... Process unexpectedly closed with status 0
```

It never happened in sequential runs. Because it fails in `@BeforeMethod`, test retry cannot help, so `DriverFactory` now retries **only the browser start** (up to 3 attempts) and logs each attempt:

```
WARN firefox could not start (attempt 1/3): Could not start a new session ...
```

Honest status: the retry path is verified with an injected failure (attempt 1 fails, attempt 2 starts Firefox, test passes). The real Firefox failure has **not reproduced** in the 9 cross-browser runs since, so I cannot yet show the fix catching it in the wild.

## A finding: where the "slow user" is actually slow

I expected `performance_glitch_user` to need the explicit wait. Measurements said otherwise:

| Explicit wait | `performance_glitch_user` | `standard_user` |
|---|---|---|
| 0 s | ✅ 9.0 s | ✅ 4.1 s |
| 10 s | ✅ 9.0 s | ✅ 4.2 s |

The 5-second delay happens **inside the login click**: the site's JavaScript keeps the browser busy, and Selenium's `click()` returns only when that work is done. By then the page is ready. Lesson: measure before explaining a flaky or slow test.

## What I learned
- _TODO: 2–3 sentences in your own words_
