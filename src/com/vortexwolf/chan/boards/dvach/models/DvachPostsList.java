package com.vortexwolf.chan.boards.dvach.models;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class DvachPostsList {
    @JsonProperty("thread")
    private DvachPostInfo[][] thread;

    @JsonIgnore
    public DvachPostInfo[] getThread() {
        if (this.thread == null) {
            return new DvachPostInfo[0];
        }

        DvachPostInfo[] newThread = new DvachPostInfo[this.thread.length];
        for (int i = 0; i < this.thread.length; i++) {
            newThread[i] = this.thread[i][0];
        }
        return newThread;
    }
}
