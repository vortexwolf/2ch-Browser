package com.vortexwolf.dvach.common.view;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.Html;
import com.vortexwolf.dvach.common.library.Html.TagHandler;
import com.vortexwolf.dvach.common.library.HtmlToSpannedConverter;
import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;

import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class UnknownTagsHandler implements TagHandler {

	private final Theme mTheme;
	private final int mPostQuoteForeground;
	private final int mSpoilerForeground;
	private final int mSpoilerBackground;
	
	public UnknownTagsHandler(Theme theme) {
		super();
		this.mTheme = theme;

		TypedArray a = this.mTheme.obtainStyledAttributes(R.styleable.Theme);
		this.mPostQuoteForeground = a.getColor(R.styleable.Theme_postQuoteForeground, -1);
		this.mSpoilerForeground = a.getColor(R.styleable.Theme_spoilerForeground, -1);
		this.mSpoilerBackground = a.getColor(R.styleable.Theme_spoilerBackground, -1);
	}

	@Override
	public void handleTag(boolean opening, String tag, SpannableStringBuilder output, Attributes attributes) {
		if(tag == "span"){
			if(opening) {
				startSpan(output, attributes);
			}
			else {
				endSpan(output);
			}
		}
		else if (tag == "code"){
			if(opening) {
				HtmlToSpannedConverter.start(output, new Code());
				HtmlToSpannedConverter.start(output, new Code());
			}
			else {
				HtmlToSpannedConverter.end(output, Code.class, new TypefaceSpan("monospace"));
				HtmlToSpannedConverter.end(output, Code.class, new RelativeSizeSpan(0.7f));
			}
		}
	}
	

    private void startSpan(SpannableStringBuilder text, Attributes attributes) {
        Span span = Span.fromAttributes(attributes);

        int len = text.length();
        text.setSpan(span, len, len, Spannable.SPAN_MARK_MARK);
    }

    private void endSpan(SpannableStringBuilder text) {
        int len = text.length();
        Span span = HtmlToSpannedConverter.getLast(text, Span.class);
        int where = text.getSpanStart(span);

        text.removeSpan(span);

        if(where == len) {
        	return;
        }
        
        if (span.mSpanType != null) {
        	switch(span.mSpanType){
        		case QUOTE:
        			text.setSpan(new ForegroundColorSpan(mPostQuoteForeground), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			break;
        		case SPOILER:
        			text.setSpan(new ForegroundColorSpan(mSpoilerForeground), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			text.setSpan(new BackgroundColorSpan(mSpoilerBackground), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			break;
        		case STRIKE:
        			text.setSpan(new StrikethroughSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			break;
        		case UNDERLINE:
        			text.setSpan(new UnderlineSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			break;
        		case COLOR:
        			ColorSpan colorSpan = (ColorSpan)span;
        			text.setSpan(new ForegroundColorSpan(colorSpan.mColor), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        			break;
        	}
        }
    }
    
    private static class Code { }
    
    private static enum SpanType { QUOTE, SPOILER, STRIKE, UNDERLINE, COLOR };
    private static class Span {
    	private static HashMap<String, SpanType> sMap = new HashMap<String, SpanType>();
        public SpanType mSpanType;
        
        static {
        	sMap.put("unkfunc", SpanType.QUOTE);
        	sMap.put("spoiler", SpanType.SPOILER);
        	sMap.put("s", SpanType.STRIKE);
        	sMap.put("u", SpanType.UNDERLINE);
        }

        public Span(SpanType spanType) {
        	mSpanType = spanType;
        }
        
        public static Span fromAttributes(final Attributes attributes){
        	// Проверяем класс из зарегистрированных типов
            String spanClass = attributes.getValue("", "class");
        	SpanType type = sMap.get(spanClass);
        	
        	// Иногда бывают span-теги просто для раскраски текста (автозамена "школьник-сосницкий" и т.п.)
        	if(type == null){
        		String style = StringUtils.emptyIfNull(attributes.getValue("", "style"));
    			Integer color = HtmlUtils.getIntFontColor(style);
    			
    			if(color != null){
    				return new ColorSpan(color | Color.BLACK);
    			}
        	}
        	
        	return new Span(type);
        }
    }
    
    private static class ColorSpan extends Span {
    	public int mColor;
    	
    	public ColorSpan(int color){
    		super(SpanType.COLOR);
    		
    		mColor = color;
    	}
    }
}
