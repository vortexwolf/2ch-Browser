package com.vortexwolf.dvach.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.controls.ClickableURLSpan;
import com.vortexwolf.dvach.common.library.Html;
import com.vortexwolf.dvach.common.library.UnknownTagsHandler;
import com.vortexwolf.dvach.interfaces.INetworkResourceLoader;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import android.content.res.TypedArray;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.httpimage.NetworkResourceLoader;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

public class HtmlUtils {
	private static final Pattern styleColorPattern = Pattern.compile(".*?color: rgb\\((\\d+), (\\d+), (\\d+)\\);.*");
	
	private static final DefaultHttpClient httpClient = MainApplication.getHttpClient();
	//Картинки со смайликами во время всяких праздников
	private static final Html.ImageGetter imageGetter = new Html.ImageGetter(){
		
		private final INetworkResourceLoader mNetworkResourceLoader = new NetworkResourceLoader(httpClient);
		
		@Override
		public Drawable getDrawable(String ref) {
			Uri uri = UriUtils.adjust2chRelativeUri(Uri.parse(ref));
			
			Bitmap bmp = mNetworkResourceLoader.loadBitmap(uri);
			Drawable d = new BitmapDrawable(bmp);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};
	
	public static SpannableStringBuilder createSpannedFromHtml(String htmlText, Theme theme){
		SpannableStringBuilder builder = (SpannableStringBuilder)Html.fromHtml(StringUtils.emptyIfNull(htmlText), imageGetter, new UnknownTagsHandler(theme));
        
        return builder;
	}

	/** Добавляет обработчики событий к ссылкам */
	public static SpannableStringBuilder replaceUrls(SpannableStringBuilder builder, IURLSpanClickListener listener, Theme theme){
        if(listener != null){
	        URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);
	        
	        if(spans.length > 0){
	        	TypedArray a = theme.obtainStyledAttributes(R.styleable.Theme);
	    		int urlColor = a.getColor(R.styleable.Theme_urlLinkForeground, Color.BLUE);
	    		
		        for(URLSpan span : spans)
		        {
		        	ClickableURLSpan newSpan = ClickableURLSpan.replaceURLSpan(builder, span, urlColor);
		        	newSpan.setOnClickListener(listener);
		        }
	        }
        }
        
        return builder;
	}
	
	public static String fixHtmlTags(String htmlText){
		if(htmlText == null) return null;
		
		String result = htmlText;
		//Убираем абзацы
		if(result.startsWith("<p>") && result.endsWith("</p>")){
			result = "<span>"+result.substring(3, result.length() - 4)+"</span>"; //except <p>
		}
		
		return result;
	}
	
	public static Integer getIntFontColor(String htmlText){
		if(htmlText == null) return null;
		
		Matcher m = styleColorPattern.matcher(htmlText);
		while (m.find() && m.groupCount() == 3) {
			Integer n1 = Integer.valueOf(m.group(1));
			Integer n2 = Integer.valueOf(m.group(2));
			Integer n3 = Integer.valueOf(m.group(3));
			int c = Color.rgb(n1, n2, n3);
			
			return c;
		}
		return null;
	}
	
	public static String getStringFontColor(String htmlText){
		Integer color = getIntFontColor(htmlText);
		
		if(color != null){
			return String.format("#%06X", (0xFFFFFF & color));
		}
		
		return null;
	}
}
