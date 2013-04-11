package com.vortexwolf.dvach.models.presentation;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.FlowTextHelper;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostItemViewModel {

    private static final Pattern sReplyLinkFullPattern = Pattern.compile("<a.+?>&gt;&gt;(\\d+)</a>");

    private final int mPosition;
    private final PostInfo mModel;
    private final Theme mTheme;
    private final IURLSpanClickListener mUrlListener;
    private final DvachUriBuilder mDvachUriBuilder;
    private final ApplicationSettings mSettings;

    private SpannableStringBuilder mSpannedComment = null;
    private AttachmentInfo mAttachment;
    private String mPostDate = null;

    private final ArrayList<String> refersTo = new ArrayList<String>();
    private final ArrayList<String> referencesFrom = new ArrayList<String>();

    public boolean isFloatImageComment = false;

    public PostItemViewModel(int position, PostInfo model, Theme theme, ApplicationSettings settings, IURLSpanClickListener listener, DvachUriBuilder dvachUriBuilder) {
        this.mModel = model;
        this.mTheme = theme;
        this.mUrlListener = listener;
        this.mPosition = position;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mSettings = settings;

        this.parseReferences();
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
        HtmlUtils.replaceUrls(builder, this.mUrlListener, this.mTheme);

        return builder;
    }

    public void makeCommentFloat(FloatImageModel floatModel) {
        // Игнорируем, если был уже сделан или у поста нет прикрепленного файла
        if (this.canMakeCommentFloat()) {
            this.isFloatImageComment = true;
            this.mSpannedComment = FlowTextHelper.tryFlowText(this.getSpannedComment(), floatModel);
        }
    }

    public void addReferenceFrom(String postNumber) {
        this.referencesFrom.add(postNumber);
    }

    public int getPosition() {
        return this.mPosition;
    }

    public String getNumber() {
        return this.mModel.getNum();
    }
    
    public String getParentThreadNumber() {
        String parent = this.mModel.getParent();
        return parent != null && !parent.equals("0") ? parent : this.getNumber();
    }

    public String getName() {
        return this.mModel.getName();
    }

    public boolean hasAttachment() {
        return ThreadPostUtils.hasAttachment(this.mModel);
    }

    public AttachmentInfo getAttachment(String boardCode) {
        if (this.mAttachment == null && this.hasAttachment()) {
            this.mAttachment = new AttachmentInfo(this.mModel, boardCode, this.mDvachUriBuilder);
        }

        return this.mAttachment;
    }

    public SpannableStringBuilder getSpannedComment() {
        if (this.mSpannedComment == null) {
            this.mSpannedComment = this.createSpannedComment();
        }

        return this.mSpannedComment;
    }

    public ArrayList<String> getRefersTo() {
        return this.refersTo;
    }

    public String getPostDate(Context context) {
        if (this.mPostDate == null) {
            long realTimeStamp = (this.mModel.getTimestamp() - 3600) * 1000; // the
                                                                             // returned
                                                                             // value
                                                                             // is
                                                                             // incorrect,
                                                                             // I
                                                                             // need
                                                                             // to
                                                                             // substract
                                                                             // 1
                                                                             // hour
            Date date = this.mSettings.isLocalDateTime()
                    ? ThreadPostUtils.getLocalDateFromTimestamp(realTimeStamp)
                    : ThreadPostUtils.getMoscowDateFromTimestamp(realTimeStamp);

            this.mPostDate = DateFormat.getDateFormat(context).format(date) + ", " + DateFormat.getTimeFormat(context).format(date);
        }

        return this.mPostDate;
    }

    public boolean hasReferencesFrom() {
        return !this.referencesFrom.isEmpty();
    }

    public SpannableStringBuilder getReferencesFromAsSpannableString(Resources res, String boardName, String threadNumber) {
        String firstWord = res.getString(R.string.postitem_replies);
        SpannableStringBuilder builder = this.createReferencesString(firstWord, this.referencesFrom, boardName, threadNumber);

        return builder;
    }

    private SpannableStringBuilder createReferencesString(String firstWord, ArrayList<String> references, String boardName, String threadNumber) {
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

            String refUrl = this.mDvachUriBuilder.create2chPostUrl(boardName, threadNumber, refNumber);
            // String htmlLink = String.format("<a href=\"%s\">%s</a>", refUrl,
            // "&gt;&gt;" + refNumber);
            sb.append("<a href=\"");
            sb.append(refUrl);
            sb.append("\">");
            sb.append("&gt;&gt;");
            sb.append(refNumber);
            sb.append("</a>");

            if (iterator.hasNext()) {
                sb.append(", ");
            } else {
                sb.append("\n");
            }
        }
        // Разбираю строку на объекты-ссылки и добавляю обработчики событий
        String joinedLinks = sb.toString();
        SpannableStringBuilder builder = (SpannableStringBuilder) Html.fromHtml(joinedLinks);
        HtmlUtils.replaceUrls(builder, this.mUrlListener, this.mTheme);

        return builder;
    }

    /**
     * Можно поставить обтекание текста если версия 2.2 и к посту прикреплено
     * изображение
     */
    public boolean canMakeCommentFloat() {
        return FlowTextHelper.sNewClassAvailable && !this.isFloatImageComment && this.hasAttachment();
    }

    public boolean isCommentFloat() {
        return this.isFloatImageComment;
    }
}
