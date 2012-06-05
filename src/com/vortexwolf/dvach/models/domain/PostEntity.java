package com.vortexwolf.dvach.models.domain;

import java.io.File;

public class PostEntity {
	private String mCaptchaKey;
	private String mCaptchaAnswer;
	private String mComment;
	private boolean mIsSage;
	private File mAttachment;
	private String mSubject;
	
	public PostEntity(String captchaKey, String captchaAnswer, String comment){
		this(captchaKey, captchaAnswer, comment, false, null, null);
	}
	public PostEntity(String captchaKey, String captchaAnswer, String comment, boolean isSage, File attachment, String subject){
		this.mCaptchaKey = captchaKey;
		this.mCaptchaAnswer = captchaAnswer;
		this.mComment = comment;
		this.mIsSage = isSage;
		this.mAttachment = attachment;
		this.mSubject = subject;
	}
	
	public void setCaptchaKey(String captchaKey) {
		this.mCaptchaKey = captchaKey;
	}
	public String getCaptchaKey() {
		return mCaptchaKey;
	}
	public void setCaptchaAnswer(String captchaAnswer) {
		this.mCaptchaAnswer = captchaAnswer;
	}
	public String getCaptchaAnswer() {
		return mCaptchaAnswer;
	}
	public void setComment(String comment) {
		this.mComment = comment;
	}
	public String getComment() {
		return mComment;
	}
	public void setSage(boolean isSage) {
		this.mIsSage = isSage;
	}
	public boolean isSage() {
		return mIsSage;
	}
	public void setAttachment(File attachment) {
		this.mAttachment = attachment;
	}
	public File getAttachment() {
		return mAttachment;
	}
	public void setSubject(String subject) {
		this.mSubject = subject;
	}
	public String getSubject() {
		return mSubject;
	}
}
