package com.teamcity;


import com.teamcity.enums.TCParam;
import com.teamcity.excel.ResultExcelWriter;

import java.io.File;
import java.time.LocalDateTime;

public class GetLatestsResults extends TeamCityAPI {
    public static void main(String[] args) {
        RESTInvoker restInvoker = new RESTInvoker(baseUrl, getAuthToken());

        String projectName = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests";
        String id = "ContinuousDeliveryPipeline_Nti_StandaloneSystemTests_SystemTestsStandalone";
        if (args.length > 0) {
            for (int a=0; a<args.length; a++) {
                if (args[a].startsWith("-project="))
                    projectName = args[a].substring("-project=".length());
                else if (args[a].startsWith("-id="))
                    id = args[a].substring("-id=".length());
                else {
                    System.err.println("Unknown argument " + args[a]);
                    System.exit(1);
                }
            }
        }

        for (int b = 0; b<50; b++) {
            TCNavigator navigator = new TCNavigator(restInvoker);
            TCResults result = navigator.getResultsForBuild(projectName, TCParam.ID, id, b);
            if (result.getTestStartDateTimes().size() == 0)
                System.out.println(b + " no result");
            for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
                String fileName = "results/" + startDateTime.toString().replace(":", "-") + ".xlsx";
                File file = new File(fileName);
                if (!file.exists()) {
                    System.out.println(b + " adding result " + startDateTime.toString());
                    ResultExcelWriter excelResultWriter = new ResultExcelWriter();
                    excelResultWriter.writeResultFile(result, fileName);
                } else
                    System.out.println(b + " result already exist " + startDateTime.toString());
            }
        }
    }
}
