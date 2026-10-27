package com.servet.saucedemo.listeners;

import java.util.List;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;

/**
 * Allure'da Parameter.Mode.MASKED sadece raporun EKRANINDA "******" gösterir; gerçek değer
 * allure-results/*.json dosyasında kalır (ölçüldü). Bu listener, sonuç diske yazılmadan hemen önce
 * maskelenmiş her değeri (testin ve tüm adımlarının) gerçekten siler.
 *
 * Kayıt: src/test/resources/META-INF/services/io.qameta.allure.listener.TestLifecycleListener
 */
public class MaskedParameterScrubber implements TestLifecycleListener {

    static final String MASK = "******";

    @Override
    public void beforeTestWrite(TestResult result) {
        scrub(result.getParameters());
        scrubSteps(result.getSteps());
    }

    private static void scrubSteps(List<StepResult> steps) {
        for (StepResult step : steps) {
            scrub(step.getParameters());
            scrubSteps(step.getSteps());
        }
    }

    private static void scrub(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (parameter.getMode() == Parameter.Mode.MASKED) {
                parameter.setValue(MASK);
            }
        }
    }
}
