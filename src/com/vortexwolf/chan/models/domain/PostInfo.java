package com.vortexwolf.chan.models.domain;

import java.io.Serializable;

public class PostInfo implements IAttachmentEntity, Serializable {

    private static final long serialVersionUID = 8212562227302607027L;

    protected String num;
    private String thumbnail;
    protected String comment;
    protected String subject;
    private String video;
    private String image;
    private int size;
    private String name;
    private int width;
    private int height;
    private long timestamp;
    private String parent;
    private String date;
    private String postername;

    public String getNum() {
        return this.num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Override
    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSubject() {
        if (this.subject == null) {
            return null;
        }
        
        this.subject = this.subject.replaceAll("&#44;", ",");
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String getVideo() {
        return this.video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    @Override
    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return this.width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostername() {
        return postername;
    }

    public void setPostername(String postername) {
        this.postername = postername;
    }
}
