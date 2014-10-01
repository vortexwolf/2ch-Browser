package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaError {
    @JsonProperty("Code")
    public int code;
    @JsonProperty("Error")
    public String error;
}