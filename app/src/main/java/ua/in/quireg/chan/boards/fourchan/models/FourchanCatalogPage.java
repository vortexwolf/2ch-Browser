package ua.in.quireg.chan.boards.fourchan.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class FourchanCatalogPage {
    @JsonProperty("page")
    public int page;

    @JsonProperty("threads")
    public FourchanCatalogThread[] threads;
}
