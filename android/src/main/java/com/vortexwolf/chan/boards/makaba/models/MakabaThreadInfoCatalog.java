package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadInfoCatalog extends MakabaPostInfo {
    @JsonProperty("posts_count")
    public int postsCount;
    
    @JsonProperty("images_count")
    public int filesCount;
}
