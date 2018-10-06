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

    public void addTest(String parameters, String stackTrace, LocalDateTime startDateTime, TCStatus status) {
        for (TCTest tcTest : tests) {
            if (tcTest.getParameters().equals(parameters)) {
                tcTest.addTestRun(startDateTime, status, stackTrace);
                return;
            }
        }
        TCTest tcTest = new TCTest(parameters);
        tcTest.addTestRun(startDateTime, status, stackTrace);
        tests.add(tcTest);
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

    public boolean equals(TCMethod tcMethod) {
        return this.methodName.equals(tcMethod.getMethodName());
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

    public void addTCTests(TCMethod resultMethod) {
        for (TCTest resultTest : resultMethod.getTests())
            addTCTest(resultTest);
    }

    public void addTCTest(TCTest resultTest) {
        for (TCTest tcTest : tests) {
            if (tcTest.getParameters().equals(resultTest.getParameters())) {
                tcTest.addTCTest(resultTest);
                return;
            }
        }
        tests.add(resultTest);
    }

    public int getTestRunsSize(LocalDateTime startDateTime, TCStatus tcStatus) {
        return tests.stream().mapToInt(tcTest -> tcTest.getTestRunsSize(startDateTime, tcStatus)).sum();
    }
}
