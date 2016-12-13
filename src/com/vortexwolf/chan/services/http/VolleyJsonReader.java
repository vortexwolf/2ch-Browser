package com.vortexwolf.chan.services.http;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vortexwolf.chan.adapters.BoardsListAdapter;
import com.vortexwolf.chan.boards.makaba.MakabaModelsMapper;
import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.models.domain.BoardModel;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class VolleyJsonReader {

    private static final String LOG_TAG = VolleyJsonReader.class.getSimpleName();

    private static VolleyJsonReader mInstance;
    private RequestQueue mRequestQueue;
    private final Resources mResources;
    private Context mCtx;

    private VolleyJsonReader(Resources resources, Context context) {
        this.mResources = resources;
        this.mCtx = context;
        this.mRequestQueue = getRequestQueue();

    }

    public static synchronized VolleyJsonReader getInstance(Resources resources, Context context) {
        if (mInstance == null) {
            mInstance = new VolleyJsonReader(resources, context);
            Log.d(LOG_TAG, "JSON Fetcher created");
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            Log.d(LOG_TAG, "Volley.newRequestQueue requested");

        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        Log.d(LOG_TAG, "Request placed to queue");

    }



    public void readBoards(String url, final BoardsListAdapter adapter) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
                        mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);

                        ArrayList<BoardModel> models = new ArrayList<>();
                        try {
                            //Костыль для обработки разделов тематики в которые завёрнуты сами доски.
                            //Не судите строго
                            JsonNode result = mapper.readValue(response.toString(), JsonNode.class);

                            //Parse each category as single JsonNode
                            Iterator iterator = result.getElements();
                            while (iterator.hasNext()){
                                JsonNode node = (JsonNode) iterator.next();
                                MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);
                                BoardModel[] tempBoardModels = MakabaModelsMapper.mapBoardModels(data);
                                for (int i = 0; i < tempBoardModels.length; i++) {
                                    models.add(tempBoardModels[i]);
                                }
                            }
                            System.out.println("Stub!");
                            adapter.setData(models);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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