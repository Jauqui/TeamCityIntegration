package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.*;

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

    private TCTest getSkipTest() {
        for (TCTest tcTest : tests) {
            if (tcTest.getParameters().equals("")) {
                for (LocalDateTime time : tcTest.getRunTimes()) {
                    if (tcTest.getTestRun(time).getStatus().equals(TCStatus.SKIP))
                        return tcTest;
                }
            }
        }
        return null;
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

    public void mergeTests(HashSet<LocalDateTime> times) {
        int timesSize = times.size();
        int testsSize = getTestsSize();
        int runsSize = getTestRunsSize();
        if (runsSize != timesSize * testsSize) {
            mergeSkipTests(tests);

            runsSize = getTestRunsSize();
            testsSize = getTestsSize();
            if (runsSize != timesSize * testsSize)
                mergePassTets(tests, times);
        }
    }

    private void mergePassTets(List<TCTest> tests, HashSet<LocalDateTime> times) {
        LocalDateTime mergeTime = null;
        List<TCTest> mergeTests = null;
        for (LocalDateTime time : times) {
            if (mergeTime == null) { //first time with tests
                mergeTests = filterTests(tests, time);
                if (mergeTests.size() > 0)
                    mergeTime = time;
            } else {
                List<TCTest> timeTests = filterTests(tests, time);
                if (timeTests.size() > 0 && filterTests(mergeTests, time).size()==0) { //Only merging if tests are totally separated
                    if (timeTests.size() == mergeTests.size() && sameStatus(timeTests, time)) {
                        TCStatus status = timeTests.get(0).getTestRun(time).getStatus();
                        if (status == TCStatus.PASS || status == TCStatus.SKIP) {
                            for (int t = 0; t < mergeTests.size(); t++) {
                                TCTest mergeTest = mergeTests.get(t);
                                mergeTest.addTestRun(time, status, "");
                                tests.remove(timeTests.get(t));
                            }
                        }
                    }
                }
            }
        }
    }

    private void mergeSkipTests(List<TCTest> tests) {
        TCTest skipTest = getSkipTest();
        if (skipTest != null) {
            List<TCTest> mergeTests = findTestsWithoutTime(tests, skipTest.getRunTimes());
            if (!mergeTests.isEmpty()) {//There is some skip test in a method with tests with parameters
                for (LocalDateTime skipTime : skipTest.getRunTimes()) {
                    TCTestRun skipRun = skipTest.getTestRun(skipTime);

                    for (TCTest mergeTest : mergeTests) {
                        mergeTest.addTestRun(skipTime, TCStatus.SKIP, skipRun.getStackTrace());
                    }
                    this.tests.remove(skipTest);
                }
            }
        }
    }

    private List<TCTest> findTestsWithoutTime(List<TCTest> tests, Set<LocalDateTime> runTimes) {
        List<TCTest> tcTests = new ArrayList<>();
        LocalDateTime runTime = findRunTime(tests, runTimes);
        for (TCTest test : tests) {
            if (test.getRunTimes().contains(runTime))
                    tcTests.add(test);
        }

        return tcTests;
    }

    private LocalDateTime findRunTime(List<TCTest> tests, Set<LocalDateTime> skipRunTimes) {
        for (TCTest test : tests) {
            for (LocalDateTime skipRunTime : skipRunTimes)
                if (!test.getRunTimes().contains(skipRunTime))
                    return test.getRunTimes().iterator().next();
        }

        return null;
    }

    private boolean sameStatus(List<TCTest> timeTests, LocalDateTime time) {
        TCStatus status = timeTests.get(0).getTestRun(time).getStatus();
        for (TCTest timeTest : timeTests) {
            if (timeTest.getTestRun(time).getStatus() != status)
                return false;
        }

        return true;
    }

    private List<TCTest> filterTests(List<TCTest> tests, LocalDateTime time) {
        List<TCTest> timeTests = new ArrayList<>();
        for (TCTest test : tests) {
            if (test.getRunTimes().contains(time))
                timeTests.add(test);
        }
        return timeTests;
    }

    public int getTestRunsSize() {
        int testRunsSize = 0;
        for (TCTest tcTest : tests)
            testRunsSize += tcTest.getTestRunsSize();

        return testRunsSize;
    }

    public int getTestsSize() {
        return tests.size();
    }
}
