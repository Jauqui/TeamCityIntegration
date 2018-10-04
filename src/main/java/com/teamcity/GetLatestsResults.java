package com.teamcity;


import com.teamcity.excel.ResultExcelWriter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class GetLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, authToken);

        String path = "httpAuth/app/rest/projects/ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String id = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone";
        for (int b = 0; b<50; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResult result = navigator.getResultsForBuildWithId(path, id, b);
            String className = "com.philips.sapphire.systemintegrationtests.tests.task.F1053_Trilogy_Rules_Refactored";
            String methodName = "Test_Create_MinuteVentilation_Task_Above_Range";
            TCMethod tcMethod = result.getTestMethod(className, methodName, "");
            if (tcMethod == null)
                System.out.println("Result MISSING");
            else {
                List<TCTest> tests = tcMethod.getTests();
                for (LocalDateTime startDateTime : tcMethod.getTestTimes()) {
                    for (TCTest test : tests) {
                        System.out.println(result.size() + " -> " + test.getStartDateTime().toString() + ": " + test.getStatus().name());
                    }
                    String fileName = "results/" + startDateTime.toString().replace(":", "-") + ".xlsx";
                    File file = new File(fileName);
                    if (!file.exists()) {
                        ResultExcelWriter excelResultWriter = new ResultExcelWriter();
                        excelResultWriter.writeResultFile(result, fileName);
                    }
                }
            }
        }
    }
}
