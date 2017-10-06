package ua.in.quireg.chan.interfaces;

import org.apache.http.Header;

import ua.in.quireg.chan.exceptions.HttpRequestException;

public interface IHttpStringReader {

    public abstract String fromUri(String uri) throws HttpRequestException;

    public abstract String fromUri(String uri, Header[] customHeaders) throws HttpRequestException;

}