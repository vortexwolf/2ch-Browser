package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadInfo {
    @JsonProperty("thread_num")
    public int threadNumber;
    @JsonProperty("posts_count")
    public int postsCount;
    @JsonProperty("files_count")
    public int filesCount;
    @JsonProperty("posts")
    public MakabaPostInfo[] posts;
}
