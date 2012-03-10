package com.vortexwolf.dvach.activities.addpost;

import java.io.File;

public class PostEntity {
	private String captchaKey;
	private String captchaAnswer;
	private String comment;
	private boolean isSage;
	private File attachment;
	
	public PostEntity(String captchaKey, String captchaAnswer, String comment){
		this(captchaKey, captchaAnswer, comment, false, null);
	}
	public PostEntity(String captchaKey, String captchaAnswer, String comment, boolean isSage, File attachment){
		this.captchaKey = captchaKey;
		this.captchaAnswer = captchaAnswer;
		this.comment = comment;
		this.isSage = isSage;
		this.setAttachment(attachment);
	}
	
	public void setCaptchaKey(String captchaKey) {
		this.captchaKey = captchaKey;
	}
	public String getCaptchaKey() {
		return captchaKey;
	}
	public void setCaptchaAnswer(String captchaAnswer) {
		this.captchaAnswer = captchaAnswer;
	}
	public String getCaptchaAnswer() {
		return captchaAnswer;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}
	public void setSage(boolean isSage) {
		this.isSage = isSage;
	}
	public boolean isSage() {
		return isSage;
	}
	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}
	public File getAttachment() {
		return attachment;
	}
}
