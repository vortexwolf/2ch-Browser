package com.vortexwolf.dvach.models.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class FoundPostsList {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("posts")
    private PostInfo[] posts;
    
    @JsonProperty("error_text")
    private String errorText; 

    public PostInfo[] getPosts() {
        return posts;
    }

    public void setPosts(PostInfo[] posts) {
        this.posts = posts;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
