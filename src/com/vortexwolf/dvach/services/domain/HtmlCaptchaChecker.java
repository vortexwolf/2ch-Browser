package com.vortexwolf.dvach.services.domain;

import android.net.Uri;
import android.text.Html;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class HtmlCaptchaChecker implements IHtmlCaptchaChecker {
	//private static final String CHECK_URI_FORMAT = "http://2ch.so/%s/wakaba.pl?task=captcha&thread=%s&dummy=";
	//private static final String HTML_RESPONSE_SKIP_CAPTCHA = "Вам не надо вводить капчу.";
	
	private final IHttpStringReader mHttpStringReader;
	private final DvachUriBuilder mDvachUriBuilder;
	
	public HtmlCaptchaChecker(IHttpStringReader httpStringReader, DvachUriBuilder dvachUriBuilder) {
		this.mHttpStringReader = httpStringReader;
		this.mDvachUriBuilder = dvachUriBuilder;
	}
	
	@Override
	public boolean canSkipCaptcha(String boardName, String threadNumber){
		
		Uri uri = this.mDvachUriBuilder.create2chUri("makaba/captcha?code=");
		String captchaBlock = this.mHttpStringReader.fromUri(uri.toString());
		
		return checkHtmlBlock(captchaBlock);
	}
	
	public boolean checkHtmlBlock(String captchaBlock){
		if(captchaBlock == null){
			return false;
		}
		
		if(captchaBlock.contains("OK")) {
			return true;
		}
		
/*		String text = Html.fromHtml(captchaBlock).toString();
		if(text.contains(HTML_RESPONSE_SKIP_CAPTCHA)){
			return true;
		}*/
		
		return false;
	}
}
