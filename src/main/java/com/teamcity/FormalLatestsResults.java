package com.teamcity;

import com.teamcity.enums.TCMetric;
import com.teamcity.enums.TCParam;
import com.teamcity.excel.ComparativeResultExcelWriter;

import java.time.LocalDateTime;
import java.util.List;

public class FormalLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, getAuthToken());

        String path = "Teams_SystemTestingTeam_FormalTestSets";
        String name = "Formal Test Set - 1.17 Branch";
        List<TCMetric> metrics = null;

        TCResults finalResult = new TCResults();
        finalResult.setMergeTests(true);
        ComparativeResultExcelWriter excelWriter = new ComparativeResultExcelWriter(metrics);
        TCResults result = null;
        int b = 0;
        TCNavigator navigator = new TCNavigator(restInvoker);
        do {
            navigator.addFilterProperty("system.Environment", "SAPPHIREQAS1");
            result = navigator.getTestNGResultsForBuild(path, TCParam.NAME, name, b++);

            if (result != null && result.getTestStartDateTimes().size() > 0) {
                for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                    System.out.println(result.size() + " -> " + startDateTime.toString());
                }
                finalResult.addTCResult(result);
                excelWriter.addResult(result);
            }
        } while (result != null);

        //include personal builds
        navigator.setPersonalBuild(true);
        b = 0;
        do {
            result = navigator.getTestNGResultsForBuild(path, TCParam.NAME, name, b++);

            if (result != null && result.getTestStartDateTimes().size() > 0) {
                for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                    System.out.println(result.size() + " -> " + startDateTime.toString());
                }
                finalResult.addTCResult(result);
                excelWriter.addResult(result);
            }
        } while (result != null);

        LocalDateTime firstDateTime = finalResult.getFirstDateTime();
        LocalDateTime lastDateTime = finalResult.getLastDateTime();
        String fileName = "mergeResults/" + firstDateTime.toString().replace(":", "-") +
                "_To_" + lastDateTime.toString().replace(":", "-");
        excelWriter.writeToFile(finalResult, fileName);
    }
}
