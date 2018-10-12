package com.teamcity;

import com.teamcity.enums.TCMetric;
import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.Set;

public class MetricsCalculator {
    public static Object processMetric(Set<LocalDateTime> times, TCTest tcTest, TCMetric metric) {
        switch (metric) {
            case Total_Runs:
                return computeTotalRuns(times, tcTest);
            case Pass_Percentage:
                return computePassPercentage(tcTest);
            case Stability_Percentage:
                return stabilityPercentage(times, tcTest);
        }

        return  null;
    }

    private static Object stabilityPercentage(Set<LocalDateTime> times, TCTest tcTest) {
        int runTimes = times.size();
        int testPass = tcTest.getRunsCountWithStatus(TCStatus.PASS);
        int testSkip = tcTest.getRunsCountWithStatus(TCStatus.SKIP);
        int testFail = tcTest.getRunsCountWithStatus(TCStatus.FAIL);

        int max = Math.max(Math.max(testFail, testPass), testSkip);
        return max * 100.0 / runTimes;
    }

    private static String computeTotalRuns(Set<LocalDateTime> startDateTimes, TCTest tcTest) {
        return tcTest.getRunTimes().size() + "/" + startDateTimes.size();
    }

    public static Object computePassPercentage(TCTest tcTest) {
        double percentage = tcTest.getRunsCountWithStatus(TCStatus.PASS) * 100.0 / tcTest.getRunsCount();
        return percentage;
    }
}
