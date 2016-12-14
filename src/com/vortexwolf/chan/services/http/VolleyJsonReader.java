package com.vortexwolf.chan.services.http;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vortexwolf.chan.BuildConfig;
import com.vortexwolf.chan.activities.PickBoardActivity;
import com.vortexwolf.chan.boards.makaba.MakabaModelsMapper;
import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.settings.SettingsEntity;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
            if(BuildConfig.DEBUG) Log.d(LOG_TAG, "JSON Reader Created");
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



    private class ProcessReadBoardsRespond extends AsyncTask<ProcessReadBoardsRespondParams, Void, ArrayList<BoardModel>> {
        private PickBoardActivity pickBoardActivity = null;

        @Override
        protected ArrayList<BoardModel> doInBackground(ProcessReadBoardsRespondParams... processReadBoardsResponceParams) {
            this.pickBoardActivity = processReadBoardsResponceParams[0].pickBoardActivity;
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);

            ArrayList<BoardModel> models = new ArrayList<>();
            try {
                if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Processing retrieved JSON");

                //Костыль для обработки разделов тематики в которые завёрнуты сами доски.
                //Не судите строго
                JsonNode result = mapper.readValue(processReadBoardsResponceParams[0].response.toString(), JsonNode.class);

                //Parse each category as single JsonNode
                Iterator iterator = result.getElements();
                while (iterator.hasNext()){
                    JsonNode node = (JsonNode) iterator.next();
                    MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);
                    BoardModel[] tempBoardModels = MakabaModelsMapper.mapBoardModels(data);
                    for (BoardModel tempBoardModel : tempBoardModels) {
                        models.add(tempBoardModel);
                    }
                }
                //Now let's check if received array differs from one that is currently stored in settings.
                //If not, we will return empty list
                SettingsEntity se = this.pickBoardActivity.getmSettings().getCurrentSettings();

                if(equalLists(models, se.mBoards)){
                    if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Boards list has not been modified since last check");
                    models.clear();
                    return models;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            pickBoardActivity.getmSettings().setBoards(models);
            return models;
        }

        @Override
        protected void onPostExecute(ArrayList<BoardModel> models) {
            //In case something bad happened and we did not receive boards we won't update adapter.
            //Values from previous run, stored in settings will be displayed.
            if(models.size() != 0){
                this.pickBoardActivity.getmBoards().clear();
                for (BoardModel model: models) {
                    this.pickBoardActivity.getmBoards().add(model);
                }
                this.pickBoardActivity.updateVisibleBoards(this.pickBoardActivity.getmAdapter());
            }
        }
        private boolean equalLists(List<BoardModel> a, List<BoardModel> b){
            for (BoardModel modelA: a) {
                boolean matchFound = false;
                for (BoardModel modelB: b) {
                    if(modelA.getId().equals(modelB.getId())){
                        matchFound = true;
                        break;
                    }
                }
                if(!matchFound){
                    if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Match was not found for board: " + modelA.getId() +
                    " Updating boards list");
                    return false;
                }
            }
            return true;
        }
    }

    private class ProcessReadBoardsRespondParams{
        JSONObject response;
        PickBoardActivity pickBoardActivity;

        ProcessReadBoardsRespondParams(JSONObject response, PickBoardActivity pickBoardActivity) {
            this.response = response;
            this.pickBoardActivity = pickBoardActivity;
        }
    }
}

