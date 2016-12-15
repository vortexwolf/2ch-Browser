package com.vortexwolf.chan.asynctasks.helpers;

import com.vortexwolf.chan.activities.PickBoardActivity;

import org.json.JSONObject;

public class ProcessReadBoardsRespondParams{
    public final JSONObject response;
    public final PickBoardActivity pickBoardActivity;

    public ProcessReadBoardsRespondParams(JSONObject response, PickBoardActivity pickBoardActivity) {
        this.response = response;
        this.pickBoardActivity = pickBoardActivity;
    }
}