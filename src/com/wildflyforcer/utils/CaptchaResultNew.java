package com.wildflyforcer.utils;

import org.codehaus.jackson.annotate.JsonProperty;

public class CaptchaResultNew {
    @JsonProperty("id")
    public String id;
    @JsonProperty("result")
    public String result;
    @JsonProperty("type")
    public String type;

    public CaptchaResultNew() {
    }

    public CaptchaResultNew(String id, String result, String type) {
        this.id = id;
        this.result = result;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}