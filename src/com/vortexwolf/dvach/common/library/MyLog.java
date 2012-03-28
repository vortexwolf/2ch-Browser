package com.vortexwolf.dvach.common.library;

import org.apache.http.StatusLine;

import com.vortexwolf.dvach.common.Constants;

import android.util.Log;

public class MyLog {

	public static void d(String tag, String msg) {
		if (Constants.LOGGING) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (Constants.LOGGING) {
			Log.i(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (Constants.LOGGING) {
			Log.e(tag, msg);
		}
	}
	
	public static void e(String tag, Throwable e) {
		if (Constants.LOGGING) {
			if(e.getCause() != null){
				Log.e(tag, e.getCause().getMessage().toString() + " " + e.getMessage().toString());
			}
			else {
				Log.e(tag, e.getClass().getName() + " " + e.getMessage() != null ? e.getMessage().toString() : "");
			}
		}
	}

	public static void w(String tag, String msg) {
		if (Constants.LOGGING) {
			Log.w(tag, msg);
		}
	}
	
	public static void v(String tag, String msg) {
		if (Constants.LOGGING) {
			Log.v(tag, msg);
		}
	}
	
	public static void v(String tag, StatusLine status) {
		if (Constants.LOGGING) {
			Log.v(tag, status.getStatusCode()+ " " + status.getReasonPhrase());
		}
	}
}