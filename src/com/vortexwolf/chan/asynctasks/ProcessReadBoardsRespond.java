package com.vortexwolf.chan.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.vortexwolf.chan.BuildConfig;
import com.vortexwolf.chan.activities.PickBoardActivity;
import com.vortexwolf.chan.asynctasks.helpers.ProcessReadBoardsRespondParams;
import com.vortexwolf.chan.boards.makaba.MakabaModelsMapper;
import com.vortexwolf.chan.boards.makaba.models.MakabaBoardInfo;
import com.vortexwolf.chan.common.utils.GeneralUtils;
import com.vortexwolf.chan.models.domain.BoardModel;
import com.vortexwolf.chan.settings.SettingsEntity;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ProcessReadBoardsRespond extends AsyncTask<ProcessReadBoardsRespondParams, Void, ArrayList<BoardModel>> {

    private static final String LOG_TAG = ProcessReadBoardsRespond.class.getSimpleName();

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

            if(GeneralUtils.equalLists(models, se.mBoards)){
                if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Boards list has not been modified since last check");
                models.clear();
                return models;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(BuildConfig.DEBUG) Log.d(LOG_TAG, "Boards list have been modified, updating...");
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



}

