package com.vortexwolf.chan.asynctasks;

import android.os.AsyncTask;

import com.vortexwolf.chan.boards.makaba.MakabaModelsMapper;
import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.GeneralUtils;
import com.vortexwolf.chan.interfaces.IBoardsListCallback;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.settings.ApplicationSettings;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ParseBoardsTask extends AsyncTask<JSONObject, Void, ArrayList<BoardModel>> {

    private static final String LOG_TAG = ParseBoardsTask.class.getSimpleName();

    private IBoardsListCallback callback;
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);


    public ParseBoardsTask(IBoardsListCallback callback){
        this.callback = callback;
    }

    @Override
    protected ArrayList<BoardModel> doInBackground(JSONObject... jsonObjects) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);

        ArrayList<BoardModel> models = new ArrayList<>();
        try {
            MyLog.d(LOG_TAG, "Processing retrieved JSON");

            JsonNode result = mapper.readValue(jsonObjects[0].toString(), JsonNode.class);

            //Parse each category as single JsonNode
            Iterator iterator = result.getElements();
            while (iterator.hasNext()){
                JsonNode node = (JsonNode) iterator.next();
                MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);
                BoardModel[] tempBoardModels = MakabaModelsMapper.mapBoardModels(data);

                models.addAll(Arrays.asList(tempBoardModels));

            }
            //Now let's check if received array differs from one that is currently stored in settings.
            //If not, we will return empty list
            if(GeneralUtils.equalLists(models,this.mSettings.getBoards())){
                MyLog.d(LOG_TAG, "Boards list has not been modified since last check");
                models.clear();
                return models;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyLog.d(LOG_TAG, "Boards list have been modified, updating...");
        this.mSettings.setBoards(models);
        return models;
    }

    @Override
    protected void onPostExecute(ArrayList<BoardModel> models) {
        //In case something bad happened and we did not receive boards we won't update adapter.
        //Values from previous run, stored in settings will be displayed.
        if(!models.isEmpty()){
            this.callback.listUpdated(models);
        }
    }
}

