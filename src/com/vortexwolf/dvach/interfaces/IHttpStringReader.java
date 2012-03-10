package com.vortexwolf.dvach.interfaces;

import org.apache.http.HttpResponse;

public interface IHttpStringReader {

	public String fromUri(String uri);

	public String fromResponse(HttpResponse response);

}