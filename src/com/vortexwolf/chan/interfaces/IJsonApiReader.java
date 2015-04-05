package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.SearchPostListModel;
import com.vortexwolf.chan.models.domain.ThreadModel;

public interface IJsonApiReader {
    ThreadModel[] readCatalog(String boardName, int filter, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    PostModel[] readPostsList(String boardName, String threadNumber, int fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;
}
