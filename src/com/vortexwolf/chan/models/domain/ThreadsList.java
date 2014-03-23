package com.vortexwolf.chan.models.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class ThreadsList {

    @JsonProperty("threads")
    private ThreadInfo[] threads;

    public ThreadInfo[] getThreads() {
        return this.threads;
    }

    public void setThreads(ThreadInfo[] threads) {
        this.threads = threads;
    }
}
