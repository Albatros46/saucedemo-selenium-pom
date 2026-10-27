# SauceDemo · Selenium 4 + TestNG + Page Object Model

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.51-43B02A) ![TestNG](https://img.shields.io/badge/TestNG-7.12-red) ![Browsers](https://img.shields.io/badge/Chrome%20%7C%20Edge%20%7C%20Firefox-tested-blue) ![Allure](https://img.shields.io/badge/report-Allure%202-orange) ![Extent](https://img.shields.io/badge/report-ExtentReports%205-blue) ![Tests](https://img.shields.io/badge/tests-14%20passing-brightgreen)

UI test automation for [saucedemo.com](https://www.saucedemo.com) with a clean Page Object Model, explicit waits only, and cross-browser runs from one `testng.xml`.
Part of my 52-week QA Automation roadmap — **Week 6** (Page Object Model), **Week 7** (listeners, retry, screenshots), **Week 8** (thread-safe parallel execution), **Week 9** (Allure + ExtentReports) and **Week 11** (data-driven tests).

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
| `mvn test -Dsuite=datadriven` | 20 tests driven by Excel, JSON and an H2 database |
| `mvn test -Dsuite=demo` | listener demo: one real bug with screenshot, one flaky test passing on retry (red on purpose) |
| `mvn test -DretryCount=0` | disable retry |
| `mvn allure:report` | Allure HTML report from the last run → `target/site/allure-maven-plugin/index.html` |
| `mvn allure:serve` | build the Allure report and open it in the browser |

No driver downloads needed: Selenium Manager (Selenium 4.6+) resolves the right driver.
In PowerShell, quote parameters that contain dots or `#`: `mvn test "-Dtest=LoginTest#validLoginOpensProductsPage"`.

## Listeners, retry and screenshots (Week 7)

```
src/test/java/com/servet/saucedemo/listeners/
├── TestListener       ITestListener: log every test event, screenshot on failure
├── RetryAnalyzer      IRetryAnalyzer: retry a failed test up to retryCount times (default 1)
├── RetryTransformer   IAnnotationTransformer: attaches RetryAnalyzer to every @Test
├── ExtentReportListener + ExtentManager   ExtentReports, one ExtentTest per thread (Week 9)
├── AllureEnvironmentListener              environment.properties for Allure (Week 9)
└── Screenshots        shared screenshot helper (file + Allure + Extent)
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

## Reports: Allure + ExtentReports (Week 9)

Every run produces two reports — no test class had to change, only page methods got `@Step` and test classes got labels.

| | Allure | ExtentReports |
|---|---|---|
| Output | `target/allure-results` → `mvn allure:report` | `target/extent-report/index.html` (single file) |
| Steps | `@Step` on 11 page actions, recorded by the AspectJ agent | — |
| Grouping | `@Epic` / `@Feature` / `@Severity` | TestNG groups as categories, browser as device |
| Screenshot on failure | attached to the failed test | embedded as Base64 |
| Retries | retried attempts kept as separate *skipped* results | category `retried` |
| Environment | `environment.properties` (URL, browser, Java, OS) | system info |

**What a failed test looks like in Allure** (`mvn test -Dsuite=demo`):

```
Her ürünün kendi resmi olmalı → failed   (feature: Listener demo)
   - Open login page                [passed]
   - Log in as problem_user         [passed]
   attachment: Screenshot on failure (image/png, 327 KB)
```

**Correction (Week 11):** I first wrote here that the password never appears in a step because the step text is `Log in as {username}`. That was wrong — `@Step` also records **every method argument** as a step parameter, so the password was in the Allure results all along. See *Keeping the password out of every report* below for the fix.

**Parallel-safe.** `ExtentManager` keeps one `ExtentTest` per thread (`ThreadLocal`, the same idea as `DriverManager`). In the 4-thread run all 14 tests landed in the right entries, and Allure kept each thread's steps separate.

**Allure also shows the parallel gain:** in one run the tests' durations add up to 78.6 s, but the run took 41.9 s wall time.

Notes:
- Allure Java 2.35.5 is used on purpose; 3.0 is a new major version.
- On JDK 26 the AspectJ weaver prints a `sun.misc.Unsafe` warning. It is harmless: all steps are recorded.

## Data-driven tests: Excel, JSON, database (Week 11)

```
src/test/resources/testdata/
├── login-data.xlsx       7 login cases (+1 row with run = N, skipped)       → Apache POI
├── checkout-data.json    3 orders: products, customer, expected totals      → Jackson
└── users.sql             saucedemo users loaded into an in-memory H2 DB     → JDBC
src/test/java/com/servet/saucedemo/data/
└── ExcelReader · JsonReader · DbReader · TestDataProviders (parallel = true)
```

| Test | Source | Rows | What is checked |
|---|---|---|---|
| `LoginExcelTest` | Excel | 7 | success → "Products", otherwise the exact error message |
| `CheckoutJsonTest` | JSON | 3 | cart items, every item price, subtotal, 8 % tax, total, order confirmation |
| `UsersDbTest` | H2 database | 6 | users who may log in do; the locked user gets the expected message |
| `DataReadersTest` | — | 4 | readers work **and the test data itself is consistent** (no browser, < 1 s) |

A new scenario is a new row — no Java change. Expected values come from the data file, not from the code. `DataReadersTest` checks the data first: a UI test built on wrong data "verifies" the wrong thing.

Result: **20/20 green**, data rows spread over 3 threads.

### Keeping the password out of every report

While checking the Allure results I found the demo password (`secret_sauce`) in 14 result files: as a DataProvider parameter and as a `@Step` parameter of `loginAs`. Masking alone was not enough:

| Attempt | Result |
|---|---|
| `@Param(mode = MASKED)` on the **test** method | ignored by allure-testng |
| `@Param(mode = MASKED)` on the **step** method | the report UI shows `******`, but the **real value stays in the JSON files** |

The fix works at the source, with a second layer behind it:

1. **No secrets in test data.** Excel, the database and `LoginTest`'s DataProvider contain only `{{password}}`. `TestSecrets.resolve()` turns it into the real value at run time — from the `SAUCE_PASSWORD` environment variable (a CI secret) or the public demo password.
2. **`MaskedParameterScrubber`** (an Allure `TestLifecycleListener`) replaces every masked step parameter with `******` *before* the result file is written.
3. Extent names data-driven tests by their first parameter only (`loginFromExcel [locked out user]`).

Verified: after running `testng` (14) and `datadriven` (20), **0 files** under `target/` (Allure, Extent, TestNG and Surefire reports) contain the password.

## A finding: where the "slow user" is actually slow

I expected `performance_glitch_user` to need the explicit wait. Measurements said otherwise:

| Explicit wait | `performance_glitch_user` | `standard_user` |
|---|---|---|
| 0 s | ✅ 9.0 s | ✅ 4.1 s |
| 10 s | ✅ 9.0 s | ✅ 4.2 s |

The 5-second delay happens **inside the login click**: the site's JavaScript keeps the browser busy, and Selenium's `click()` returns only when that work is done. By then the page is ready. Lesson: measure before explaining a flaky or slow test.

## What I learned
- _TODO: 2–3 sentences in your own words_
