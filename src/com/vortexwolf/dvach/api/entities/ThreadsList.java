package com.vortexwolf.dvach.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class ThreadsList {

	@JsonProperty("threads")
	private ThreadInfo[] threads;

	public ThreadInfo[] getThreads() {
		return threads;
	}
	public void setThreads(ThreadInfo[] threads) {
		this.threads = threads;
	}
}
