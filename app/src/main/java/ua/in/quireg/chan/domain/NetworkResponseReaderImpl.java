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
import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Date 12/1/2017.
 *
 * @author Artur Menchenko
 */

public class NetworkResponseReaderImpl implements NetworkResponseReader {

    public List<BoardModel> parseBoardsListResponse(Response response) throws IOException {
        ArrayList<BoardModel> models = new ArrayList<>();

        if (!response.isSuccessful()) {
            Timber.e("Response was not successful");
            response.close();
            return models;
        }

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            Timber.e("responseBody is null");
            return models;
        }

        JsonNode result = mapper.readValue(responseBody.string(), JsonNode.class);
        response.close();

        Iterator iterator = result.getElements();

        while (iterator.hasNext()) {
            JsonNode node = (JsonNode) iterator.next();

            MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);

            models.addAll(Arrays.asList(MakabaModelsMapper.mapBoardModels(data)));
        }
        return models;
    }
}
