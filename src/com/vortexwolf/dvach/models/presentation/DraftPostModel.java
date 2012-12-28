package com.vortexwolf.dvach.models.presentation;

import android.graphics.Bitmap;

import com.vortexwolf.dvach.models.domain.CaptchaEntity;

public class DraftPostModel {
    private String mComment;
    private ImageFileModel mAttachedFile;
    private boolean mIsSage;
    private CaptchaViewType mCaptchaType;
    private CaptchaEntity mCaptcha;
    private Bitmap mCaptchaImage;

    public DraftPostModel(String comment, ImageFileModel attachedFile, boolean isSage, CaptchaViewType captchaType, CaptchaEntity captcha, Bitmap captchaImage) {
        this.mComment = comment;
        this.mAttachedFile = attachedFile;
        this.mIsSage = isSage;
        this.mCaptchaType = captchaType;
        this.mCaptcha = captcha;
        this.mCaptchaImage = captchaImage;
    }

    public String getComment() {
        return mComment;
    }

    public ImageFileModel getAttachedFile() {
        return mAttachedFile;
    }

    public boolean isSage() {
        return mIsSage;
    }

    public CaptchaViewType getCaptchaType() {
        return mCaptchaType;
    }

    public CaptchaEntity getCaptcha() {
        return mCaptcha;
    }

    public Bitmap getCaptchaImage() {
        return mCaptchaImage;
    }
}
