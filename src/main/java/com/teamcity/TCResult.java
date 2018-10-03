package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TCResult {
    private List<TCClass> classes;
    private List<LocalDateTime> startDateTimes;

    public TCResult() {
        startDateTimes = new ArrayList<>();
        classes = new ArrayList<>();
    }

    public void addTest(String className, String methodName, String testParameters, String testStackTrace,
                        String startTime, TCStatus status) {
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"));
        if (!startDateTimes.contains(startDateTime))
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

    public TCMethod getTestMethod(String className, String methodName, String parameters) {
        for (TCClass tcClass : classes) {
            if (tcClass.getClassName().equals(className))
                return tcClass.getTestMethod(methodName, parameters);
        }

        return null;
    }

    public int getClassesSize() {
        return classes.size();
    }

    /*public List<TCTestResult> getTests() {
        List<TCTestResult> tests = new ArrayList<>();
        for (TCClass tcClass : classes) {
            tests.addAll(tcClass.getTests());
        }
        return tests;
    }*/

    public List<TCClass> getClasses() {
        return classes;
    }

    public HashSet<LocalDateTime> getRunTimes() {
        HashSet<LocalDateTime> tcRunDateTime = new HashSet<>();
        for (TCClass tcClass : classes) {
            HashSet<LocalDateTime> tcClassDateTimes = tcClass.getRunTimes();
            tcRunDateTime.addAll(tcClassDateTimes);
        }

        return tcRunDateTime;
    }
}
