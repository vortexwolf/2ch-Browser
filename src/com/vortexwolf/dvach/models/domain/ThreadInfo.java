package com.vortexwolf.dvach.models.domain;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

public class ThreadInfo implements Serializable {
    private static final long serialVersionUID = -2097742770262868291L;

    @JsonProperty("reply_count")
    private int replyCount;
    @JsonProperty("image_count")
    private int imageCount;
    @JsonProperty("posts")
    private PostInfo[][] posts;

    public int getReplyCount() {
        return this.replyCount;
    }

    public int getImageCount() {
        return this.imageCount;
    }

    public PostInfo[] getPosts() {
        return this.posts[0];
    }
}
