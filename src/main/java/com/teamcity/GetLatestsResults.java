package com.teamcity;


import com.teamcity.excel.ResultExcelWriter;

import java.io.File;
import java.time.LocalDateTime;

public class GetLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, getAuthToken());

        String path = "httpAuth/app/rest/projects/ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String id = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone";
        for (int b = 0; b<50; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResults result = navigator.getResultsForBuildWithId(path, id, b);
            for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                String fileName = "results/" + startDateTime.toString().replace(":", "-") + ".xlsx";
                File file = new File(fileName);
                if (!file.exists()) {
                    System.out.println(b + " Adding result " + startDateTime.toString());
                    ResultExcelWriter excelResultWriter = new ResultExcelWriter();
                    excelResultWriter.writeResultFile(result, fileName);
                } else
                    System.out.println(b + " Result already exist " + startDateTime.toString());
            }
        }
    }
}
