package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TCResults {
    private List<TCClass> classes;
    HashSet<LocalDateTime> startDateTimes = new HashSet<>();
    boolean mergeTests = false;


    public TCResults() {
        classes = new ArrayList<>();
    }

    public void addTest(String className, String methodName, String testParameters, String testStackTrace,
                        LocalDateTime startDateTime, TCStatus status) {
        startDateTimes.add(startDateTime);

        for (TCClass tcClass: classes) {
            if (tcClass.getClassName().equals(className)) {
                tcClass.addTest(methodName, testParameters, testStackTrace, startDateTime, status);
                return;
            }
        }
        TCClass tcClass = new TCClass(className);
        tcClass.addTest(methodName, testParameters, testStackTrace, startDateTime, status);
        classes.add(tcClass);
    }

    public int size() {
        int size = 0;
        for (TCClass tcClass : classes) {
            size += tcClass.size();
        }

        return size;
    }

    public int getClassesSize() {
        return classes.size();
    }

    public int getTestRunsSize(LocalDateTime startDateTime, TCStatus tcStatus) {
        return classes.stream().mapToInt(tcClass -> tcClass.getTestRunsSize(startDateTime, tcStatus)).sum();
    }

    public List<TCClass> getClasses() {
        return classes;
    }

    public HashSet<LocalDateTime> getTestStartDateTimes() {
        return startDateTimes;
    }

    public LocalDateTime getFirstDateTime() {
        Iterator<LocalDateTime> iteratorsFirstTime = startDateTimes.iterator();

        if (startDateTimes.size()==0)
            return null;

        LocalDateTime firstDateTime = iteratorsFirstTime.next();
        while (iteratorsFirstTime.hasNext()) {
            LocalDateTime dateTime = iteratorsFirstTime.next();
            if (dateTime.isBefore(firstDateTime))
                firstDateTime = dateTime;
        }

        return firstDateTime;
    }

    public LocalDateTime getLastDateTime() {
        Iterator<LocalDateTime> iteratorsLastTime = startDateTimes.iterator();

        if (startDateTimes.size()==0)
            return null;

        LocalDateTime firstDateTime = iteratorsLastTime.next();
        while (iteratorsLastTime.hasNext()) {
            LocalDateTime dateTime = iteratorsLastTime.next();
            if (dateTime.isAfter(firstDateTime))
                firstDateTime = dateTime;
        }

        return firstDateTime;
    }

    public void addTCResult(TCResults result) {
        startDateTimes.addAll(result.getTestStartDateTimes());
        for (TCClass resultClass : result.getClasses())
            addTCClass(resultClass);

        if (mergeTests && startDateTimes.size() > 1)//Could there be something to merge
            mergeTests();
    }

    private void addTCClass(TCClass resultClass) {
        for (TCClass tcClass : classes) {
            if (tcClass.equals(resultClass)) {
                tcClass.addMethods(resultClass);
                return;
            }
        }
        classes.add(resultClass);
    }

    public void mergeTests() {
        int timesSize = startDateTimes.size();
        int testsSize = getTestsSize();
        int runsSize = getTestRunsSize();

        if (runsSize != timesSize * testsSize) {
            for (TCClass tcClass : classes) {
                tcClass.mergeTests(startDateTimes);
            }
        }
    }

    private int getTestsSize() {
        int testsSize = 0;
        for (TCClass tcClass : classes)
            testsSize += tcClass.getTestsSize();

        return testsSize;
    }

    private int getTestRunsSize() {
        int testRunsSize = 0;
        for (TCClass tcClass : classes)
            testRunsSize += tcClass.getTestRunsSize();

        return testRunsSize;
    }

    public void setMergeTests(boolean mergeSkipTests) {
        this.mergeTests = mergeSkipTests;
    }
}
