package com.teamcity;

import com.teamcity.enums.TCStatus;


public class TCTestResult {
    private final String testStackTrace;
    private final TCStatus status;


    public TCTestResult(String testStackTrace, TCStatus status) {
        this.testStackTrace = testStackTrace;
        this.status = status;
    }

    public String getStackTrace() {
        return testStackTrace;
    }

    public TCStatus getStatus() {
        return status;
    }
}
