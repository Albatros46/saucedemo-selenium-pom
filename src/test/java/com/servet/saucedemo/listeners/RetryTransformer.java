package com.servet.saucedemo.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Tüm @Test'lere RetryAnalyzer'ı otomatik ekler; her teste tek tek
 * @Test(retryAnalyzer = RetryAnalyzer.class) yazmaya gerek kalmaz.
 *
 * Annotation transformer'lar @Listeners ile değil, testng.xml'deki <listeners> ile kaydedilmelidir.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
