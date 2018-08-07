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
        mWebsite = website;
        mBoardName = boardName;
        mTheme = theme;

        mUrlBuilder = mWebsite.getUrlBuilder();
        mOpPost = model.getPosts()[0];
        mReplyCount = model.getReplyCount();
        mImageCount = model.getImageCount();

        mSpannedComment = createSpannedComment();
    }

    public IWebsite getWebsite() {
        return mWebsite;
    }

    public String getBoardName() {
        return mBoardName;
    }

    public SpannableStringBuilder getSpannedComment() {
        return mSpannedComment;
    }

    private SpannableStringBuilder createSpannedComment() {
        String fixedComment = HtmlUtils.fixHtmlTags(mOpPost.getComment());
        SpannableStringBuilder spanned = HtmlUtils.createSpannedFromHtml(fixedComment, mTheme, mUrlBuilder);
        HtmlUtils.replaceUrls(spanned, null, mTheme);

        return spanned;
    }

    public String getSubject() {
        return StringUtils.emptyIfNull(mOpPost.getSubject());
    }

    public String getSubjectOrText() {
        String subject = mOpPost.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            return subject;
        }

        return StringUtils.cutIfLonger(StringUtils.emptyIfNull(getSpannedComment()), 50) + "...";
    }

    public boolean hasAttachment() {
        return ThreadPostUtils.hasAttachment(mOpPost);
    }

    public int getAttachmentsNumber() {
        return ThreadPostUtils.getAttachmentsNumber(mOpPost);
    }

    public AttachmentInfo getAttachment(int index) {
        if (index >= getAttachmentsNumber()) {
            return null;
        }

        if (mAttachments[index] == null) {
            mAttachments[index] = new AttachmentInfo(mOpPost.getAttachments().get(index), mWebsite, mBoardName, mOpPost.getNumber());
        }

        return mAttachments[index];
    }

    public PostModel getOpPost() {
        return mOpPost;
    }

    public String getNumber() {
        return mOpPost.getNumber();
    }

    public int getReplyCount() {
        return mReplyCount;
    }

    public int getImageCount() {
        return mImageCount;
    }

    public void setEllipsized(boolean ellipsized) {
        mEllipsized = ellipsized;
    }

    public boolean isEllipsized() {
        return mEllipsized;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public void setHidden(boolean hidden) {
        mHidden = hidden;
    }

    @Override
    public Type getType() {
        if (mHidden) {
            return Type.THREAD;
        } else {
            return Type.HIDDEN_THREAD;
        }
    }
}
