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
            TCTest skipTest = getSkipTest();
            if (skipTest != null) {
                List<TCTest> mergeTests = findTestsWithoutTime(tests, skipTest.getRunTimes());
                if (!mergeTests.isEmpty()) {//There is some skip test in a method with tests with parameters
                    for (LocalDateTime skipTime : skipTest.getRunTimes()) {
                        TCTestRun skipRun = skipTest.getTestRun(skipTime);

                        for (TCTest mergeTest : mergeTests) {
                            mergeTest.addTestRun(skipTime, TCStatus.SKIP, skipRun.getStackTrace());
                        }
                        tests.remove(skipTest);
                    }
                }
            }
            /*LocalDateTime mergeTime = null;
            List<TCTest> mergeTests = null;
            for (LocalDateTime time : times) {
                if (mergeTime == null) { //first time
                    mergeTime = time;
                    mergeTests = filterTests(tests, time);
                } else {
                    List<TCTest> timeTests = filterTests(tests, time);
                    /*if (timeTests.size()>0) {
                        if (timeTests.size() == mergeTests.size() && sameStatus(timeTests, time)) {
                            TCStatus status = timeTests.get(0).getTestRun(time).getStatus();
                            for (int t = 0; t < mergeTests.size(); t++) {
                                TCTest mergeTest = mergeTests.get(t);
                                mergeTest.addTestRun(time, status, "???");
                                tests.remove(timeTests.get(t));
                            }
                        } else*/
                    /*if (timeTests.size() == 1) {
                        TCTest tcTest = timeTests.get(0);
                        if (tcTest.getParameters().isEmpty()) { //Skipped runs that have no parameters due to dependency failure
                            List<TCTestRun> skipRuns = tcTest.getRuns(TCStatus.SKIP);
                            if (skipRuns.size() == 1) {//Not all results were skipped
                                System.out.println("Skipped "+ this.methodName + " " +  skipRuns.get(0).getStartDateTime());
                                for (TCTest mergeTest : mergeTests) {
                                    mergeTest.addTestRun(time, TCStatus.SKIP, tcTest.getTestRun(time).getStackTrace());
                                }
                                tests.remove(tcTest);
                            }
                        }
                    }
                    //}
                }
            }*/
            /*for (TCTest tcTest : tests) {
                if (tcTest.getParameters().isEmpty()) { //Skipped runs that have no parameters due to dependency failure
                    List<TCTestRun> skipRuns = tcTest.getRuns(TCStatus.SKIP);
                    Set<LocalDateTime> runTimes = tcTest.getRunTimes();
                    if (skipRuns.size() != runTimes.size()) {//Not all results were skipped
                        LocalDateTime mergeTime = getMergeTime(runTimes, skipRuns);
                        for (int s=0; s< skipRuns.size(); s++) {
                            tcTest.addTestRun(mergeTime, TCStatus.SKIP, "");
                        }
                    }
                }
            }*/
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

    private boolean testMissingTimes(TCTest tcTest, Set<LocalDateTime> runTimes) {
        for (LocalDateTime time : tcTest.getRunTimes()) {
            if (!runTimes.contains(time))
                return true;
        }

        return false;
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

    private LocalDateTime getMergeTime(Set<LocalDateTime> runTimes, List<TCTestRun> tcRuns) {
        HashSet<LocalDateTime> tcRunTimes = new HashSet<>();
        for (TCTestRun tcTestRun: tcRuns) {
            tcRunTimes.add(tcTestRun.getStartDateTime());
        }
        for (LocalDateTime runTime : runTimes) {
            if (!tcRunTimes.contains(runTime))
                return runTime;
        }

        return null;
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
