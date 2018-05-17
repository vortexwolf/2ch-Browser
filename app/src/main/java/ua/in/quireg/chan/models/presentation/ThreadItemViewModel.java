package ua.in.quireg.chan.models.presentation;

import android.content.res.Resources.Theme;
import android.text.SpannableStringBuilder;

import ua.in.quireg.chan.common.utils.HtmlUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.domain.ThreadModel;

public class ThreadItemViewModel implements IThreadListEntity {
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final Theme mTheme;
    private final PostModel mOpPost;
    private final int mReplyCount;
    private final int mImageCount;

    private final SpannableStringBuilder mSpannedComment;
    private AttachmentInfo[] mAttachments = new AttachmentInfo[4];
    private boolean mEllipsized = false;
    private boolean mHidden = false;
    private IUrlBuilder mUrlBuilder;

    public ThreadItemViewModel(IWebsite website, String boardName, ThreadModel model, Theme theme) {
        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mTheme = theme;

        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
        this.mOpPost = model.getPosts()[0];
        this.mReplyCount = model.getReplyCount();
        this.mImageCount = model.getImageCount();

        this.mSpannedComment = this.createSpannedComment();
    }

    public IWebsite getWebsite() {
        return mWebsite;
    }

    public String getBoardName() {
        return mBoardName;
    }

    public SpannableStringBuilder getSpannedComment() {
        return this.mSpannedComment;
    }

    private SpannableStringBuilder createSpannedComment() {
        String fixedComment = HtmlUtils.fixHtmlTags(this.mOpPost.getComment());
        SpannableStringBuilder spanned = HtmlUtils.createSpannedFromHtml(fixedComment, this.mTheme, this.mUrlBuilder);
        HtmlUtils.replaceUrls(spanned, null, this.mTheme);

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
        return ThreadPostUtils.getAttachmentsNumber(this.mOpPost);
    }

    public AttachmentInfo getAttachment(int index) {
        if(index >= this.getAttachmentsNumber()) {
            return null;
        }

        if (this.mAttachments[index] == null) {
            this.mAttachments[index] = new AttachmentInfo(this.mOpPost.getAttachments().get(index), this.mWebsite, this.mBoardName, this.mOpPost.getNumber());
        }

        return this.mAttachments[index];
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

    @Override
    public Type getType() {
        if(mHidden) {
            return Type.THREAD;
        } else  {
            return Type.HIDDEN_THREAD;
        }
    }
}
