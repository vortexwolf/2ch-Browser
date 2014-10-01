package com.vortexwolf.chan.models.domain;

import java.io.Serializable;

public class AttachmentModel implements Serializable {
    private static final long serialVersionUID = 2679952635069517972L;
    
    private String thumbnailUrl;
    private String path;
    private int imageSize;
    private int imageWidth;
    private int imageHeight;
    
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
}
