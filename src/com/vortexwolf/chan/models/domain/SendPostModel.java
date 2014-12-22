package com.vortexwolf.chan.models.domain;

import java.io.File;
import java.util.List;

import com.vortexwolf.chan.services.Recaptcha2;

public class SendPostModel {
    private Recaptcha2 mRecaptcha = null;
    private String mCaptchaKey;
    private String mCaptchaAnswer;
    private String mComment;
    private boolean mIsSage;
    private List<File> mAttachedFiles;
    private String mSubject;
    private String mPolitics;
    private String mName;
    private String mVideo;
    private String mParentThread;

    public SendPostModel(String captchaKey, String captchaAnswer, String comment, boolean isSage, List<File> attachedFiles, String subject, String politics, String name) {
        this.mCaptchaKey = captchaKey;
        this.mCaptchaAnswer = captchaAnswer;
        this.mComment = comment;
        this.mIsSage = isSage;
        this.mAttachedFiles = attachedFiles;
        this.mSubject = subject;
        this.mPolitics = politics;
        this.mName = name;
    }
    
    public SendPostModel(Recaptcha2 recaptcha, String captchaAnswer, String comment, boolean isSage, List<File> attachedFiles, String subject, String politics, String name) {
        this((String)null, captchaAnswer, comment, isSage, attachedFiles, subject, politics, name);
        this.mRecaptcha = recaptcha;
    }
    
    public boolean isRecaptcha() {
        return this.mRecaptcha != null;
    }
    
    public void setRecaptcha(Recaptcha2 recaptcha) {
        this.mRecaptcha = recaptcha;
    }
    
    public Recaptcha2 getRecaptcha() {
        return this.mRecaptcha;
    }
    
    public void setCaptchaKey(String captchaKey) {
        this.mCaptchaKey = captchaKey;
    }

    public String getCaptchaKey() {
        return this.mCaptchaKey;
    }

    public void setCaptchaAnswer(String captchaAnswer) {
        this.mCaptchaAnswer = captchaAnswer;
    }

    public String getCaptchaAnswer() {
        return this.mCaptchaAnswer;
    }

    public void setComment(String comment) {
        this.mComment = comment;
    }

    public String getComment() {
        return this.mComment;
    }

    public void setSage(boolean isSage) {
        this.mIsSage = isSage;
    }

    public boolean isSage() {
        return this.mIsSage;
    }

    public List<File> getAttachedFiles() {
        return this.mAttachedFiles;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getSubject() {
        return this.mSubject;
    }

    public String getPolitics() {
        return this.mPolitics;
    }

    public void setPolitics(String politics) {
        this.mPolitics = politics;
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getParentThread() {
        return mParentThread;
    }

    public void setParentThread(String parentThread) {
        this.mParentThread = parentThread;
    }
}
