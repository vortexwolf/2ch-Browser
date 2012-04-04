package com.vortexwolf.dvach.common.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;

public class HttpStringReader implements IHttpStringReader {
	public static final String TAG = "HttpStringReader";
	
	private final DefaultHttpClient mHttpClient;
	
	public HttpStringReader(DefaultHttpClient httpClient){
		this.mHttpClient = httpClient;
	}
	
	@Override
	public String fromUri(String uri){
		HttpGet request = null;
		try {
			request = new HttpGet(uri);
			HttpResponse response = mHttpClient.execute(request);

			String result = this.fromResponse(response);
			
			return result;
			
		} catch (Exception e) {
			MyLog.e(TAG, e);
		} finally {
			if(request != null){
				request.abort();
			}
		}
		
		return null;
	}
	
	@Override
	public String fromResponse(HttpResponse response){
		HttpEntity entity = null;
		try {
			entity = response.getEntity();
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), Constants.UTF8_CHARSET));
			StringBuilder sb =  new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			
			return sb.toString();
		} catch (Exception e) {
			MyLog.e(TAG, e);
		} finally {
			if (entity != null){
				try {
					entity.consumeContent();
				} catch (IOException e) {
					MyLog.e(TAG, e);
				}
			}
		}
		
		return null;
	}
}
