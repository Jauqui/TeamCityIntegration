package com.teamcity.enums;

public enum TCParam {
    ID("id"),
    NAME("name"),
    PROJECT_NAME("projectName"),
    PROJECT_ID("projectId"),
    HREF("href"),
    WEB_URL("webUrl"),
    BUILD_TYPE("buildType"),
    ;


    private final String parameter;


    TCParam(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
