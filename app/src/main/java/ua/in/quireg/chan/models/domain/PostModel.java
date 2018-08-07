package ua.in.quireg.chan.models.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "postModel")
public class PostModel implements Serializable {
    private static final long serialVersionUID = 3897934462057089443L;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uniqueid")
    private String uniqueid = UUID.randomUUID().toString();
    @ColumnInfo(name = "number")
    private String number;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "subject")
    private String subject;
    @ColumnInfo(name = "comment")
    private String comment;
    @ColumnInfo(name = "sage")
    private boolean sage;
    @ColumnInfo(name = "trip")
    private String trip;
    @Ignore
    private BadgeModel badge;
    @ColumnInfo(name = "op")
    private boolean op;
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    @ColumnInfo(name = "parentThread")
    private String parentThread;
    @Ignore
    private ArrayList<AttachmentModel> attachments = new ArrayList<AttachmentModel>();

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

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
    public boolean getSage() {
        return sage;
    }
    public void setSage(boolean sage) {
        this.sage = sage;
    }
    public String getTrip() {
        return trip;
    }
    public void setTrip(String trip) {
        this.trip = trip;
    }
    public BadgeModel getBadge() {
        return badge;
    }
    public void setBadge(BadgeModel icon) {
        if (icon != null) {
            this.badge = icon;
            this.badge.setPostId(uniqueid);
        }
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
        if (model != null) {
            model.setPostId(uniqueid);
            attachments.add(model);
        }
    }
    public List<AttachmentModel> getAttachments(){
        return attachments;
    }

}
