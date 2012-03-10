package com.vortexwolf.dvach.common.view;

import com.vortexwolf.dvach.common.utils.XmlUtils;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;

public class ClickableURLSpan extends ClickableSpan {
    private final String mURL;
	private IURLSpanClickListener mListener;
    
    public ClickableURLSpan(String url) {  
        mURL = url;
    }  
  
    @Override  
    public void onClick(View widget) {  
        if(mListener != null)
        	mListener.onClick(widget, mURL);  
    }  
    
    public void setOnClickListener(IURLSpanClickListener listener){
    	mListener = listener;
    }
    
    public static ClickableURLSpan replaceURLSpan(SpannableStringBuilder builder, URLSpan span, int color){
    	int start = builder.getSpanStart(span);
    	int end = builder.getSpanEnd(span);
    	String url = span.getURL();
    	
    	builder.removeSpan(span);
    	
    	ClickableURLSpan newSpan = new ClickableURLSpan(url);
    	builder.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    	builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    	
    	return newSpan;
    }
}
