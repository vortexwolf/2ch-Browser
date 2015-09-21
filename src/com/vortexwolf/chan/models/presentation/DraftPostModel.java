package com.vortexwolf.chan.models.presentation;

import java.util.List;

import android.graphics.Bitmap;

import com.vortexwolf.chan.models.domain.CaptchaEntity;

public class DraftPostModel {
    private String mComment;
    private List<FileModel> mAttachedFiles;
    private boolean mIsSage;
    private CaptchaViewType mCaptchaType;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private CaptchaInfoType mCaptchaInfoType;

    public DraftPostModel(String comment, List<FileModel> attachedFiles, boolean isSage, CaptchaViewType captchaType, CaptchaEntity captcha, Bitmap captchaImage, CaptchaInfoType captchaInfoType) {
        this.mComment = comment;
        this.mAttachedFiles = attachedFiles;
        this.mIsSage = isSage;
        this.mCaptchaType = captchaType;
        this.mCaptcha = captcha;
        this.mCaptchaImage = captchaImage;
        this.mCaptchaInfoType = captchaInfoType;
    }

    public String getComment() {
        return this.mComment;
    }

    public List<FileModel> getAttachedFiles() {
        return this.mAttachedFiles;
    }

    public boolean isSage() {
        return this.mIsSage;
    }

    public CaptchaViewType getCaptchaType() {
        return this.mCaptchaType;
    }

    public CaptchaEntity getCaptcha() {
        return this.mCaptcha;
    }

    public Bitmap getCaptchaImage() {
        return this.mCaptchaImage;
    }

    public CaptchaInfoType getCaptchaInfoType() {
        return mCaptchaInfoType;
    }
}
