package ua.in.quireg.chan.models.domain;

public class SearchPostListModel {
    private PostModel[] posts;
    private String error;
    
    public PostModel[] getPosts() {
        return posts;
    }
    public void setPosts(PostModel[] posts) {
        this.posts = posts;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
}
