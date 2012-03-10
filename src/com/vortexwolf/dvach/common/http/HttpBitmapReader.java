package com.vortexwolf.dvach.common.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vortexwolf.dvach.common.library.FlushedInputStream;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.interfaces.IHttpBitmapReader;

public class HttpBitmapReader implements IHttpBitmapReader {
	public static final String TAG = "HttpBitmapReader";
	
	private final DefaultHttpClient mHttpClient;

	public HttpBitmapReader(DefaultHttpClient httpClient) {
		this.mHttpClient = httpClient;
	}
	
	/**
	 * http://ballardhack.wordpress.com/2010/04/10/loading-images-over-http-on-a-separate-thread-on-android/ 
	 * Convenience method to retrieve a bitmap image from a URL over the network. 
	 * The built-in methods do not seem to work, as they return a FileNotFound exception.
	 * 
	 * Note that this does not perform any threading -- it blocks the call while
	 * retrieving the data.
	 * 
	 * @param url The URL to read the bitmap from.
	 * @return A Bitmap image or null if an error occurs.
	 */
	@Override
	public Bitmap fromUri(String uri) {
		HttpGet request = null;
		HttpEntity entity = null;
		Bitmap bmp = null;
		try {
			request = new HttpGet(uri);
			HttpResponse response = this.mHttpClient.execute(request);
			entity = response.getEntity();
			FlushedInputStream fis = new FlushedInputStream(entity.getContent());
			bmp = BitmapFactory.decodeStream(fis);
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
			if(request != null){
				request.abort();
			}
		}
		return bmp;
	}
}
