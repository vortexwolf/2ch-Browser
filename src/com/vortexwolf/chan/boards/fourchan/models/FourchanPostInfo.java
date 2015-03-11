package com.vortexwolf.chan.boards.fourchan.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class FourchanPostInfo {
    @JsonProperty("no")
    public long number;

    @JsonProperty("resto")
    public String parent;

    @JsonProperty("time")
    public long timestamp;

    @JsonProperty("name")
    public String name;

    @JsonProperty("trip")
    public String trip;

    @JsonProperty("sub")
    public String subject;

    @JsonProperty("com")
    public String comment;

    @JsonProperty("replies")
    public int postsCount;

    @JsonProperty("images")
    public int filesCount;

    @JsonProperty("tim")
    public String renamedFileName;

    @JsonProperty("ext")
    public String fileExtension;

    @JsonProperty("fsize")
    public int fileSize;

    @JsonProperty("w")
    public int fileWidth;

    @JsonProperty("h")
    public int fileHeight;
}
