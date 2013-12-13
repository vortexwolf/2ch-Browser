package com.vortexwolf.dvach.interfaces;

import org.apache.http.Header;

import com.vortexwolf.dvach.exceptions.HttpRequestException;

public interface IHttpStringReader {

    public abstract String fromUri(String uri) throws HttpRequestException;

    public abstract String fromUri(String uri, Header[] customHeaders) throws HttpRequestException;

}