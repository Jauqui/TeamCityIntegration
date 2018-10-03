package com.teamcity;


import java.time.LocalDateTime;
import java.util.List;

public class GetLatestResult {
    static String username = System.getenv().get("TeamCity_Username");
    static String password = System.getenv().get("TeamCity_Password");

    public static void main(String[] args) {
        String baseUrl = "https://teamcity.sapphirepri.com/";
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, username, password);

        String path = "httpAuth/app/rest/projects/ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        for (int b = 0; b<50; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            navigator.openBuildsForProjectById(path, "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone");
            navigator.openBuildByIndex(b);

            TCResult result = navigator.getResultsForBuild();

            String className = "com.philips.sapphire.systemintegrationtests.tests.task.F1053_Trilogy_Rules_Refactored";
            String methodName = "Test_Create_MinuteVentilation_Task_Above_Range";
            TCMethod tcMethod = result.getTestMethod(className, methodName, "");
            if (tcMethod == null)
                System.out.println("Result MISSING");
            else {
                List<TCTest> tests = tcMethod.getTests();
                for (TCTest test : tests) {
                    for (LocalDateTime startTime : test.getResults().keySet()) {
                        TCTestResult testResult = test.getResultForTime(startTime);
                        System.out.println(result.size() + " -> " + startTime.toString() + "(" + test.getParameters() + "): " +
                                testResult.getStatus().name());
                    }
                }
            }
            ExcelWriter excelWriter = new ExcelWriter();
            excelWriter.addResultsToFile(result);
            excelWriter.writeToFile(LocalDateTime.now().toString().replace(":", "-"));
        }
    }
}
