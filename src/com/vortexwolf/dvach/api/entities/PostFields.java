package com.vortexwolf.dvach.api.entities;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class PostFields {
	@JsonProperty("captcha_key")
	private String captchaKey;
	private String video;
	@JsonProperty("nofile")
	private String noFile;
	private String subject;
	private String submit;
	private String file;
	private String name;
	private String task;
	private String captcha;
	private String email;
	private String comment;
	
	public String getCaptchaKey() {
		return captchaKey;
	}
	public void setCaptchaKey(String captchaKey) {
		this.captchaKey = captchaKey;
	}
	public String getVideo() {
		return video;
	}
	public void setVideo(String video) {
		this.video = video;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getNoFile() {
		return noFile;
	}
	public void setNoFile(String noFile) {
		this.noFile = noFile;
	}
	public String getSubmit() {
		return submit;
	}
	public void setSubmit(String submit) {
		this.submit = submit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getCaptcha() {
		return captcha;
	}
	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@JsonIgnore
	public static PostFields getDefault(){
		PostFields defPF = new PostFields();
		defPF.setCaptcha("recaptcha_response_field");
		defPF.setCaptchaKey("recaptcha_challenge_field");
		defPF.setComment("shampoo");
		defPF.setEmail("nabiki");
		defPF.setFile("file");
		defPF.setName("akane");
		defPF.setNoFile("nofile");
		defPF.setSubject("kasumi");
		defPF.setSubmit("submit");
		defPF.setTask("post");
		defPF.setVideo("video");

		return defPF;
	}
}
