package com.vortexwolf.dvach.api;

import org.apache.http.impl.client.DefaultHttpClient;

import android.text.Html;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.http.HttpStringReader;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;

public class HtmlCaptchaChecker implements IHtmlCaptchaChecker {
	private final IHttpStringReader mHttpStringReader;
	private static final String CHECK_URI_FORMAT = "http://2ch.so/%s/wakaba.pl?task=captcha&thread=%s&dummy=";
	
	public HtmlCaptchaChecker(IHttpStringReader httpStringReader) {
		this.mHttpStringReader = httpStringReader;
	}
	
	@Override
	public boolean canSkipCaptcha(String boardName, String threadNumber){
		
		String uri = String.format(CHECK_URI_FORMAT, boardName, StringUtils.emptyIfNull(threadNumber));
		
		String captchaBlock = this.mHttpStringReader.fromUri(uri);
		
		return checkHtmlBlock(captchaBlock);
	}
	
	public boolean checkHtmlBlock(String captchaBlock){
		if(captchaBlock == null){
			return false;
		}
		
		String text = Html.fromHtml(captchaBlock).toString();
		if(text.contains(Constants.HTML_RESPONSE_SKIP_CAPTCHA)){
			return true;
		}
		
		return false;
	}
}
