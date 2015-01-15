package com.vortexwolf.chan.models.domain;

public class CaptchaEntity {
    public enum Type { YANDEX, RECAPTCHA_CF, RECAPTCHA_POST }

    private String url;
    private String key;
    private Type type;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
