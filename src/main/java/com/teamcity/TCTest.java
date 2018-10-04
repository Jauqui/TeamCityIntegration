package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

public class TCTest {
    private final String parameters;
    private final String stackTrace;
    private final TCStatus status;

    public TCTest(String parameters, String stackTrace, TCStatus status) {
        this.parameters = parameters;
        this.stackTrace = stackTrace;
        this.status = status;
    }

    public String getParameters() {
        return parameters;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public TCStatus getStatus() {
        return status;
    }
}
