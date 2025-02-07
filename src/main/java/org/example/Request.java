package org.example;

import java.util.Map;

public class Request {
    private String method;
    private String path;
    private Map<String, String> queryParams;
    private String body;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.body = "";
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getValues(String key) {
        return queryParams.get(key);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
