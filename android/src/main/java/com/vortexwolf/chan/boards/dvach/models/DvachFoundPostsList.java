package com.vortexwolf.chan.boards.dvach.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class DvachFoundPostsList {
    @JsonProperty("query")
    public String query;

    @JsonProperty("posts")
    public DvachPostInfo[] posts;

    @JsonProperty("error_text")
    public String errorText;
}
