package com.vortexwolf.dvach.models.domain;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class HttpStreamModel {
    public InputStream stream;
    public HttpRequestBase request;
    public HttpResponse response;
}
