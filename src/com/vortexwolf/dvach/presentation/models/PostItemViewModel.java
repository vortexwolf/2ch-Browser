package com.vortexwolf.dvach.presentation.models;

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
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.posts.FlowTextHelper;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostItemViewModel {


	private static final Pattern sReplyLinkFullPattern = Pattern.compile("<a.+?>&gt;&gt;(\\d+)</a>");
	
	private final int mPosition;
	private final PostInfo mModel;
	private final Theme mTheme;
	private final IURLSpanClickListener mUrlListener;

	private SpannableStringBuilder mSpannedComment = null;
	private AttachmentInfo mAttachment;
	private String mPostDate = null;
	private final String mPostId;
	
	private final ArrayList<String> refersTo = new ArrayList<String>();
	private final ArrayList<String> referencesFrom = new ArrayList<String>();
	
	private boolean isFloatImageComment = false;

	
	public PostItemViewModel(int position, PostInfo model, Theme theme, IURLSpanClickListener listener) {
		this.mModel = model;
		this.mTheme = theme;
		this.mUrlListener = listener;
		this.mPosition = position;
		
		this.mPostId = HtmlUtils.parseIdFromName(this.mModel.getName());
		this.mModel.setName(null); // чистим из памяти
		this.parseReferences();
	}

	private void parseReferences(){
		String comment = this.mModel.getComment();
    	Matcher m = sReplyLinkFullPattern.matcher(comment);
		while (m.find()) {
			if(m.groupCount() > 0 && !refersTo.contains(m.group(1))){
				refersTo.add(m.group(1));
			}
		}
	}
	
	private SpannableStringBuilder createSpannedComment(){
		String fixedComment = HtmlUtils.fixHtmlTags(this.mModel.getComment());
		SpannableStringBuilder builder = (SpannableStringBuilder)HtmlUtils.createSpannedFromHtml(fixedComment, this.mTheme);
        HtmlUtils.replaceUrls(builder, this.mUrlListener, mTheme);
        
        this.mModel.setComment(null); // удаляем лишние данные из памяти

        return builder;
	}
	
	public void makeCommentFloat(FloatImageModel floatModel){
		//Игнорируем, если был уже сделан или у поста нет прикрепленного файла
        if(this.canMakeCommentFloat()){
        	this.isFloatImageComment = true;
        	this.mSpannedComment = FlowTextHelper.tryFlowText(this.getSpannedComment(), floatModel);
        }
	}
	
	public void addReferenceFrom(String postNumber){
		referencesFrom.add(postNumber);
	}

	public int getPosition(){
		return mPosition;
	}
	
	public String getNumber(){
		return mModel.getNum();
	}
	
	public String getPostId(){
		return mPostId;
	}
	
	public boolean hasAttachment(){
		return ThreadPostUtils.hasAttachment(this.mModel);
	}

	public AttachmentInfo getAttachment(String boardCode){
		if(this.mAttachment == null && this.hasAttachment()){
			this.mAttachment = new AttachmentInfo(this.mModel, boardCode);
		}
		
		return mAttachment;
	}

	public SpannableStringBuilder getSpannedComment() {
		if(this.mSpannedComment == null){
			this.mSpannedComment = this.createSpannedComment();
		}
		
		return mSpannedComment;
	}
	
	public ArrayList<String> getRefersTo(){
		return refersTo;
	}
	
	public String getPostDate(Context context){
		if (this.mPostDate == null){
			Date date = new Date(this.mModel.getTimestamp() * 1000);
			this.mPostDate =  DateFormat.getDateFormat(context).format(date) + ", " + DateFormat.getTimeFormat(context).format(date);
		}

		return this.mPostDate;
	}
	
	public boolean hasReferencesFrom(){
		return !this.referencesFrom.isEmpty();
	}
	
	public SpannableStringBuilder getReferencesFromAsSpannableString(Resources res, String boardName, String threadNumber){
		String firstWord = res.getString(R.string.postitem_replies);
		SpannableStringBuilder builder = createReferencesString(firstWord, this.referencesFrom, boardName, threadNumber);
				
		return builder;
	}
	
	private SpannableStringBuilder createReferencesString(String firstWord, ArrayList<String> references, String boardName, String threadNumber){
		if(references.isEmpty()) return null;
				
		StringBuilder sb = new StringBuilder();
		sb.append(firstWord);
		sb.append(" ");
		
		Iterator<String> iterator = references.iterator();
		// Собираю список ссылок в одну строку, разделенную запятыми
		while(iterator.hasNext()){
			String refNumber = iterator.next();

			String refUrl = UriUtils.create2chPostURL(boardName, threadNumber, refNumber);
			//String htmlLink = String.format("<a href=\"%s\">%s</a>", refUrl, "&gt;&gt;" + refNumber);
			sb.append("<a href=\"");
			sb.append(refUrl);
			sb.append("\">");
			sb.append("&gt;&gt;");
			sb.append(refNumber);
			sb.append("</a>");
			
			if(iterator.hasNext()){
				sb.append(", ");
			}
		}
		// Разбираю строку на объекты-ссылки и добавляю обработчики событий
		String joinedLinks = sb.toString();
		SpannableStringBuilder builder = (SpannableStringBuilder)Html.fromHtml(joinedLinks);
		HtmlUtils.replaceUrls(builder, this.mUrlListener, mTheme);
		
		return builder;
	}

	/** Можно поставить обтекание текста если версия 2.2 и к посту прикреплено изображение */
	public boolean canMakeCommentFloat() {
		return FlowTextHelper.sNewClassAvailable && !this.isFloatImageComment && this.hasAttachment();
	}
}
