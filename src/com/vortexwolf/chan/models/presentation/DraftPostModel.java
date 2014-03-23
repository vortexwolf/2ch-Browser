package com.vortexwolf.chan.models.presentation;

import android.graphics.Bitmap;

import com.vortexwolf.chan.models.domain.CaptchaEntity;

public class DraftPostModel {
    private String mComment;
    private ImageFileModel mAttachedFile;
    private boolean mIsSage;
    private CaptchaViewType mCaptchaType;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;
    private boolean mCaptchaPasscodeSuccess;
    private boolean mCaptchaPasscodeFail;

    public DraftPostModel(String comment, ImageFileModel attachedFile, boolean isSage, CaptchaViewType captchaType, CaptchaEntity captcha, Bitmap captchaImage, boolean isCaptchaPasscodeSuccess, boolean isCaptchaPasscodeFail) {
        this.mComment = comment;
        this.mAttachedFile = attachedFile;
        this.mIsSage = isSage;
        this.mCaptchaType = captchaType;
        this.mCaptcha = captcha;
        this.mCaptchaImage = captchaImage;
        this.mCaptchaPasscodeSuccess = isCaptchaPasscodeSuccess;
        this.mCaptchaPasscodeFail = isCaptchaPasscodeFail;
    }

    public String getComment() {
        return this.mComment;
    }

    public ImageFileModel getAttachedFile() {
        return this.mAttachedFile;
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
    
    public boolean isCaptchaPasscodeSuccess() {
        return this.mCaptchaPasscodeSuccess;
    }

    public boolean isCaptchaPasscodeFail() {
        return this.mCaptchaPasscodeFail;
    }
}
