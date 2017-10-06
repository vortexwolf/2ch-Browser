package ua.in.quireg.chan.services.http;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class HttpStreamModel {
    public InputStream stream;
    public HttpRequestBase request;
    public HttpResponse response;
    public boolean notModifiedResult;
}
