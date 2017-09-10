package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaFoundPostsList {
    @JsonProperty("Board")
    public String board;

    @JsonProperty("posts")
    public MakabaPostInfo[] posts;

    @JsonProperty("search")
    public String search;
}
