package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "threadModel")
public class ThreadModel implements Serializable {
    private static final long serialVersionUID = -3797904320330675845L;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "board")
    private String board;
    @ColumnInfo(name = "replyCount")
    private int replyCount;
    @ColumnInfo(name = "imageCount")
    private int imageCount;

    @Ignore
    private PostModel[] posts;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBoard() {
        return board;
    }
    public void setBoard(String board) {
        this.board = board;
    }
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
        if (posts != null) {
            for (PostModel postModel : posts) {
                postModel.setParentThread(posts[0].getNumber());
            }
            id = posts[0].getNumber();
            this.posts = posts;
        }
    }

}
