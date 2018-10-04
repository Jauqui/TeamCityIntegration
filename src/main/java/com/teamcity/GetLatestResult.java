package com.teamcity;


import java.time.LocalDateTime;
import java.util.List;

public class GetLatestResult {
    static String authToken = System.getenv().get("TeamCity_AuthToken");

    public static void main(String[] args) {
        String baseUrl = "https://teamcity.sapphirepri.com/";
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, authToken);

        String path = "httpAuth/app/rest/projects/ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String id = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone";
        for (int b = 0; b<50; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResult result = navigator.getResultsForBuildWithId(path, id, b);

            String className = "com.philips.sapphire.systemintegrationtests.tests.task.F1053_Trilogy_Rules_Refactored";
            String methodName = "Test_Create_MinuteVentilation_Task_Above_Range";
            TCMethod tcMethod = result.getTestMethod(className, methodName, "");
            LocalDateTime startDateTime = result.getStartDateTime();
            if (tcMethod == null)
                System.out.println("Result MISSING");
            else {
                List<TCTest> tests = tcMethod.getTests();
                for (TCTest test : tests) {
                    System.out.println(result.size() + " -> " + startDateTime.toString() + "): " + test.getStatus().name());
                }
            }
            ExcelWriter excelWriter = new ExcelWriter();
            excelWriter.addResultsToFile(result);
            excelWriter.writeToFile(result.getStartDateTime().toString().replace(":", "-"));
        }
    }
}
