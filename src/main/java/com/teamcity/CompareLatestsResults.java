package com.teamcity;

import com.teamcity.excel.ComparativeResultExcelWriter;

import java.time.LocalDateTime;

public class CompareLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, authToken);

        String path = "httpAuth/app/rest/projects/ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String id = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone";

        TCResult finalResult = new TCResult();
        ComparativeResultExcelWriter excelWriter = new ComparativeResultExcelWriter();
        for (int b = 0; b<5; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResult result = navigator.getResultsForBuildWithId(path, id, b);

            for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                System.out.println(result.size() + " -> " + startDateTime.toString());
            }
            finalResult.addTCResult(result);
            excelWriter.addResult(result);
        }

        LocalDateTime firstDateTime = finalResult.getFirstDateTime();
        LocalDateTime lastDateTime = finalResult.getLastDateTime();
        String fileName = "results/" + firstDateTime.toString().replace(":", "-") +
                "_To_" + lastDateTime.toString().replace(":", "-") + ".xlsx";
        excelWriter.writeToFile(finalResult, fileName);
    }
}
