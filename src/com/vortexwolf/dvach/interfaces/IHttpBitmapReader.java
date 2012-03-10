package com.vortexwolf.dvach.interfaces;

import android.graphics.Bitmap;

public interface IHttpBitmapReader {

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
	public Bitmap fromUri(String uri);

}