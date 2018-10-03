package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

public class TCTest {
    private final String testParameters;
    private final HashMap<LocalDateTime, TCTestResult> results;

    public TCTest(String testParameters) {
        this.testParameters = testParameters;
        results = new HashMap<>();
    }

    public void addTestResult(String stackTrace, LocalDateTime startDateTime, TCStatus status) {
        TCTestResult tcTestResult = new TCTestResult(stackTrace, status);
        results.put(startDateTime, tcTestResult);
    }

    public String getParameters() {
        return testParameters;
    }

    public boolean equals(TCTest tcTest) {
        return this.testParameters.equals(tcTest.getParameters());
    }

    public HashSet<LocalDateTime> getRunTimes() {
        HashSet<LocalDateTime> runTimes = new HashSet<>();
        runTimes.addAll(results.keySet());

        return runTimes;
    }

    public HashMap<LocalDateTime, TCTestResult> getResults() {
        return results;
    }

    public boolean containsTime(LocalDateTime time) {
        return results.containsKey(time);
    }

    public TCTestResult getResultForTime(LocalDateTime time) {
        return results.get(time);
    }
}
