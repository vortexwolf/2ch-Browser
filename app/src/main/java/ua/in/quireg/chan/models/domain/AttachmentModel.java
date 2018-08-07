package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "attachmentModel")
public class AttachmentModel implements Serializable {

    private static final long serialVersionUID = 2679952635069517972L;

    @PrimaryKey
    @ColumnInfo(name = "thumbnailUrl")
    @NonNull
    private String thumbnailUrl;
    @ColumnInfo(name = "path")
    private String path;
    @ColumnInfo(name = "imageSize")
    private int imageSize;
    @ColumnInfo(name = "imageWidth")
    private int imageWidth;
    @ColumnInfo(name = "imageHeight")
    private int imageHeight;
    @ColumnInfo(name = "postId")
    private String postId;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int getImageSize() {
        return imageSize;
    }
    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }
    public int getImageWidth() {
        return imageWidth;
    }
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }
    public int getImageHeight() {
        return imageHeight;
    }
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
    public String getPostId() {
        return postId;
    }
    public void setPostId(String parentid) {
        this.postId = parentid;
    }
}
