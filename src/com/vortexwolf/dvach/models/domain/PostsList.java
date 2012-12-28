package com.vortexwolf.dvach.models.domain;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class PostsList {

    @JsonProperty("thread")
    private PostInfo[][] thread;

    @JsonIgnore
    public PostInfo[] getThread() {
        PostInfo[] newThread = new PostInfo[thread.length];
        for (int i = 0; i < thread.length; i++)
            newThread[i] = thread[i][0];
        return newThread;
    }
}
