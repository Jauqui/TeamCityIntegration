package com.teamcity;

import com.teamcity.enums.TCStatus;

import java.time.LocalDateTime;

public class TCTestRun {
    private final LocalDateTime startDateTime;
    private final TCStatus status;
    private final String stackTrace;


    public TCTestRun(LocalDateTime startDateTime, TCStatus status, String stackTrace) {
        this.startDateTime = startDateTime;
        this.status = status;
        this.stackTrace = stackTrace;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public TCStatus getStatus() {
        return status;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
