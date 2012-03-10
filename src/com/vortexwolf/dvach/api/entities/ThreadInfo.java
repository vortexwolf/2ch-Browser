package com.vortexwolf.dvach.api.entities;

import org.codehaus.jackson.annotate.JsonProperty;

public class ThreadInfo {
	@JsonProperty("reply_count")
	private int replyCount;
	@JsonProperty("image_count")
	private int imageCount;
	@JsonProperty("posts")
	private PostInfo[][] posts;

	public int getReplyCount() {
		return replyCount;
	}

	public int getImageCount() {
		return imageCount;
	}
	
	public PostInfo[] getPosts() {
		return posts[0];
	}
}
