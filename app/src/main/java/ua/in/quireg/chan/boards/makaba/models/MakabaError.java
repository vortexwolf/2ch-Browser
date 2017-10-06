package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaError {
    @JsonProperty("Code")
    public int code;
    @JsonProperty("Error")
    public String error;
}