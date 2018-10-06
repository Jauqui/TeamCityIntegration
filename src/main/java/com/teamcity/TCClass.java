package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TCClass {
    private final String className;

    List<TCMethod> methods;

    public TCClass(String className) {
        methods = new ArrayList<>();
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public int size() {
        int size = 0;
        for (TCMethod tcMethod : methods) {
            size += tcMethod.size();
        }

        return size;
    }

    public void addTest(String methodName, String parameters, String stackTrace, LocalDateTime startDateTime, TCStatus status) {
        for (TCMethod method : methods) {
            if (method.getMethodName().equals(methodName)) {
                method.addTest(parameters, stackTrace, startDateTime, status);
                return;
            }
        }

        TCMethod tcMethod = new TCMethod(methodName);
        tcMethod.addTest(parameters, stackTrace, startDateTime, status);
        methods.add(tcMethod);
    }

    public boolean equals(TCClass tcClass) {
        return this.className.equals(tcClass.getClassName());
    }

    public TCMethod getTestMethod(String methodName, String parameters) {
        for (TCMethod tcMethod : methods) {
            if (tcMethod.getMethodName().equals(methodName)) {
                return tcMethod.filterTests(parameters);
            }
        }
        return null;
    }

    /*public List<TCTestResult> getTests() {
        List<TCTestResult> tcTests = new ArrayList<>();
        for (TCMethod method : methods) {
            tcTests.addAll(method.getTests());
        }

        return tcTests;
    }*/

    public List<TCMethod> getMethods() {
        return methods;
    }

    public void addMethods(TCClass resultClass) {
        for (TCMethod resultMethod : resultClass.getMethods())
            addTCMethod(resultMethod);
    }

    private void addTCMethod(TCMethod resultMethod) {
        for (TCMethod tcMethod : methods) {
            if (tcMethod.equals(resultMethod)) {
                tcMethod.addTCTests(resultMethod);
                return;
            }
        }
        methods.add(resultMethod);
    }

    public int getTestRunsSize(LocalDateTime startDateTime, TCStatus tcStatus) {
        return methods.stream().mapToInt(tcMethod -> tcMethod.getTestRunsSize(startDateTime, tcStatus)).sum();
    }

    /*public TCClass filterMethodsByRunTime(LocalDateTime localDateTime) {
        TCClass tcClass = new TCClass(this.className);
        for (TCMethod tcMethod : methods) {
            tcClass.addMethod(tcMethod.filterTestsByRunTime(localDateTime));
        }

        return tcClass;
    }*/
}
