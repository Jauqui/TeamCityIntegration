package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TCMethod {
    private final String methodName;

    private List<TCTest> tests;


    public TCMethod(String methodName) {
        tests = new ArrayList<>();
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<TCTest> getTests() {
        return tests;
    }

    public int size() {
        return tests.size();
    }

    public void addTest(TCTest tcTest) {
        tests.add(tcTest);
    }

    public void addTest(String testParameters, String testStackTrace, LocalDateTime startTime, TCStatus status) {
        for (TCTest tcTest : tests) {
            if (tcTest.getParameters().equals(testParameters)) {
                tcTest.addTestResult(testStackTrace, startTime, status);
                return;
            }
        }

        TCTest tcTest = new TCTest(testParameters);
        tcTest.addTestResult(testStackTrace, startTime, status);
        tests.add(tcTest);
    }

    public boolean equals(TCMethod tcMethod) {
        return this.methodName.equals(tcMethod.getMethodName());
    }

    public void setTests(List<TCTest> tests) {
        this.tests = tests;
    }

    /*public List<TCTestResult> filterTests(String parameters) {
        Predicate<TCTestResult> predicate = tcTest -> tcTest.getParameters().equals(parameters);
        return tests.stream().filter(predicate).collect(Collectors.toList());
    }

    /*public TCMethod filterTestsByRunTime(LocalDateTime localDateTime) {
        Predicate<TCTestResult> predicate = tcTest -> tcTest.getStartDateTime().equals(localDateTime);
        TCMethod tcMethod = new TCMethod(this.getMethodName());
        tcMethod.setTests(tests.stream().filter(predicate).collect(Collectors.toList()));
        return tcMethod;
    }*/

    public HashSet<LocalDateTime> getRunTimes() {
        HashSet<LocalDateTime> dateTimes = new HashSet<>();
        for (TCTest tcTest : tests) {
            HashSet<LocalDateTime> tcTestStartDateTime = tcTest.getRunTimes();
            dateTimes.addAll(tcTestStartDateTime);
        }

        return dateTimes;
    }

    public TCMethod filterTests(String parameters) {
        TCMethod tcMethod = new TCMethod(methodName);
        for (TCTest tcTest : tests) {
            if (tcTest.getParameters().equals(parameters)) {
                tcMethod.addTest(tcTest);
                return tcMethod;
            }
        }

        return tcMethod;
    }
}
