package com.vortexwolf.chan.models.domain;

import java.io.File;
import java.util.List;

public class SendPostModel {
    private String mRecaptchaHash = null;
    private CaptchaEntity mCaptcha;
    private String mCaptchaAnswer;
    private String mComment;
    private boolean mIsSage;
    private List<File> mAttachedFiles;
    private String mSubject;
    private String mPolitics;
    private String mName;
    private String mVideo;
    private String mParentThread;

    public SendPostModel(CaptchaEntity captcha, String captchaAnswer, String comment, boolean isSage, List<File> attachedFiles, String subject, String politics, String name) {
        this.mCaptcha = captcha;
        this.mCaptchaAnswer = captchaAnswer;
        this.mComment = comment;
        this.mIsSage = isSage;
        this.mAttachedFiles = attachedFiles;
        this.mSubject = subject;
        this.mPolitics = politics;
        this.mName = name;
    }

    public boolean isRecaptcha() {
        return this.mCaptcha != null
                && this.mCaptcha.getType() == CaptchaEntity.Type.RECAPTCHA_POST;
    }

    public void setRecaptchaHash(String hash) {
        this.mRecaptchaHash = hash;
    }

    public String getRecaptchaHash() {
        return this.mRecaptchaHash;
    }

    public void setCaptchaAnswer(String captchaAnswer) {
        this.mCaptchaAnswer = captchaAnswer;
    }

    public String getCaptchaKey() {
        return this.mCaptcha != null ? this.mCaptcha.getKey() : null;
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
