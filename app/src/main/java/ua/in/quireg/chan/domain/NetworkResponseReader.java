package ua.in.quireg.chan.domain;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;
import ua.in.quireg.chan.models.domain.BoardModel;

/**
 * Date 12/1/2017.
 *
 * @author Artur Menchenko
 */

public interface NetworkResponseReader {

    List<BoardModel> parseBoardsListResponse(Response response) throws IOException;

}
