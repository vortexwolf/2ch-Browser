package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class MakabaIconInfo implements Serializable{
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("num")
    public int num;

    @JsonProperty("url")
    public String url;
}
