package ua.in.quireg.chan.domain;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 9:26 AM.
 * 2ch-Browser
 */

public interface ApiReader {

    List<BoardModel> readBoardsListResponse(Response response) throws IOException;

}
