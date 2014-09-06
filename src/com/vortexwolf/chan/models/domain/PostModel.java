package com.vortexwolf.chan.models.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostModel implements Serializable {
    private static final long serialVersionUID = 3897934462057089443L;
    
    private String number;
    private String name;
    private String subject;
    private String comment;
    private String email;
    private String trip;
    private String icon;
    private boolean op;
    private long timestamp;
    private String parentThread;
    private ArrayList<AttachmentModel> attachments = new ArrayList<AttachmentModel>();
    
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getTrip() {
        return trip;
    }
    public void setTrip(String trip) {
        this.trip = trip;
    }
    public boolean isOp() {
        return op;
    }
    public void setOp(boolean op) {
        this.op = op;
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
    
    public void addAttachment(AttachmentModel model) {
        this.attachments.add(model);
    }
    public List<AttachmentModel> getAttachments(){
        return attachments;
    }
}
