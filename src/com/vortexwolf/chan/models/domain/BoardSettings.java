package com.vortexwolf.chan.models.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class BoardSettings {

    @JsonProperty("query_interval")
    private int queryInterval;
    @JsonProperty("query_limit")
    private int queryLimit;
    @JsonProperty("ban_time")
    private int banTime;
    @JsonProperty("postfields")
    private PostFields postFields;

    public int getQueryInterval() {
        return this.queryInterval;
    }

    public void setQueryInterval(int queryInterval) {
        this.queryInterval = queryInterval;
    }

    public int getQueryLimit() {
        return this.queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public int getBanTime() {
        return this.banTime;
    }

    public void setBanTime(int banTime) {
        this.banTime = banTime;
    }

    public PostFields getPostFields() {
        return this.postFields;
    }

    public void setPostFields(PostFields postFields) {
        this.postFields = postFields;
    }
}
