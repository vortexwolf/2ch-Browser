package com.vortexwolf.dvach.models.domain;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class PostsList {

    @JsonProperty("thread")
    private PostInfo[][] thread;

    @JsonIgnore
    public PostInfo[] getThread() {
        PostInfo[] newThread = new PostInfo[this.thread.length];
        for (int i = 0; i < this.thread.length; i++) {
            newThread[i] = this.thread[i][0];
        }
        return newThread;
    }
}
