package com.teamcity;

import com.teamcity.enums.TCMetric;
import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.Set;

public class MetricsCalculator {
    public static String processMetric(Set<LocalDateTime> startDateTimes, TCTest tcTest, TCMetric metric) {
        switch (metric) {
            case Total_Runs:
                return computeTotalRuns(startDateTimes, tcTest);
            case Pass_Percentage:
                return computePassPercentage(tcTest);
        }

        return  null;
    }

    private static String computeTotalRuns(Set<LocalDateTime> startDateTimes, TCTest tcTest) {
        return tcTest.getRunTimes().size() + "/" + startDateTimes.size();
    }

    public static String computePassPercentage(TCTest tcTest) {
        double percentage = tcTest.getRunsCountWithStatus(TCStatus.PASS) * 100 / tcTest.getRunsCount();
        return String.valueOf(percentage);
    }
}
