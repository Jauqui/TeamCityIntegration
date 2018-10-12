package com.teamcity;

import com.teamcity.enums.TCParam;
import com.teamcity.excel.ComparativeResultExcelWriter;

import java.time.LocalDateTime;

public class CompareLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, getAuthToken());

        String path = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String name = "System Tests **standalone**";

        TCResults finalResult = new TCResults();
        finalResult.setMergeTests(true);
        ComparativeResultExcelWriter excelWriter = new ComparativeResultExcelWriter();
        for (int b = 0; b<40; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResults result = navigator.getTestNGResultsForBuild(path, TCParam.NAME, name, b);

            for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                System.out.println(result.size() + " -> " + startDateTime.toString());
            }
            finalResult.addTCResult(result);
            excelWriter.addResult(result);
        }

        LocalDateTime firstDateTime = finalResult.getFirstDateTime();
        LocalDateTime lastDateTime = finalResult.getLastDateTime();
        String fileName = "mergeResults/" + firstDateTime.toString().replace(":", "-") +
                "_To_" + lastDateTime.toString().replace(":", "-");
        excelWriter.writeToFile(finalResult, fileName);
    }
}
