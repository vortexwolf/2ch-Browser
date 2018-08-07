package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "badgeModel")
public class BadgeModel {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "source")
    public String source;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "postId")
    public String postId;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
