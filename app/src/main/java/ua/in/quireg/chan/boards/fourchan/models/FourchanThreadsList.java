package ua.in.quireg.chan.boards.fourchan.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class FourchanThreadsList {
    @JsonProperty("threads")
    public FourchanThreadInfo[] threads;
}
