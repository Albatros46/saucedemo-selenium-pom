package com.servet.saucedemo.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.servet.saucedemo.config.Config;

/**
 * Başarısız bir testi en fazla 'retryCount' kez tekrar dener (config.properties, varsayılan 1).
 *
 * DİKKAT: Retry, geçici sorunlar (ağ yavaşlığı, yavaş yüklenen sayfa) için bir emniyet kemeridir.
 * Gerçek bir hatayı gizlememeli: tekrar denemede geçen her test raporda "skipped + passed" olarak
 * görünür ve araştırılmalıdır.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = Config.retryCount();
        if (attempts < maxRetries) {
            attempts++;
            System.out.printf("[%s] ↻ RETRY   %s.%s (%d/%d)%n",
                    Thread.currentThread().getName(),
                    result.getTestClass().getRealClass().getSimpleName(),
                    result.getMethod().getMethodName(), attempts, maxRetries);
            return true;
        }
        return false;
    }
}
