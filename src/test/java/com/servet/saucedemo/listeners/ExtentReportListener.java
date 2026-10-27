package com.servet.saucedemo.listeners;

import java.util.Base64;

import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.servet.saucedemo.config.Config;

/** Her TestNG olayını ExtentReports'a yazar. Rapor: target/extent-report/index.html */
public class ExtentReportListener implements ITestListener, IExecutionListener {

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = ExtentManager.start(name(result), result.getMethod().getDescription());
        test.assignCategory(result.getMethod().getGroups());
        test.assignDevice(browser(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        current(result).pass("Passed in " + duration(result) + " ms");
        ExtentManager.end();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = current(result);
        test.fail(result.getThrowable());
        byte[] png = Screenshots.capture();
        if (png != null) {
            test.fail("Screenshot on failure", MediaEntityBuilder
                    .createScreenCaptureFromBase64String(Base64.getEncoder().encodeToString(png)).build());
        }
        ExtentManager.end();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = current(result);
        if (result.wasRetried()) {
            test.assignCategory("retried");
            test.skip("Failed, retried by RetryAnalyzer: " + message(result));
        } else {
            test.skip("Skipped: " + message(result));
        }
        ExtentManager.end();
    }

    @Override
    public void onExecutionFinish() {
        ExtentManager.flush();
        System.out.println("Extent report: " + ExtentManager.REPORT_PATH);
    }

    /** @BeforeMethod patladığında onTestStart hiç çağrılmaz; o durumda kaydı burada açarız. */
    private static ExtentTest current(ITestResult result) {
        ExtentTest test = ExtentManager.current();
        return test != null ? test : ExtentManager.start(name(result), result.getMethod().getDescription());
    }

    private static String name(ITestResult result) {
        String name = result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
        // DataProvider'larda ilk parametre okunabilir senaryo adıdır (case / sipariş / kullanıcı).
        // Tüm parametreleri yazmak, bir gün şifre gibi bir değeri rapora sızdırabilir.
        return result.getParameters().length == 0 ? name : name + " [" + result.getParameters()[0] + "]";
    }

    private static String browser(ITestResult result) {
        String fromXml = result.getTestContext().getCurrentXmlTest().getParameter("browser");
        return fromXml != null ? fromXml : Config.browser();
    }

    private static String message(ITestResult result) {
        return result.getThrowable() == null ? "-" : result.getThrowable().getMessage();
    }

    private static long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
