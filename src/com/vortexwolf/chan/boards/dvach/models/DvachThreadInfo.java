package com.vortexwolf.chan.boards.dvach.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class DvachThreadInfo {
    @JsonProperty("reply_count")
    public int replyCount;
    @JsonProperty("image_count")
    public int imageCount;
    @JsonProperty("posts")
    public DvachPostInfo[][] posts;
}
