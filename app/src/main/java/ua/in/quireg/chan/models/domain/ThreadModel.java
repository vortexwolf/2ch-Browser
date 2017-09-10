package ua.in.quireg.chan.models.domain;

import java.io.Serializable;

public class ThreadModel implements Serializable {
    private static final long serialVersionUID = -3797904320330675845L;
    
    private int replyCount;
    private int imageCount;
    private PostModel[] posts;
    
    public int getReplyCount() {
        return replyCount;
    }
    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }
    public int getImageCount() {
        return imageCount;
    }
    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
    public PostModel[] getPosts() {
        return posts;
    }
    public void setPosts(PostModel[] posts) {
        this.posts = posts;
    }
}
