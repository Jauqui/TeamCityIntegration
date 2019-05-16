package com.teamcity;

import com.teamcity.enums.TCMetric;
import com.teamcity.enums.TCParam;
import com.teamcity.excel.ComparativeResultExcelWriter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


public class CompareLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, getAuthToken());

        String path = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String name = "System Tests **standalone**";
        List<TCMetric> metrics = Arrays.asList(TCMetric.Total_Runs, TCMetric.Pass_Percentage, TCMetric.Stability_Percentage);

        TCResults finalResult = new TCResults();
        finalResult.setMergeTests(true);
        ComparativeResultExcelWriter excelWriter = new ComparativeResultExcelWriter(metrics);
        TCNavigator navigator = new TCNavigator(restInvoker);
        for (int b = 0; b<60; b++) {
            TCResults result = navigator.getTestNGResultsForBuild(path, TCParam.NAME, name, b);

            if (result != null && result.getTestStartDateTimes().size() > 0) {
                for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                    System.out.println(result.size() + " -> " + startDateTime.toString());
                }
                finalResult.addTCResult(result);
                excelWriter.addResult(result);
            }
        }

        LocalDateTime firstDateTime = finalResult.getFirstDateTime();
        LocalDateTime lastDateTime = finalResult.getLastDateTime();
        String fileName = "mergeResults/" + firstDateTime.toString().replace(":", "-") +
                "_To_" + lastDateTime.toString().replace(":", "-");
        excelWriter.writeToFile(finalResult, fileName);
    }
}
