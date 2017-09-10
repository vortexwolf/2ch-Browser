package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadsListCatalog {
    @JsonProperty("threads")
    public MakabaThreadInfoCatalog[] threads;
}
