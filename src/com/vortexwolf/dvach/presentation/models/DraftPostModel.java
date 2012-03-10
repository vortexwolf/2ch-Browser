package com.vortexwolf.dvach.presentation.models;

import android.graphics.Bitmap;

import com.vortexwolf.dvach.activities.files.SerializableFileModel;
import com.vortexwolf.dvach.api.entities.CaptchaEntity;

public class DraftPostModel {
	private String mComment;
	private ImageFileModel mAttachedFile;
	private boolean mIsSage;
	private CaptchaViewType mCaptchaType;
	private CaptchaEntity mCaptcha;
	private Bitmap mCaptchaImage;
	
	public DraftPostModel(String comment, ImageFileModel attachedFile, boolean isSage, CaptchaViewType captchaType, CaptchaEntity captcha, Bitmap captchaImage){
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
