package com.vortexwolf.dvach.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class BoardSettings{

	@JsonProperty("query_interval")
	private int queryInterval;
	@JsonProperty("query_limit")
	private int queryLimit;
	@JsonProperty("ban_time")
	private int banTime;
	@JsonProperty("postfields")
	private PostFields postFields;

	public int getQueryInterval() {
		return queryInterval;
	}
	public void setQueryInterval(int queryInterval) {
		this.queryInterval = queryInterval;
	}

	public int getQueryLimit() {
		return queryLimit;
	}
	public void setQueryLimit(int queryLimit) {
		this.queryLimit = queryLimit;
	}

	public int getBanTime() {
		return banTime;
	}
	public void setBanTime(int banTime) {
		this.banTime = banTime;
	}

	public PostFields getPostFields() {
		return postFields;
	}
	public void setPostFields(PostFields postFields) {
		this.postFields = postFields;
	}
}
