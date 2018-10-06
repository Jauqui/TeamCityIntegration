package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TCTest {
    private final String parameters;
    HashMap<LocalDateTime, TCTestRun> runs;

    public TCTest(String parameters) {
        this.parameters = parameters;
        this.runs = new HashMap<>();
    }

    public String getParameters() {
        return parameters;
    }

    public void addTestRun(LocalDateTime startDateTime, TCStatus status, String stackTracke) {
        runs.put(startDateTime, new TCTestRun(startDateTime, status, stackTracke));
    }

    public TCTestRun getTestRun(LocalDateTime startDateTime) {
        return runs.get(startDateTime);
    }

    public Set<LocalDateTime> getRunTimes() {
        return runs.keySet();
    }

    public int getTestRunsSize(LocalDateTime startDateTime, TCStatus tcStatus) {
        if (!runs.containsKey(startDateTime))
            return 0;

        TCTestRun tcTestRun = runs.get(startDateTime);
        if (tcTestRun.getStatus()!= tcStatus)
            return 0;
        return 1;
    }

    public void addTCTest(TCTest resultTest) {
        for (LocalDateTime runTime : resultTest.getRunTimes()) {
            TCTestRun resultTestRun = resultTest.getTestRun(runTime);
            runs.put(runTime, resultTestRun);
        }
    }

    public int getRunsCountWithStatus(TCStatus status) {
        int count = 0;
        for (LocalDateTime time : runs.keySet()) {
            if (runs.get(time).getStatus() == TCStatus.PASS)
                count++;
        }

        return count;
    }

    public int getRunsCount() {
        return runs.size();
    }
}
