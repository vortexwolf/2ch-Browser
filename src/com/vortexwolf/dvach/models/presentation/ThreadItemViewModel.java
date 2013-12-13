package com.vortexwolf.dvach.models.presentation;

import android.content.res.Resources.Theme;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.domain.ThreadInfo;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class ThreadItemViewModel {

    private final Theme mTheme;
    private final PostInfo mOpPost;
    private final int mReplyCount;
    private final int mImageCount;
    private final DvachUriBuilder mDvachUriBuilder;

    private final SpannableStringBuilder mSpannedComment;
    private AttachmentInfo mAttachment = null;
    private boolean mEllipsized = false;
    private boolean mHidden = false;

    public ThreadItemViewModel(ThreadInfo model, Theme theme, DvachUriBuilder dvachUriBuilder) {
        this.mTheme = theme;
        this.mDvachUriBuilder = dvachUriBuilder;

        this.mOpPost = model.getPosts()[0];
        this.mReplyCount = model.getReplyCount();
        this.mImageCount = model.getImageCount();
        
        this.mSpannedComment = this.createSpannedComment();
    }

    public SpannableStringBuilder getSpannedComment() {
        return this.mSpannedComment;
    }
    
    private SpannableStringBuilder createSpannedComment() {
        String fixedComment = HtmlUtils.fixHtmlTags(this.mOpPost.getComment());
        SpannableStringBuilder spanned = HtmlUtils.createSpannedFromHtml(fixedComment, this.mTheme);
        return spanned;
    }

    public String getSubject() {
        return StringUtils.emptyIfNull(this.mOpPost.getSubject());
    }
    
    public String getSubjectOrText(){
        String subject = this.mOpPost.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            return subject;
        }
        
        return StringUtils.cutIfLonger(StringUtils.emptyIfNull(this.getSpannedComment()), 50) + "...";
    }

    public boolean hasAttachment() {
        return ThreadPostUtils.hasAttachment(this.mOpPost);
    }

    public AttachmentInfo getAttachment(String boardCode) {
        if (this.mAttachment == null && this.hasAttachment()) {
            this.mAttachment = new AttachmentInfo(this.mOpPost, boardCode, this.mDvachUriBuilder);
        }

        return this.mAttachment;
    }

    public PostInfo getOpPost() {
        return this.mOpPost;
    }

    public String getNumber() {
        return this.mOpPost.getNum();
    }

    public int getReplyCount() {
        return this.mReplyCount;
    }

    public int getImageCount() {
        return this.mImageCount;
    }

    public void setEllipsized(boolean ellipsized) {
        this.mEllipsized = ellipsized;
    }

    public boolean isEllipsized() {
        return this.mEllipsized;
    }

    public boolean isHidden() {
        return this.mHidden;
    }

    public void setHidden(boolean hidden) {
        this.mHidden = hidden;
    }
}
