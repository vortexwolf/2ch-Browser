package com.vortexwolf.chan.models.domain;

import java.io.Serializable;

public class PostModel implements IAttachmentEntity, Serializable {
    private static final long serialVersionUID = 3897934462057089443L;
    
    private String number;
    private String name;
    private String subject;
    private String comment;
    private String thumbnailUrl;
    private String videoUrl;
    private String imageUrl;
    private int imageSize;
    private int imageWidth;
    private int imageHeight;
    private long timestamp;
    private String parentThread;
    
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getVideoUrl() {
        return videoUrl;
    }
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getParentThread() {
        return parentThread;
    }
    public void setParentThread(String parentThread) {
        this.parentThread = parentThread;
    }
}
