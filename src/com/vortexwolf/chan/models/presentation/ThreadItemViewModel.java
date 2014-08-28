package com.vortexwolf.chan.models.presentation;

import android.content.res.Resources.Theme;
import android.text.SpannableStringBuilder;

import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.models.DvachPostInfo;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadInfo;
import com.vortexwolf.chan.common.utils.HtmlUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.domain.ThreadModel;

public class ThreadItemViewModel {

    private final Theme mTheme;
    private final PostModel mOpPost;
    private final int mReplyCount;
    private final int mImageCount;
    private final DvachUriBuilder mDvachUriBuilder;

    private final SpannableStringBuilder mSpannedComment;
    private AttachmentInfo[] mAttachments = new AttachmentInfo[4];  //4 - максимальное число аттачей к посту на макабе
    private boolean mEllipsized = false;
    private boolean mHidden = false;

    public ThreadItemViewModel(ThreadModel model, Theme theme, DvachUriBuilder dvachUriBuilder) {
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

    public String getSubjectOrText() {
        String subject = this.mOpPost.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            return subject;
        }

        return StringUtils.cutIfLonger(StringUtils.emptyIfNull(this.getSpannedComment()), 50) + "...";
    }

    public boolean hasAttachment() {
        return ThreadPostUtils.hasAttachment(this.mOpPost);
    }
  
    public int getAttachmentsNumber() {
        return this.mOpPost.getAttachments().size();
    }

    public AttachmentInfo getAttachment(String boardCode, int attachmentNumber) {
        if (this.mAttachments[attachmentNumber] == null && this.getAttachmentsNumber() > attachmentNumber) {
            this.mAttachments[attachmentNumber] = new AttachmentInfo(this.mOpPost.getAttachments().get(attachmentNumber), boardCode, this.mDvachUriBuilder);
        }

        return this.mAttachments[attachmentNumber];
    }

    public PostModel getOpPost() {
        return this.mOpPost;
    }

    public String getNumber() {
        return this.mOpPost.getNumber();
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
