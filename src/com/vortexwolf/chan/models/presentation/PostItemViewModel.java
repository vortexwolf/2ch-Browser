package com.vortexwolf.chan.models.presentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.models.DvachPostInfo;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.HtmlUtils;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.services.presentation.FlowTextHelper;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class PostItemViewModel {
    private static final Pattern sReplyLinkFullPattern = Pattern.compile("<a.+?>(?:>>|&gt;&gt;)(\\d+)</a>");
    private static final Pattern sBadgePattern = Pattern.compile("<img.+?src=\"(.+?)\".+?title=\"(.+?)\".+?/>");
    private static final Pattern sFlagPattern = Pattern.compile("<img.+?src=\"(.+?)\".+?/>");

    private final String mBoardName;
    private final String mThreadNumber;
    private final int mPosition;
    private final PostModel mModel;
    private final Theme mTheme;
    private final IURLSpanClickListener mUrlListener;
    private final DvachUriBuilder mDvachUriBuilder = Factory.resolve(DvachUriBuilder.class);
    private final Resources mResources = Factory.resolve(Resources.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);

    private final SpannableStringBuilder mSpannedComment;
    private SpannableStringBuilder mCachedReferencesString = null;
    private AttachmentInfo[] mAttachments = new AttachmentInfo[4]; //4 - максимальное число аттачей к посту на макабе
    private String mPostDate = null;
    private final BadgeModel mBadge;
    private String mName = null;

    private final ArrayList<String> refersTo = new ArrayList<String>();
    private final ArrayList<String> referencesFrom = new ArrayList<String>();

    public boolean isFloatImageComment = false;
    private boolean mIsLocalDateTime = false;
    private boolean mHasUrlSpans = false;
    private boolean mIsLongTextExpanded = false;

    public PostItemViewModel(String boardName, String threadNumber, int position, PostModel model, Theme theme, IURLSpanClickListener listener) {
        this.mModel = model;
        this.mTheme = theme;
        this.mUrlListener = listener;
        this.mPosition = position;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;

        this.parseReferences();
        this.mBadge = this.parseBadge();
        this.mSpannedComment = this.createSpannedComment();
    }

    public String getSubjectOrText() {
        String subject = this.mModel.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            return subject;
        }

        return StringUtils.cutIfLonger(StringUtils.emptyIfNull(this.getSpannedComment()), 50);
    }

    private BadgeModel parseBadge() {
        this.mName = this.mModel.getName();
        if (this.mName == null) {
            return null;
        }

        Matcher m = sBadgePattern.matcher(this.mName);
        if (m.find() && m.groupCount() > 0) {
            this.mName = this.mName.replace(m.group(0), "");

            BadgeModel model = new BadgeModel();
            model.source = m.group(1);
            model.title = m.group(2);
            return model;
        }
        
        m = sFlagPattern.matcher(this.mName);
        if (m.find() && m.groupCount() > 0) {
            this.mName = this.mName.replace(m.group(0), "");

            BadgeModel model = new BadgeModel();
            model.source = m.group(1);
            return model;
        }

        return null;
    }

    private void parseReferences() {
        String comment = this.mModel.getComment();

        if (comment == null) {
            MyLog.v("PostItemViewModel", "comment == null");
            return;
        }

        Matcher m = sReplyLinkFullPattern.matcher(comment);
        while (m.find()) {
            if (m.groupCount() > 0 && !this.refersTo.contains(m.group(1))) {
                this.refersTo.add(m.group(1));
            }
        }
    }

    private SpannableStringBuilder createSpannedComment() {
        if (StringUtils.isEmpty(this.mModel.getComment())) {
            return new SpannableStringBuilder("");
        }

        String fixedComment = HtmlUtils.fixHtmlTags(this.mModel.getComment());
        SpannableStringBuilder builder = HtmlUtils.createSpannedFromHtml(fixedComment, this.mTheme);

        URLSpan[] urlSpans = builder.getSpans(0, builder.length(), URLSpan.class);
        if (urlSpans.length > 0) {
            this.mHasUrlSpans = true;
            HtmlUtils.replaceUrls(builder, this.mUrlListener, this.mTheme);
        }

        return builder;
    }

    public void makeCommentFloat(FloatImageModel floatModel) {
        // Игнорируем, если был уже сделан или у поста нет прикрепленного файла
        if (this.canMakeCommentFloat()) {
            this.isFloatImageComment = true;
            FlowTextHelper.tryFlowText(this.getSpannedComment(), floatModel);
        }
    }

    public void addReferenceFrom(String postNumber) {
        this.referencesFrom.add(postNumber);
        this.mCachedReferencesString = null;
    }

    public int getPosition() {
        return this.mPosition;
    }

    public String getNumber() {
        return this.mModel.getNumber();
    }
    
    public BadgeModel getBadge() {
        return this.mBadge;
    }

    public String getParentThreadNumber() {
        String parent = this.mModel.getParentThread();
        return parent != null && !parent.equals("0") ? parent : this.getNumber();
    }

    public String getName() {
        return this.mName;
    }
    
    public String getTrip() {
        return this.mModel.getTrip();
    }
    
    public String getSubject() {
        return StringUtils.emptyIfNull(this.mModel.getSubject());
    }
    
    public boolean isSage() {
        return "mailto:sage".equals(this.mModel.getEmail());
    }
    
    public boolean isOp() {
        return this.mModel.isOp();
    }

    public boolean hasAttachment() {
        return ThreadPostUtils.hasAttachment(this.mModel);
    }
    
    public int getAttachmentsNumber() {
        return this.mModel.getAttachments().size();
    }

    public AttachmentInfo getAttachment(int index) {
        if(index >= this.getAttachmentsNumber()) {
            return null;
        }
        
        if (this.mAttachments[index] == null) {
            this.mAttachments[index] = new AttachmentInfo(this.mModel.getAttachments().get(index), this.mBoardName, this.mThreadNumber);
        }

        return this.mAttachments[index];
    }

    public SpannableStringBuilder getSpannedComment() {
        return this.mSpannedComment;
    }

    public ArrayList<String> getRefersTo() {
        return this.refersTo;
    }

    public String getPostDate(Context context) {
        if (this.mPostDate == null || this.mIsLocalDateTime != this.mSettings.isLocalDateTime()) {
            this.mIsLocalDateTime = this.mSettings.isLocalDateTime();
            String formattedDate = this.mIsLocalDateTime
                    ? ThreadPostUtils.getLocalDateFromTimestamp(context, this.mModel.getTimestamp())
                    : ThreadPostUtils.getMoscowDateFromTimestamp(context, this.mModel.getTimestamp());

            this.mPostDate = formattedDate;
        }

        return this.mPostDate;
    }

    public boolean hasUrls() {
        return this.mHasUrlSpans;
    }

    public boolean hasReferencesFrom() {
        return !this.referencesFrom.isEmpty();
    }

    public boolean isLongTextExpanded() {
        return this.mIsLongTextExpanded;
    }

    public void setLongTextExpanded(boolean isExpanded) {
        this.mIsLongTextExpanded = isExpanded;
    }

    public SpannableStringBuilder getReferencesFromAsSpannableString() {
        if (this.mCachedReferencesString == null) {
            String firstWord = this.mResources.getString(R.string.postitem_replies);
            this.mCachedReferencesString = this.createReferencesString(firstWord, this.referencesFrom);
        }

        return this.mCachedReferencesString;
    }

    private SpannableStringBuilder createReferencesString(String firstWord, ArrayList<String> references) {
        if (references.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(firstWord);
        sb.append(" ");

        Iterator<String> iterator = references.iterator();
        // Собираю список ссылок в одну строку, разделенную запятыми
        while (iterator.hasNext()) {
            String refNumber = iterator.next();

            String refUrl = this.mDvachUriBuilder.createPostUri(this.mBoardName, this.mThreadNumber, refNumber);
            // String htmlLink = String.format("<a href=\"%s\">%s</a>", refUrl,
            // "&gt;&gt;" + refNumber);
            sb.append("<a href=\"" + refUrl + "\">" + "&gt;&gt;" + refNumber + "</a>");

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        // Разбираю строку на объекты-ссылки и добавляю обработчики событий
        String joinedLinks = sb.toString();
        SpannableStringBuilder builder = (SpannableStringBuilder) MyHtml.fromHtml(joinedLinks);
        HtmlUtils.replaceUrls(builder, this.mUrlListener, this.mTheme);

        return builder;
    }

    /**
     * Можно поставить обтекание текста если версия 2.2 и к посту прикреплено
     * изображение
     */
    public boolean canMakeCommentFloat() {
        return FlowTextHelper.sNewClassAvailable && !this.isFloatImageComment && (this.getAttachmentsNumber() == 1);
    }

    public boolean isCommentFloat() {
        return this.isFloatImageComment;
    }
}
