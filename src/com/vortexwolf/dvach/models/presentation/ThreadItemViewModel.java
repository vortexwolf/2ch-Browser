package com.vortexwolf.dvach.models.presentation;

import android.content.res.Resources.Theme;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.ThreadInfo;

public class ThreadItemViewModel {

	private final Theme mTheme;
	private final PostInfo mOpPost;
	private final int mReplyCount;
	private final int mImageCount;
	
	private SpannableStringBuilder mSpannedComment = null;
	private Spanned mSpannedSubject = null;
	private AttachmentInfo mAttachment = null;
	private boolean mEllipsized = false;
	
	public ThreadItemViewModel(ThreadInfo model, Theme theme) {
		this.mTheme = theme;
		
		this.mOpPost = model.getPosts()[0];
		this.mReplyCount = model.getReplyCount();
		this.mImageCount = model.getImageCount();
	}

	public SpannableStringBuilder getSpannedComment() {
		if(this.mSpannedComment == null){
			String fixedComment = HtmlUtils.fixHtmlTags(this.mOpPost.getComment());
			Spanned spanned = HtmlUtils.createSpannedFromHtml(fixedComment, this.mTheme);
			this.mSpannedComment = (SpannableStringBuilder)spanned;
		}
		
		return mSpannedComment;
	}
	
	public Spanned getSpannedSubject() {
		if(this.mSpannedSubject == null){
			String subject = this.mOpPost.getSubject();
			Spanned spanned = !StringUtils.isEmpty(subject) ? Html.fromHtml(subject) : null;
			this.mSpannedSubject = spanned;
		}
		
		return mSpannedSubject;
	}
	
	public boolean hasAttachment(){
		return ThreadPostUtils.hasAttachment(this.mOpPost);
	}
	
	public AttachmentInfo getAttachment(String boardCode){
		if(this.mAttachment == null && this.hasAttachment()) {
			this.mAttachment = new AttachmentInfo(this.mOpPost, boardCode);
		}
		
		return mAttachment;
	}
	
	public PostInfo getOpPost(){
		return this.mOpPost;
	}
	
	public String getNumber(){
		return this.mOpPost.getNum();
	}
	
	public int getReplyCount(){
		return mReplyCount;
	}
	
	public int getImageCount(){
		return mImageCount;
	}

	public void setEllipsized(boolean ellipsized) {
		this.mEllipsized = ellipsized;
	}

	public boolean isEllipsized() {
		return mEllipsized;
	}
}
