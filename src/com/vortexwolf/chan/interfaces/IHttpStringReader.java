package com.vortexwolf.chan.interfaces;

import org.apache.http.Header;

import com.vortexwolf.chan.exceptions.HttpRequestException;

public interface IHttpStringReader {

    public abstract String fromUri(String uri) throws HttpRequestException;

    public abstract String fromUri(String uri, Header[] customHeaders) throws HttpRequestException;

}