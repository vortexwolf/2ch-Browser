package ua.in.quireg.chan.domain;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.boards.makaba.models.MakabaThreadsList;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 9:26 AM.
 * 2ch-Browser
 */

public class ApiReaderImpl implements ApiReader {

    private ObjectMapper mMapper = new ObjectMapper()
            .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public List<BoardModel> readBoardsListResponse(Response response) throws IOException {
        ArrayList<BoardModel> models = new ArrayList<>();

        if (!response.isSuccessful()) {
            Timber.e("Response was not successful");
            response.close();
            return models;
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            Timber.e("responseBody is null");
            return models;
        }

        JsonNode result = mMapper.readValue(responseBody.string(), JsonNode.class);
        response.close();

        Iterator iterator = result.getElements();

        while (iterator.hasNext()) {
            JsonNode node = (JsonNode) iterator.next();

            MakabaBoardInfo[] data = mMapper.convertValue(node, MakabaBoardInfo[].class);

            models.addAll(Arrays.asList(MakabaModelsMapper.mapBoardModels(data)));
        }
        return models;
    }

    @Override
    public List<ThreadModel> readThreadsListResponse(Response response) throws IOException {
        ArrayList<ThreadModel> models = new ArrayList<>();

        if (!response.isSuccessful()) {
            Timber.e("Response was not successful");
            response.close();
            return models;
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            Timber.e("responseBody is null");
            return models;
        }

//        JsonNode result = mMapper.readValue(responseBody.string(), JsonNode.class);
//        response.close();
//
//        Iterator iterator = result.getElements();
//
//        while (iterator.hasNext()) {
//            JsonNode node = (JsonNode) iterator.next();
//
//            MakabaThreadsList data = mMapper.convertValue(node, MakabaThreadsList.class);
//
//            models.addAll(Arrays.asList(MakabaModelsMapper.mapThreadModels(data)));
//        }


        JsonNode result = mMapper.readValue(responseBody.string(), JsonNode.class);
        response.close();

        MakabaThreadsList data = mMapper.convertValue(result, MakabaThreadsList.class);

        models.addAll(Arrays.asList(MakabaModelsMapper.mapThreadModels(data)));

        return models;
    }
}
