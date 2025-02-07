package org.example;

import java.util.Map;

class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getValues(String key) {
        return queryParams.getOrDefault(key, "");
    }
}