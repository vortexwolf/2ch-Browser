package com.vortexwolf.chan.services.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vortexwolf.chan.BuildConfig;
import com.vortexwolf.chan.activities.PickBoardActivity;
import com.vortexwolf.chan.asynctasks.ProcessReadBoardsRespond;
import com.vortexwolf.chan.asynctasks.helpers.ProcessReadBoardsRespondParams;

import org.json.JSONObject;



public class VolleyJsonReader {

    private static final String LOG_TAG = VolleyJsonReader.class.getSimpleName();

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

    public void readBoards(String url, final PickBoardActivity pickBoardActivity) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //onResponse method is invoked in main thread but boards object creation may take a while
                        //so it's better to move this process to asynctask.

                        //Helper class is created to pass both, response and initial activity so adapter callback can be triggered.
                        ProcessReadBoardsRespondParams processReadBoardsRespondParams = new ProcessReadBoardsRespondParams(response, pickBoardActivity);
                        new ProcessReadBoardsRespond().execute(processReadBoardsRespondParams);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        this.addToRequestQueue(jsObjRequest);
    }
}

