package ua.in.quireg.chan.interfaces;

import ua.in.quireg.chan.exceptions.HtmlNotJsonException;
import ua.in.quireg.chan.exceptions.JsonApiReaderException;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.SearchPostListModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

public interface IJsonApiReader {
    ThreadModel[] readCatalog(String boardName, int filter, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    PostModel[] readPostsList(String boardName, String threadNumber, int fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;
}
