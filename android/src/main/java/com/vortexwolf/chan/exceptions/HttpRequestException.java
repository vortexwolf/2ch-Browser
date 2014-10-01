package com.vortexwolf.chan.exceptions;

public class HttpRequestException extends Exception {
    private static final long serialVersionUID = -4886859098677607941L;

    public HttpRequestException() {
        super();
    }

    public HttpRequestException(String detailMessage) {
        super(detailMessage);
    }

    public HttpRequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
