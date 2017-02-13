package com.vortexwolf.chan.services.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vortexwolf.chan.BuildConfig;
import com.vortexwolf.chan.asynctasks.ParseBoardsTask;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.interfaces.IBoardsListCallback;

import org.json.JSONObject;



public class VolleyJsonReader {

    private static final String LOG_TAG = VolleyJsonReader.class.getSimpleName();
    //Since this is singletone, no context leak expected.
    @SuppressLint("StaticFieldLeak")
    private static VolleyJsonReader mInstance;
    private RequestQueue mRequestQueue;
    private Context mCtx;

    private VolleyJsonReader(Context context) {
        this.mCtx = context;
        this.mRequestQueue = getRequestQueue();

    }

    public static synchronized VolleyJsonReader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyJsonReader(context);
            if(BuildConfig.DEBUG) Log.d(LOG_TAG, "JSON Reader created");
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Volley.newRequestQueue requested");
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Request placed to queue: " + req.toString());
    }

    public void readBoards(String url, final IBoardsListCallback callback) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //onResponse method is invoked in main thread but boards object creation may take a while
                        //so it's better to move this process to asynctask.
                        new ParseBoardsTask(callback).execute(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MyLog.e(LOG_TAG, error);
                        AppearanceUtils.showToastMessage(mCtx, "Unable to fetch boards list from server");
                    }
                });
        this.addToRequestQueue(jsObjRequest);
    }
}

